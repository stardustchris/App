package com.enduranceflow.workout.data.dao

import androidx.room.*
import com.enduranceflow.core.domain.model.TargetType
import com.enduranceflow.workout.data.entity.DailyWorkoutEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) pour DailyWorkout
 *
 * Gère les séances d'entraînement quotidiennes générées par l'IA
 *
 * Fonctionnalités principales :
 * - Récupérer les séances de la semaine
 * - Marquer une séance comme complétée
 * - Basculer entre mode Indoor/Outdoor
 * - Supprimer les séances anciennes
 */
@Dao
interface DailyWorkoutDao {

    /**
     * Récupère toutes les séances futures (non complétées)
     * Triées par date croissante
     *
     * @return Flow<List<DailyWorkoutEntity>>
     *
     * Flow = Observable : l'UI se met à jour automatiquement
     * quand de nouvelles séances sont générées
     */
    @Query("SELECT * FROM daily_workout WHERE isCompleted = 0 ORDER BY date ASC")
    fun getUpcomingWorkouts(): Flow<List<DailyWorkoutEntity>>

    /**
     * Récupère toutes les séances d'une date spécifique
     *
     * @param date Date au format YYYY-MM-DD (ex: "2026-01-20")
     */
    @Query("SELECT * FROM daily_workout WHERE date = :date ORDER BY createdAt ASC")
    fun getWorkoutsByDate(date: String): Flow<List<DailyWorkoutEntity>>

    /**
     * Récupère les séances de la semaine en cours
     *
     * @param startDate Date de début de semaine (YYYY-MM-DD)
     * @param endDate Date de fin de semaine (YYYY-MM-DD)
     */
    @Query("SELECT * FROM daily_workout WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWorkoutsForWeek(startDate: String, endDate: String): Flow<List<DailyWorkoutEntity>>

    /**
     * Récupère une séance par son ID
     *
     * @param workoutId ID de la séance
     */
    @Query("SELECT * FROM daily_workout WHERE id = :workoutId")
    fun getWorkoutById(workoutId: Long): Flow<DailyWorkoutEntity?>

    /**
     * Insère une nouvelle séance
     *
     * @return L'ID de la séance créée
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: DailyWorkoutEntity): Long

    /**
     * Insère plusieurs séances en une fois
     * (Utilisé lors de la génération de la semaine par l'IA)
     *
     * @param workouts Liste des séances à insérer
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<DailyWorkoutEntity>)

    /**
     * Marque une séance comme complétée
     *
     * @param workoutId ID de la séance
     *
     * Déclenche l'affichage du formulaire de feedback (RPE)
     */
    @Query("UPDATE daily_workout SET isCompleted = 1 WHERE id = :workoutId")
    suspend fun markAsCompleted(workoutId: Long)

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
     * Note : Ce changement devrait aussi recalculer le TargetType
     * (géré par le Repository/UseCase)
     */
    @Query("UPDATE daily_workout SET isIndoor = :isIndoor WHERE id = :workoutId")
    suspend fun toggleIndoorMode(workoutId: Long, isIndoor: Boolean)

    /**
     * Met à jour le TargetType d'une séance
     *
     * @param workoutId ID de la séance
     * @param targetType Nouveau type de cible
     *
     * Utilisé lors du basculement Indoor/Outdoor pour adapter
     * le type de cible selon le sport et l'équipement
     */
    @Query("UPDATE daily_workout SET targetType = :targetType WHERE id = :workoutId")
    suspend fun updateTargetType(workoutId: Long, targetType: TargetType)

    /**
     * Récupère une séance de manière synchrone (non-Flow)
     *
     * @param workoutId ID de la séance
     * @return DailyWorkoutEntity? - null si non trouvée
     */
    @Query("SELECT * FROM daily_workout WHERE id = :workoutId")
    suspend fun getWorkoutByIdSync(workoutId: Long): DailyWorkoutEntity?

    /**
     * Supprime les séances anciennes (plus de 30 jours)
     * Évite l'accumulation de données inutiles
     *
     * @param cutoffDate Date limite (YYYY-MM-DD)
     */
    @Query("DELETE FROM daily_workout WHERE date < :cutoffDate AND isCompleted = 1")
    suspend fun deleteOldWorkouts(cutoffDate: String)

    /**
     * Supprime une séance spécifique
     *
     * @param workoutId ID de la séance à supprimer
     */
    @Query("DELETE FROM daily_workout WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Long)

    /**
     * Supprime toutes les séances (pour debug/reset)
     */
    @Query("DELETE FROM daily_workout")
    suspend fun deleteAllWorkouts()

    /**
     * Compte le nombre de séances complétées
     * (Pour statistiques)
     *
     * @return Nombre de séances terminées
     */
    @Query("SELECT COUNT(*) FROM daily_workout WHERE isCompleted = 1")
    suspend fun getCompletedWorkoutsCount(): Int
}
