package com.enduranceflow.workout.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entité Room : Feedback post-séance
 *
 * Table : session_feedback
 *
 * Collecte le ressenti de l'athlète après chaque séance.
 * Permet à l'IA d'adapter les séances futures selon la fatigue/forme.
 *
 * Core Entity (context.json) :
 * "SessionFeedback (RPE)"
 *
 * RPE = Rating of Perceived Exertion (Échelle de Borg)
 * Échelle 1-10 :
 * 1-2 = Très facile
 * 3-4 = Facile
 * 5-6 = Modéré
 * 7-8 = Difficile
 * 9-10 = Très difficile / Maximal
 */
@Entity(
    tableName = "session_feedback",
    foreignKeys = [
        ForeignKey(
            entity = DailyWorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SessionFeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Référence vers la séance concernée
     */
    val workoutId: Long,

    /**
     * RPE (Rating of Perceived Exertion)
     * Échelle 1-10
     *
     * Utilisé par l'IA pour :
     * - Détecter la fatigue accumulée
     * - Adapter l'intensité des prochaines séances
     * - Éviter le surentraînement
     */
    val rpe: Int,

    /**
     * Commentaire libre de l'athlète
     * Exemple : "Très difficile, jambes lourdes", "Super séance !"
     *
     * Peut être analysé par l'IA pour affiner les recommandations
     */
    val comment: String?,

    /**
     * La séance était-elle trop facile ?
     * Signal pour l'IA d'augmenter l'intensité
     */
    val tooEasy: Boolean = false,

    /**
     * La séance était-elle trop difficile ?
     * Signal pour l'IA de réduire l'intensité
     */
    val tooHard: Boolean = false,

    /**
     * Timestamp de soumission du feedback
     */
    val submittedAt: Long = System.currentTimeMillis()
)
