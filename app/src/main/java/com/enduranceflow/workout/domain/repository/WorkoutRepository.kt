package com.enduranceflow.workout.domain.repository

import com.enduranceflow.core.domain.model.Sport
import com.enduranceflow.core.domain.model.TargetType
import com.enduranceflow.profile.domain.repository.ProfileRepository
import com.enduranceflow.workout.data.dao.DailyWorkoutDao
import com.enduranceflow.workout.data.dao.SessionFeedbackDao
import com.enduranceflow.workout.data.entity.DailyWorkoutEntity
import com.enduranceflow.workout.data.entity.SessionFeedbackEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour la gestion des séances d'entraînement
 *
 * Responsabilités :
 * - Gestion des séances quotidiennes (DailyWorkout)
 * - Gestion des feedbacks post-séance (SessionFeedback)
 * - Logique de basculement Indoor/Outdoor
 * - Adaptation automatique du TargetType selon le mode et l'équipement
 *
 * Injecté par Hilt dans les ViewModels
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val dailyWorkoutDao: DailyWorkoutDao,
    private val sessionFeedbackDao: SessionFeedbackDao,
    private val profileRepository: ProfileRepository
) {

    // ========== DailyWorkout ==========

    /**
     * Récupère toutes les séances à venir (non complétées)
     *
     * @return Flow<List<DailyWorkoutEntity>>
     */
    fun getUpcomingWorkouts(): Flow<List<DailyWorkoutEntity>> {
        return dailyWorkoutDao.getUpcomingWorkouts()
    }

    /**
     * Récupère les séances d'une semaine spécifique
     *
     * @param startDate Date de début (YYYY-MM-DD)
     * @param endDate Date de fin (YYYY-MM-DD)
     */
    fun getWorkoutsForWeek(startDate: String, endDate: String): Flow<List<DailyWorkoutEntity>> {
        return dailyWorkoutDao.getWorkoutsForWeek(startDate, endDate)
    }

    /**
     * Récupère une séance par son ID
     *
     * @param workoutId ID de la séance
     */
    fun getWorkoutById(workoutId: Long): Flow<DailyWorkoutEntity?> {
        return dailyWorkoutDao.getWorkoutById(workoutId)
    }

    /**
     * Crée une nouvelle séance d'entraînement
     *
     * @param date Date de la séance (YYYY-MM-DD)
     * @param sport Sport (RUNNING/CYCLING)
     * @param title Titre de la séance
     * @param description Description détaillée
     * @param targetType Type de cible (POWER/PACE/SPEED/HEART_RATE)
     * @param isIndoor Mode Indoor activé ?
     * @param durationMinutes Durée en minutes
     * @param targetValue Valeur cible
     *
     * @return L'ID de la séance créée
     */
    suspend fun createWorkout(
        date: String,
        sport: Sport,
        title: String,
        description: String,
        targetType: TargetType,
        isIndoor: Boolean = false,
        durationMinutes: Int,
        targetValue: Double
    ): Long {
        val workout = DailyWorkoutEntity(
            date = date,
            sport = sport,
            title = title,
            description = description,
            targetType = targetType,
            isIndoor = isIndoor,
            durationMinutes = durationMinutes,
            targetValue = targetValue
        )
        return dailyWorkoutDao.insertWorkout(workout)
    }

    /**
     * Crée plusieurs séances en batch (utilisé par l'IA)
     *
     * @param workouts Liste des séances à créer
     * @return Nombre de séances créées
     *
     * Utilisé par AIRepository pour enregistrer les séances générées par l'IA
     */
    suspend fun createWorkouts(workouts: List<DailyWorkoutEntity>): Int {
        return try {
            workouts.forEach { workout ->
                dailyWorkoutDao.insertWorkout(workout)
            }
            workouts.size
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Marque une séance comme complétée
     *
     * @param workoutId ID de la séance
     *
     * Après cet appel, l'UI devrait afficher le formulaire de feedback (RPE)
     */
    suspend fun markWorkoutAsCompleted(workoutId: Long) {
        dailyWorkoutDao.markAsCompleted(workoutId)
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
     * Recalcule automatiquement le TargetType selon :
     * - Running Outdoor : PACE (min/km)
     * - Running Indoor : SPEED (km/h sur tapis)
     * - Cycling avec capteur : POWER (Watts)
     * - Cycling sans capteur : HEART_RATE (bpm)
     */
    suspend fun toggleIndoorMode(workoutId: Long, isIndoor: Boolean) {
        // 1. Basculer le mode Indoor/Outdoor
        dailyWorkoutDao.toggleIndoorMode(workoutId, isIndoor)

        // 2. Récupérer la séance pour connaître le sport
        val workout = dailyWorkoutDao.getWorkoutByIdSync(workoutId) ?: return

        // 3. Recalculer le TargetType selon le sport, le mode et l'équipement
        val newTargetType = calculateTargetType(workout.sport, isIndoor)

        // 4. Mettre à jour le TargetType
        dailyWorkoutDao.updateTargetType(workoutId, newTargetType)
    }

    /**
     * Calcule le TargetType approprié selon le sport, le mode et l'équipement
     *
     * @param sport Sport de la séance (RUNNING/CYCLING)
     * @param isIndoor Mode indoor activé
     * @return TargetType adapté
     */
    private suspend fun calculateTargetType(sport: Sport, isIndoor: Boolean): TargetType {
        return when (sport) {
            Sport.RUNNING -> {
                if (isIndoor) TargetType.SPEED else TargetType.PACE
            }
            Sport.CYCLING -> {
                val hasPowerMeter = profileRepository.hasPowerMeter()
                if (hasPowerMeter) TargetType.POWER else TargetType.HEART_RATE
            }
        }
    }

    /**
     * Supprime une séance
     *
     * @param workoutId ID de la séance à supprimer
     */
    suspend fun deleteWorkout(workoutId: Long) {
        dailyWorkoutDao.deleteWorkout(workoutId)
    }

    // ========== SessionFeedback ==========

    /**
     * Récupère le feedback d'une séance
     *
     * @param workoutId ID de la séance
     */
    fun getFeedbackByWorkout(workoutId: Long): Flow<SessionFeedbackEntity?> {
        return sessionFeedbackDao.getFeedbackByWorkout(workoutId)
    }

    /**
     * Récupère les 7 derniers feedbacks
     * (Utilisé par l'IA pour détecter la fatigue)
     */
    fun getRecentFeedbacks(): Flow<List<SessionFeedbackEntity>> {
        return sessionFeedbackDao.getRecentFeedbacks()
    }

    /**
     * Crée un feedback post-séance
     *
     * @param workoutId ID de la séance concernée
     * @param rpe Rating of Perceived Exertion (1-10)
     * @param comment Commentaire libre (nullable)
     * @param tooEasy La séance était trop facile ?
     * @param tooHard La séance était trop difficile ?
     *
     * @return L'ID du feedback créé
     *
     * Core Entity (context.json) : "SessionFeedback (RPE)"
     */
    suspend fun submitFeedback(
        workoutId: Long,
        rpe: Int,
        comment: String? = null,
        tooEasy: Boolean = false,
        tooHard: Boolean = false
    ): Long {
        val feedback = SessionFeedbackEntity(
            workoutId = workoutId,
            rpe = rpe,
            comment = comment,
            tooEasy = tooEasy,
            tooHard = tooHard
        )
        return sessionFeedbackDao.insertFeedback(feedback)
    }

    /**
     * Calcule le RPE moyen des N dernières séances
     *
     * @param limit Nombre de séances à considérer (défaut: 7)
     * @return Moyenne du RPE (1.0 à 10.0) ou null si pas de données
     *
     * Utilisé par l'IA pour :
     * - Détecter la fatigue accumulée
     * - Adapter l'intensité des prochaines séances
     */
    suspend fun getAverageRPE(limit: Int = 7): Double? {
        return sessionFeedbackDao.getAverageRPE(limit)
    }

    /**
     * Détecte si l'athlète trouve les séances trop difficiles
     *
     * @param limit Nombre de séances à analyser (défaut: 7)
     * @return true si >= 3 séances jugées "trop difficiles"
     *
     * Si true, l'IA devrait réduire l'intensité
     */
    suspend fun isFatigued(limit: Int = 7): Boolean {
        val tooHardCount = sessionFeedbackDao.countTooHardRecent(limit)
        return tooHardCount >= 3
    }

    /**
     * Détecte si l'athlète trouve les séances trop faciles
     *
     * @param limit Nombre de séances à analyser (défaut: 7)
     * @return true si >= 3 séances jugées "trop faciles"
     *
     * Si true, l'IA devrait augmenter l'intensité
     */
    suspend fun needsProgression(limit: Int = 7): Boolean {
        val tooEasyCount = sessionFeedbackDao.countTooEasyRecent(limit)
        return tooEasyCount >= 3
    }
}
