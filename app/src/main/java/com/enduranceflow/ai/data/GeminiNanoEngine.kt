package com.enduranceflow.ai.data

import android.content.Context
import com.enduranceflow.ai.domain.AIEngine
import com.enduranceflow.core.domain.model.Gender
import com.enduranceflow.core.domain.model.Sport
import com.enduranceflow.core.domain.model.TargetType
import com.enduranceflow.workout.data.entity.DailyWorkoutEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Implémentation Gemini Nano - IA locale (on-device)
 *
 * Tech Stack Rule (context.json) :
 * "Priority = Gemini Nano (On-Device)"
 *
 * Avantages :
 * - Fonctionne sans connexion Internet
 * - Latence très faible (< 100ms)
 * - Privacy absolue (aucune donnée envoyée)
 * - Gratuit (pas de coût API)
 *
 * Inconvénients :
 * - Disponible uniquement sur certains appareils récents
 * - Capacités limitées vs modèle cloud
 *
 * User Story (context.json) :
 * "Si mon téléphone ne supporte pas l'IA locale,
 *  l'app utilise une connexion sécurisée vers une IA Cloud (Fallback)."
 *
 * TODO: Intégrer l'API Gemini Nano quand disponible publiquement
 * Documentation : https://ai.google.dev/gemini-api/docs/
 */
class GeminiNanoEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : AIEngine {

    // Instance du modèle Gemini Nano (quand API disponible)
    // private var nanoModel: GeminiNanoModel? = null

    override fun getEngineType(): AIEngine.EngineType {
        return AIEngine.EngineType.GEMINI_NANO
    }

    /**
     * Vérifie la disponibilité de Gemini Nano sur cet appareil
     *
     * Conditions requises :
     * - Android 14+ (API Level 34+)
     * - Processeur compatible (Tensor, Snapdragon 8 Gen 3+)
     * - Minimum 6GB RAM
     * - Modèle Gemini Nano téléchargé (auto ou manuel)
     *
     * @return true si Gemini Nano est disponible
     */
    override suspend fun isAvailable(): Boolean {
        // TODO: Implémenter la détection réelle via l'API Gemini Nano
        // Exemple pseudo-code :
        // return GeminiNanoClient.isSupported(context) &&
        //        GeminiNanoClient.isModelDownloaded()

        // Pour l'instant, retourne false (fallback vers Gemini Flash)
        return false
    }

    /**
     * Initialise le moteur Gemini Nano
     *
     * Actions :
     * 1. Vérifier la disponibilité du modèle
     * 2. Télécharger le modèle si nécessaire (WiFi uniquement)
     * 3. Charger le modèle en mémoire
     *
     * @return true si l'initialisation a réussi
     */
    override suspend fun initialize(): Boolean {
        // TODO: Implémenter l'initialisation via l'API Gemini Nano
        // Exemple pseudo-code :
        // try {
        //     if (!isAvailable()) return false
        //
        //     nanoModel = GeminiNanoModel.Builder(context)
        //         .setModelType(ModelType.TEXT_GENERATION)
        //         .setMaxTokens(2048)
        //         .build()
        //
        //     return nanoModel != null
        // } catch (e: Exception) {
        //     return false
        // }

        return false
    }

    override suspend fun generateWeeklyWorkouts(
        vma: Double?,
        ftp: Int?,
        gender: Gender,
        hasPowerMeter: Boolean,
        availableDays: List<String>,
        averageRPE: Double?,
        isFatigued: Boolean,
        needsProgression: Boolean
    ): List<DailyWorkoutEntity> {
        // TODO: Implémenter la génération via Gemini Nano

        // Construction du prompt
        val prompt = buildWorkoutGenerationPrompt(
            vma, ftp, gender, hasPowerMeter,
            availableDays, averageRPE, isFatigued, needsProgression
        )

        // TODO: Appeler Gemini Nano
        // val response = nanoModel?.generateText(prompt)
        // val workouts = parseWorkoutsFromResponse(response)

        // Pour l'instant, retourne une liste vide (fallback)
        return emptyList()
    }

    override suspend fun generateMotivationalMessage(
        gender: Gender,
        workoutTitle: String,
        isBeforeWorkout: Boolean
    ): String {
        // TODO: Implémenter via Gemini Nano

        val tone = if (gender == Gender.FEMALE) {
            "empathique et bienveillant"
        } else {
            "analytique et factuel"
        }

        val prompt = """
            Tu es un coach d'endurance avec un ton $tone.
            ${if (isBeforeWorkout) "Motive" else "Félicite"} l'athlète pour la séance : "$workoutTitle".
            Réponse en 1-2 phrases maximum.
        """.trimIndent()

        // TODO: Appeler Gemini Nano
        // return nanoModel?.generateText(prompt) ?: getDefaultMessage(gender, isBeforeWorkout)

        return getDefaultMessage(gender, isBeforeWorkout, workoutTitle)
    }

