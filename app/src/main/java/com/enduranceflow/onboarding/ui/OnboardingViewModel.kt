package com.enduranceflow.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enduranceflow.core.domain.model.Gender
import com.enduranceflow.core.domain.repository.AvailabilityRepository
import com.enduranceflow.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
     * Finalise l'onboarding et crée le profil initial
     *
     * Actions :
     * 1. Crée le profil AthleteProfile
     * 2. Crée la configuration équipement
     * 3. Crée le planning de disponibilités par défaut
     * 4. Détermine si calibration nécessaire
     *
     * @param onComplete Callback appelé à la fin (navigation)
     */
    fun completeOnboarding(onComplete: (needsCalibration: Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val state = _uiState.value

                // 1. Créer le profil athlète
                profileRepository.saveProfile(
                    gender = state.selectedGender ?: Gender.MALE,
                    vma = state.knownVMA,
                    ftp = state.knownFTP,
                    maxHR = null
                )

                // 2. Créer la configuration équipement
                profileRepository.saveEquipmentConfig(
                    hasPowerMeter = state.hasPowerMeter,
                    hasTreadmill = false,
                    hasHomeTrainer = false
                )

                // 3. Créer le planning de disponibilités par défaut
                availabilityRepository.createDefaultWeeklyPlan()

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
 * Contient toutes les données nécessaires pour afficher l'écran
 */
data class OnboardingUiState(
    val selectedGender: Gender? = null,
    val hasPowerMeter: Boolean = false,
    val knownVMA: Double? = null,
    val knownFTP: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
