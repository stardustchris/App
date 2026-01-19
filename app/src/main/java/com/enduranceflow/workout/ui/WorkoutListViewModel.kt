package com.enduranceflow.workout.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enduranceflow.workout.data.entity.DailyWorkoutEntity
import com.enduranceflow.workout.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran WorkoutList (écran principal)
 *
 * Responsabilités :
 * - Afficher la liste des séances à venir
 * - Basculer le mode Indoor/Outdoor
 * - Marquer une séance comme complétée
 * - Déclencher la génération de nouvelles séances (via IA)
 * - Gérer les feedbacks post-séance
 *
 * User Story (context.json) :
 * "Je peux basculer une séance en mode 'Intérieur' :
 *  les cibles changent (Vitesse → Tapis, Allure → Watts/Cardio)."
 *
 * Injecté par Hilt (@HiltViewModel)
 */
@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val aiRepository: com.enduranceflow.ai.domain.AIRepository
) : ViewModel() {

    // État de l'UI
    private val _uiState = MutableStateFlow(WorkoutListUiState())
    val uiState: StateFlow<WorkoutListUiState> = _uiState.asStateFlow()

    init {
        loadUpcomingWorkouts()
        checkFatigueStatus()
    }

    /**
     * Charge les séances à venir (non complétées)
     */
    private fun loadUpcomingWorkouts() {
        viewModelScope.launch {
            workoutRepository.getUpcomingWorkouts().collect { workouts ->
                _uiState.value = _uiState.value.copy(
                    upcomingWorkouts = workouts,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Vérifie l'état de fatigue de l'athlète
     * (Analyse les 7 derniers feedbacks)
     */
    private fun checkFatigueStatus() {
        viewModelScope.launch {
            val isFatigued = workoutRepository.isFatigued(limit = 7)
            val needsProgression = workoutRepository.needsProgression(limit = 7)
            val averageRPE = workoutRepository.getAverageRPE(limit = 7)

            _uiState.value = _uiState.value.copy(
                isFatigued = isFatigued,
                needsProgression = needsProgression,
                averageRPE = averageRPE
            )
        }
    }

    /**
     * Bascule le mode Indoor/Outdoor d'une séance
     *
     * @param workoutId ID de la séance
     * @param isIndoor true = Indoor, false = Outdoor
     *
     * User Story (context.json) :
     * "Je peux basculer une séance en mode 'Intérieur' :
     *  les cibles changent (Vitesse → Tapis, Allure → Watts/Cardio)."
     *
     * Impact sur TargetType :
     * - Running Outdoor : PACE (allure min/km)
     * - Running Indoor : SPEED (vitesse km/h)
     * - Cycling avec capteur : POWER (Watts)
     * - Cycling sans capteur : HEART_RATE (bpm)
     *
     * TODO: Implémenter le recalcul automatique du TargetType
     */
    fun toggleIndoorMode(workoutId: Long, isIndoor: Boolean) {
        viewModelScope.launch {
            try {
                workoutRepository.toggleIndoorMode(workoutId, isIndoor)
                // TODO: Recalculer le TargetType et la valeur cible selon le mode
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors du changement de mode: ${e.message}"
                )
            }
        }
    }

    /**
     * Marque une séance comme complétée
     *
     * @param workoutId ID de la séance
     * @param onComplete Callback pour afficher le formulaire de feedback
     *
     * Après cette action, l'UI devrait afficher un dialog
     * pour collecter le RPE (Rating of Perceived Exertion)
     */
    fun markWorkoutAsCompleted(workoutId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                workoutRepository.markWorkoutAsCompleted(workoutId)
                onComplete() // Afficher le dialog de feedback
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la validation: ${e.message}"
                )
            }
        }
    }

    /**
     * Soumet un feedback post-séance
     *
     * @param workoutId ID de la séance
     * @param rpe Rating of Perceived Exertion (1-10)
     * @param comment Commentaire libre (nullable)
     * @param tooEasy La séance était trop facile ?
     * @param tooHard La séance était trop difficile ?
     *
     * Core Entity (context.json) : "SessionFeedback (RPE)"
     *
     * Impact :
     * - L'IA analyse ces feedbacks pour adapter les prochaines séances
     * - Si trop de séances "trop difficiles" → Réduction de l'intensité
     * - Si trop de séances "trop faciles" → Augmentation de l'intensité
     */
    fun submitFeedback(
        workoutId: Long,
        rpe: Int,
        comment: String? = null,
        tooEasy: Boolean = false,
        tooHard: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                workoutRepository.submitFeedback(
                    workoutId = workoutId,
                    rpe = rpe,
                    comment = comment,
                    tooEasy = tooEasy,
                    tooHard = tooHard
                )

                // Rafraîchir l'état de fatigue après soumission
                checkFatigueStatus()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de l'enregistrement du feedback: ${e.message}"
                )
            }
        }
    }

    /**
     * Supprime une séance
     *
     * @param workoutId ID de la séance à supprimer
     */
    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workoutId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }

    /**
     * Génère de nouvelles séances pour la semaine via l'IA Gemini
     *
     * L'IA génère des séances en tenant compte :
     * - Du profil athlète (VMA, FTP, Genre)
     * - De la configuration équipement (capteur de puissance)
     * - Des disponibilités (AvailabilitySlots)
     * - De l'état de fatigue (feedbacks récents)
     * - Du ton adapté (Empathique/Analytique selon le genre)
     *
     * Algorithme :
     * 1. Active le loading state
     * 2. Appelle AIRepository.generateWeeklyWorkouts()
     * 3. L'AIRepository récupère le profil, disponibilités, fatigue
     * 4. L'AIEngineFactory sélectionne Gemini Nano ou Flash
     * 5. Les séances générées sont automatiquement sauvegardées en BDD
     * 6. Le Flow réactif met à jour l'UI automatiquement
     */
    fun generateWeeklyWorkouts() {
        viewModelScope.launch {
            android.util.Log.d("WorkoutListViewModel", "generateWeeklyWorkouts() called - Starting workout generation")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Appel à l'IA via AIRepository
                android.util.Log.d("WorkoutListViewModel", "Calling aiRepository.generateWeeklyWorkouts()")
                val generatedWorkouts = aiRepository.generateWeeklyWorkouts()
                android.util.Log.d("WorkoutListViewModel", "Received ${generatedWorkouts.size} workouts from AI")

                if (generatedWorkouts.isEmpty()) {
                    android.util.Log.w("WorkoutListViewModel", "No workouts generated - showing error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Aucune séance générée. Vérifie ton profil et tes disponibilités."
                    )
                } else {
                    // Succès : les séances sont automatiquement affichées via le Flow
                    android.util.Log.d("WorkoutListViewModel", "Workouts generated successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("WorkoutListViewModel", "Error generating workouts", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors de la génération: ${e.message}"
                )
            }
        }
    }

    /**
     * Efface l'erreur affichée
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * État de l'UI pour l'écran WorkoutList
 */
data class WorkoutListUiState(
    val upcomingWorkouts: List<DailyWorkoutEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,

    // Indicateurs de fatigue (pour afficher des alertes)
    val isFatigued: Boolean = false,
    val needsProgression: Boolean = false,
    val averageRPE: Double? = null
)
