package com.enduranceflow.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enduranceflow.core.domain.model.Gender
import com.enduranceflow.core.domain.model.Sport
import com.enduranceflow.core.domain.repository.AvailabilityRepository
import com.enduranceflow.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

/**
 * ViewModel pour l'écran Onboarding
 *
 * Responsabilités :
 * - Collecter les informations de l'utilisateur (Genre, Équipement)
 * - Créer le profil initial
 * - Créer la configuration équipement
 * - Créer le planning de disponibilités par défaut
 * - Déterminer si calibration nécessaire (VMA/FTP inconnus)
 *
 * Injecté par Hilt (@HiltViewModel)
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val availabilityRepository: AvailabilityRepository
) : ViewModel() {

    // État de l'UI (Observable par l'écran)
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /**
     * Met à jour le genre sélectionné
     *
     * @param gender FEMALE ou MALE
     *
     * Impact : Influence le ton de l'IA
     * - FEMALE → Empathique/Bienveillant
     * - MALE → Analytique/Factuel
     */
    fun onGenderSelected(gender: Gender) {
        _uiState.value = _uiState.value.copy(selectedGender = gender)
    }

    /**
     * Met à jour le statut du capteur de puissance
     *
     * @param hasPowerMeter true si l'utilisateur possède un capteur
     */
    fun onPowerMeterChanged(hasPowerMeter: Boolean) {
        _uiState.value = _uiState.value.copy(hasPowerMeter = hasPowerMeter)
    }

    /**
     * Met à jour les valeurs VMA/FTP si l'utilisateur les connaît déjà
     *
     * @param vma VMA en km/h (nullable)
     * @param ftp FTP en Watts (nullable)
     */
    fun onPhysioDataEntered(vma: Double?, ftp: Int?) {
        _uiState.value = _uiState.value.copy(
            knownVMA = vma,
            knownFTP = ftp
        )
    }

    /**
     * Toggle un sport dans la liste des sports sélectionnés
     *
     * @param sport Le sport à ajouter/retirer (RUNNING ou CYCLING)
     */
    fun onSportToggled(sport: Sport) {
        val currentSports = _uiState.value.selectedSports.toMutableSet()
        if (currentSports.contains(sport)) {
            currentSports.remove(sport)
        } else {
            currentSports.add(sport)
        }
        _uiState.value = _uiState.value.copy(selectedSports = currentSports)
    }

    /**
     * Toggle un jour dans la liste des jours disponibles
     *
     * @param day Le jour à ajouter/retirer
     */
    fun onDayToggled(day: DayOfWeek) {
        val currentDays = _uiState.value.selectedDays.toMutableSet()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.value = _uiState.value.copy(selectedDays = currentDays)
    }

    /**
     * Enregistre les données de profil personnel
     *
     * @param age Âge en années
     * @param weight Poids en kg
     * @param height Taille en cm
     * @param maxHR FC Max en bpm (nullable)
     */
    fun onPersonalProfileEntered(age: Int, weight: Double, height: Int, maxHR: Int?) {
        _uiState.value = _uiState.value.copy(
            age = age,
            weight = weight,
            height = height,
            maxHR = maxHR
        )
    }

    /**
     * Enregistre l'objectif sélectionné
     *
     * @param goal L'objectif d'entraînement
     */
    fun onGoalSelected(goal: TrainingGoal) {
        _uiState.value = _uiState.value.copy(selectedGoal = goal)
    }

    /**
     * Enregistre les détails de l'événement cible
     *
     * @param eventType Type d'événement (Marathon, 10k, etc.)
     * @param eventDate Date au format YYYY-MM-DD
     * @param eventName Nom de l'événement (nullable)
     * @param distance Distance en km
     */
    fun onTargetEventEntered(eventType: EventType, eventDate: String, eventName: String?, distance: Double) {
        _uiState.value = _uiState.value.copy(
            targetEventType = eventType,
            targetEventDate = eventDate,
            targetEventName = eventName,
            targetEventDistance = distance
        )
    }

    /**
     * Finalise l'onboarding et crée le profil initial
     *
     * Actions :
     * 1. Crée le profil AthleteProfile
     * 2. Crée la configuration équipement
     * 3. Crée le planning de disponibilités personnalisé (sports + jours choisis)
     * 4. Détermine si calibration nécessaire
     *
     * @param onComplete Callback appelé à la fin (navigation)
     */
    fun completeOnboarding(onComplete: (needsCalibration: Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val state = _uiState.value

                // 1. Créer le profil athlète avec toutes les données collectées
                profileRepository.saveProfile(
                    gender = state.selectedGender ?: Gender.MALE,
                    vma = state.knownVMA,
                    ftp = state.knownFTP,
                    maxHR = state.maxHR,
                    age = state.age,
                    weight = state.weight,
                    height = state.height
                )

                // 2. Créer la configuration équipement
                profileRepository.saveEquipmentConfig(
                    hasPowerMeter = state.hasPowerMeter,
                    hasTreadmill = false,
                    hasHomeTrainer = false
                )

                // 3. Créer le planning personnalisé basé sur les choix utilisateur
                if (state.selectedSports.isNotEmpty() && state.selectedDays.isNotEmpty()) {
                    availabilityRepository.createCustomWeeklyPlan(
                        selectedSports = state.selectedSports,
                        selectedDays = state.selectedDays
                    )
                } else {
                    // Fallback : créer le planning par défaut si les choix ne sont pas renseignés
                    availabilityRepository.createDefaultWeeklyPlan()
                }

                // 4. Vérifier si calibration nécessaire
                val needsCalibration = state.knownVMA == null || state.knownFTP == null

                _uiState.value = _uiState.value.copy(isLoading = false)

                // Navigation
                onComplete(needsCalibration)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors de la création du profil: ${e.message}"
                )
            }
        }
    }
}

/**
 * État de l'UI pour l'écran Onboarding
 *
 * Contient toutes les données nécessaires pour afficher les écrans
 * et créer le profil complet de l'athlète
 */
data class OnboardingUiState(
    // Écran 1: Genre + Équipement + VMA/FTP
    val selectedGender: Gender? = null,
    val hasPowerMeter: Boolean = false,
    val knownVMA: Double? = null,
    val knownFTP: Int? = null,

    // Écran 2: Profil personnel
    val age: Int? = null,
    val weight: Double? = null,
    val height: Int? = null,
    val maxHR: Int? = null,

    // Écran 3: Sports pratiqués
    val selectedSports: Set<Sport> = emptySet(),

    // Écran 4: Objectif
    val selectedGoal: TrainingGoal? = null,

    // Écran 5: Événement cible (si compétition)
    val targetEventType: EventType? = null,
    val targetEventDate: String? = null,
    val targetEventName: String? = null,
    val targetEventDistance: Double? = null,

    // Écran 6: Disponibilités
    val selectedDays: Set<DayOfWeek> = emptySet(),

    // État du chargement et erreurs
    val isLoading: Boolean = false,
    val error: String? = null
)