    override suspend fun analyzeFeedbackAndRecommend(
        recentRPE: List<Int>,
        tooHardCount: Int,
        tooEasyCount: Int,
        gender: Gender
    ): String {
        // TODO: Implémenter via Gemini Nano

        val averageRPE = if (recentRPE.isNotEmpty()) {
            recentRPE.average()
        } else {
            null
        }

        val tone = if (gender == Gender.FEMALE) {
            "empathique et bienveillant"
        } else {
            "analytique et factuel"
        }

        val prompt = """
            Tu es un coach d'endurance avec un ton $tone.
            Analyse ces données :
            - RPE moyen : ${averageRPE?.let { "%.1f/10".format(it) } ?: "Non disponible"}
            - Séances trop difficiles : $tooHardCount
            - Séances trop faciles : $tooEasyCount

            Donne une recommandation en 2-3 phrases.
        """.trimIndent()

        // TODO: Appeler Gemini Nano
        // return nanoModel?.generateText(prompt) ?: getDefaultRecommendation(...)

        return getDefaultRecommendation(averageRPE, tooHardCount, tooEasyCount, gender)
    }

    // ========== Helpers ==========

    /**
     * Construit le prompt pour la génération de séances
     */
    private fun buildWorkoutGenerationPrompt(
        vma: Double?,
        ftp: Int?,
        gender: Gender,
        hasPowerMeter: Boolean,
        availableDays: List<String>,
        averageRPE: Double?,
        isFatigued: Boolean,
        needsProgression: Boolean
    ): String {
        val tone = if (gender == Gender.FEMALE) {
            "empathique et bienveillant"
        } else {
            "analytique et factuel"
        }

        return """
            Tu es un coach d'endurance expert avec un ton $tone.

            Profil athlète :
            - VMA : ${vma?.let { "%.1f km/h".format(it) } ?: "Non calibré"}
            - FTP : ${ftp?.let { "$it W" } ?: "Non calibré"}
            - Capteur de puissance : ${if (hasPowerMeter) "Oui" else "Non"}

            État de forme :
            - RPE moyen récent : ${averageRPE?.let { "%.1f/10".format(it) } ?: "Pas de données"}
            - Fatigue détectée : ${if (isFatigued) "Oui" else "Non"}
            - Besoin de progression : ${if (needsProgression) "Oui" else "Non"}

            Disponibilités : ${availableDays.joinToString(", ")}

            Génère un programme d'entraînement pour cette semaine.
            Pour chaque séance, fournis :
            - Jour
            - Sport (Course/Vélo)
            - Titre
            - Description détaillée
            - Durée en minutes
            - Type de cible (PACE/SPEED/POWER/HEART_RATE)
            - Valeur cible
        """.trimIndent()
    }

    /**
     * Messages par défaut si IA indisponible
     */
    private fun getDefaultMessage(gender: Gender, isBeforeWorkout: Boolean, workoutTitle: String): String {
        return when {
            isBeforeWorkout && gender == Gender.FEMALE ->
                "Tu vas y arriver ! Cette séance est parfaite pour toi 💪"
            isBeforeWorkout && gender == Gender.MALE ->
                "Objectif : $workoutTitle. Concentre-toi sur la régularité."
            !isBeforeWorkout && gender == Gender.FEMALE ->
                "Bravo ! Tu as donné le meilleur de toi-même aujourd'hui ❤️"
            else ->
                "Séance validée. Performance conforme aux attentes."
        }
    }

    /**
     * Recommandations par défaut si IA indisponible
     */
    private fun getDefaultRecommendation(
        averageRPE: Double?,
        tooHardCount: Int,
        tooEasyCount: Int,
        gender: Gender
    ): String {
        return when {
            tooHardCount >= 3 && gender == Gender.FEMALE ->
                "Je vois que les dernières séances ont été difficiles. Prenons le temps de récupérer ensemble cette semaine. 🌸"
            tooHardCount >= 3 && gender == Gender.MALE ->
                "RPE moyen : ${averageRPE?.let { "%.1f/10".format(it) } ?: "N/A"}. Réduction de 15% de l'intensité recommandée."
            tooEasyCount >= 3 && gender == Gender.FEMALE ->
                "Tu es en super forme ! Prête pour un nouveau défi ? 💪"
            tooEasyCount >= 3 && gender == Gender.MALE ->
                "Performance stable. Progression de 5% proposée."
            else && gender == Gender.FEMALE ->
                "Continue comme ça, tu progresses bien ! 🌟"
            else ->
                "Programme actuel adapté. Poursuis."
        }
    }
}
