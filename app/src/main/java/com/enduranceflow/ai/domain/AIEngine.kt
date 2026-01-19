package com.enduranceflow.ai.domain

import com.enduranceflow.core.domain.model.Gender
import com.enduranceflow.workout.data.entity.DailyWorkoutEntity

/**
 * Interface AIEngine - Strategy Pattern
 *
 * Contrat pour les moteurs d'IA (Mistral AI)
 *
 * Simplifié : Utilise uniquement Mistral AI comme moteur cloud
 *
 * Responsabilités :
 * - Générer des séances d'entraînement personnalisées
 * - Adapter le ton selon le genre de l'athlète
 * - Analyser les feedbacks pour ajuster l'intensité
 * - Fournir des encouragements contextuels
 *
 * Implémentation actuelle :
 * - MistralEngine : IA cloud française 🇫🇷
 */
interface AIEngine {

    /**
     * Type de moteur IA
     */
    enum class EngineType {
        GEMINI_FLASH    // Réutilisé pour cloud AI (Mistral)
    }

    /**
     * Retourne le type de moteur actif
     */
    fun getEngineType(): EngineType

    /**
     * Vérifie si le moteur est disponible
     *
     * @return true si le moteur peut être utilisé
     *
     * Mistral AI : Toujours disponible (nécessite Internet et clé API)
     */
    suspend fun isAvailable(): Boolean

    /**
     * Initialise le moteur IA
     *
     * @return true si l'initialisation a réussi
     *
     * Mistral AI : Valide la clé API
     */
    suspend fun initialize(): Boolean

    /**
     * Génère des séances d'entraînement pour une semaine
     *
     * @param vma VMA de l'athlète en km/h
     * @param ftp FTP de l'athlète en Watts
     * @param gender Genre de l'athlète (influence le ton)
     * @param hasPowerMeter Capteur de puissance disponible ?
     * @param availableDays Jours de disponibilité dans la semaine
     * @param availableSports Sports pratiqués par l'utilisateur (RUNNING, CYCLING)
     * @param averageRPE RPE moyen récent (indicateur de fatigue)
     * @param isFatigued L'athlète est fatigué ? (>= 3 séances "difficiles")
     * @param needsProgression L'athlète trouve ça trop facile ?
     *
     * @return Liste de séances générées (DailyWorkoutEntity)
     *
     * User Story (context.json) :
     * "L'IA adapte son ton : Empathique/Bienveillant si je suis une femme,
     *  Analytique/Factuel si je suis un homme."
     *
     * Avoid List (context.json) :
     * "Ne jamais envoyer d'identifiants personnels (Nom, Email) à l'IA Cloud."
     */
    suspend fun generateWeeklyWorkouts(
        vma: Double?,
        ftp: Int?,
        gender: Gender,
        hasPowerMeter: Boolean,
        availableDays: List<String>, // Format: "MONDAY", "WEDNESDAY", etc.
        availableSports: List<String>, // Format: "RUNNING", "CYCLING"
        averageRPE: Double?,
        isFatigued: Boolean,
        needsProgression: Boolean
    ): List<DailyWorkoutEntity>

    /**
     * Génère un message d'encouragement personnalisé
     *
     * @param gender Genre de l'athlète (influence le ton)
     * @param workoutTitle Titre de la séance
     * @param isBeforeWorkout true = avant, false = après
     *
     * @return Message d'encouragement adapté
     *
     * Exemples :
     * - Femme avant : "Tu vas y arriver ! Cette séance est parfaite pour toi 💪"
     * - Homme avant : "Objectif : 8x400m à 100% VMA. Sois régulier."
     * - Femme après : "Bravo ! Tu as donné le meilleur de toi-même aujourd'hui ❤️"
     * - Homme après : "Séance validée. Performance conforme aux attentes."
     */
    suspend fun generateMotivationalMessage(
        gender: Gender,
        workoutTitle: String,
        isBeforeWorkout: Boolean
    ): String

    /**
     * Analyse les feedbacks récents et recommande des ajustements
     *
     * @param recentRPE Liste des RPE récents (1-10)
     * @param tooHardCount Nombre de séances "trop difficiles"
     * @param tooEasyCount Nombre de séances "trop faciles"
     * @param gender Genre de l'athlète (influence le ton)
     *
     * @return Recommandation textuelle
     *
     * Exemples :
     * - Fatigué (Femme) : "Je vois que les dernières séances ont été difficiles.
     *   Prenons le temps de récupérer ensemble cette semaine. 🌸"
     * - Fatigué (Homme) : "RPE moyen : 8.2/10. Réduction de 15% de l'intensité recommandée."
     * - Trop facile (Femme) : "Tu es en super forme ! Prête pour un nouveau défi ? 💪"
     * - Trop facile (Homme) : "Performance stable. Progression de 5% proposée."
     */
    suspend fun analyzeFeedbackAndRecommend(
        recentRPE: List<Int>,
        tooHardCount: Int,
        tooEasyCount: Int,
        gender: Gender
    ): String
}
