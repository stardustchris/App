package com.enduranceflow.workout.data.dao

import androidx.room.*
import com.enduranceflow.workout.data.entity.SessionFeedbackEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) pour SessionFeedback
 *
 * Gère les feedbacks post-séance (RPE, ressenti)
 *
 * L'IA utilise ces feedbacks pour :
 * - Détecter la fatigue accumulée
 * - Adapter l'intensité des prochaines séances
 * - Personnaliser le ton des encouragements
 */
@Dao
interface SessionFeedbackDao {

    /**
     * Récupère le feedback d'une séance spécifique
     *
     * @param workoutId ID de la séance
     * @return Flow<SessionFeedbackEntity?> - null si pas encore de feedback
     */
    @Query("SELECT * FROM session_feedback WHERE workoutId = :workoutId")
    fun getFeedbackByWorkout(workoutId: Long): Flow<SessionFeedbackEntity?>

    /**
     * Récupère les 7 derniers feedbacks
     * (Utilisé par l'IA pour détecter la tendance de fatigue)
     *
     * @return Flow<List<SessionFeedbackEntity>>
     */
    @Query("SELECT * FROM session_feedback ORDER BY submittedAt DESC LIMIT 7")
    fun getRecentFeedbacks(): Flow<List<SessionFeedbackEntity>>

    /**
     * Calcule le RPE moyen des N dernières séances
     * (Indicateur de fatigue)
     *
     * @param limit Nombre de séances à considérer
     * @return Moyenne du RPE (1.0 à 10.0)
     */
    @Query("SELECT AVG(rpe) FROM session_feedback ORDER BY submittedAt DESC LIMIT :limit")
    suspend fun getAverageRPE(limit: Int = 7): Double?

    /**
     * Récupère tous les feedbacks
     * Triés par date de soumission (plus récent en premier)
     *
     * @return Flow<List<SessionFeedbackEntity>>
     */
    @Query("SELECT * FROM session_feedback ORDER BY submittedAt DESC")
    fun getAllFeedbacks(): Flow<List<SessionFeedbackEntity>>

    /**
     * Insère un nouveau feedback
     *
     * @param feedback Feedback à insérer
     * @return L'ID du feedback créé
     *
     * Appelé juste après qu'une séance soit marquée "complétée"
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: SessionFeedbackEntity): Long

    /**
     * Met à jour un feedback existant
     *
     * @param feedback Feedback mis à jour
     *
     * Permet à l'utilisateur de modifier son ressenti
     */
    @Update
    suspend fun updateFeedback(feedback: SessionFeedbackEntity)

    /**
     * Supprime un feedback
     *
     * @param feedbackId ID du feedback à supprimer
     */
    @Query("DELETE FROM session_feedback WHERE id = :feedbackId")
    suspend fun deleteFeedback(feedbackId: Long)

    /**
     * Supprime tous les feedbacks (pour reset)
     */
    @Query("DELETE FROM session_feedback")
    suspend fun deleteAllFeedbacks()

    /**
     * Compte le nombre de séances jugées "trop difficiles"
     * dans les N dernières séances
     *
     * @param limit Nombre de séances à considérer
     * @return Nombre de séances avec tooHard = true
     *
     * Si > 3, l'IA devrait réduire l'intensité
     */
    @Query("SELECT COUNT(*) FROM session_feedback WHERE tooHard = 1 ORDER BY submittedAt DESC LIMIT :limit")
    suspend fun countTooHardRecent(limit: Int = 7): Int

    /**
     * Compte le nombre de séances jugées "trop faciles"
     * dans les N dernières séances
     *
     * @param limit Nombre de séances à considérer
     * @return Nombre de séances avec tooEasy = true
     *
     * Si > 3, l'IA devrait augmenter l'intensité
     */
    @Query("SELECT COUNT(*) FROM session_feedback WHERE tooEasy = 1 ORDER BY submittedAt DESC LIMIT :limit")
    suspend fun countTooEasyRecent(limit: Int = 7): Int
}
