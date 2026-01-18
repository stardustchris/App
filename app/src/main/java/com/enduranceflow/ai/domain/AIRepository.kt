package com.enduranceflow.ai.domain

import com.enduranceflow.core.domain.repository.AvailabilityRepository
import com.enduranceflow.profile.domain.repository.ProfileRepository
import com.enduranceflow.workout.data.entity.DailyWorkoutEntity
import com.enduranceflow.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les interactions avec l'IA
 *
 * Responsabilités :
 * - Orchestrer l'utilisation de l'AIEngine (Nano ou Flash)
 * - Combiner les données des autres repositories pour l'IA
 * - Gérer les erreurs et fallbacks
 * - Fournir une API simple aux ViewModels
 *
 * Architecture :
 * AIRepository → AIEngineFactory → (GeminiNanoEngine | GeminiFlashEngine)
 *
 * Injecté par Hilt dans les ViewModels
 */
@Singleton
class AIRepository @Inject constructor(
    private val aiEngineFactory: AIEngineFactory,
    private val profileRepository: ProfileRepository,
    private val workoutRepository: WorkoutRepository,
    private val availabilityRepository: AvailabilityRepository
) {

    /**
     * Génère un programme de séances pour la semaine
     *
     * Algorithme :
     * 1. Récupère le profil athlète (VMA, FTP, Genre)
     * 2. Récupère la configuration équipement (capteur de puissance)
     * 3. Récupère les créneaux de disponibilité
     * 4. Analyse l'état de fatigue (feedbacks récents)
     * 5. Appelle l'IA pour générer les séances
     * 6. Enregistre les séances en base de données
     *
     * @return Liste des séances générées, ou liste vide si erreur
     *
     * User Stories (context.json) :
     * - "Au démarrage, si je ne connais pas mon niveau (VMA/FTP),
     *    l'app génère une 'Semaine de Calibration' avec des tests."
     * - "L'IA adapte son ton : Empathique/Bienveillant si je suis une femme,
     *    Analytique/Factuel si je suis un homme."
     */
    suspend fun generateWeeklyWorkouts(): List<DailyWorkoutEntity> {
        return try {
            // 1. Récupérer le profil athlète
            val profile = profileRepository.getProfile().first()
                ?: return emptyList() // Profil non créé

            // 2. Vérifier si calibration complète
            if (profile.vma == null || profile.ftp == null) {
                // TODO: Générer semaine de calibration (tests VMA/FTP)
                return emptyList()
            }

            // 3. Récupérer la configuration équipement
            val equipment = profileRepository.getEquipmentConfig().first()
            val hasPowerMeter = equipment?.hasPowerMeter ?: false

            // 4. Récupérer les créneaux de disponibilité
            val availabilitySlots = availabilityRepository.getAllActiveSlots().first()
            val availableDays = if (availabilitySlots.isEmpty()) {
                // Disponibilités par défaut si non configurées : Lundi, Mercredi, Vendredi
                listOf("MONDAY", "WEDNESDAY", "FRIDAY")
            } else {
                availabilitySlots.map { it.dayOfWeek.name }
            }

            // 5. Analyser l'état de fatigue
            val averageRPE = workoutRepository.getAverageRPE(limit = 7)
            val isFatigued = workoutRepository.isFatigued(limit = 7)
            val needsProgression = workoutRepository.needsProgression(limit = 7)

            // 6. Obtenir le moteur IA (Nano ou Flash selon disponibilité)
            val aiEngine = aiEngineFactory.getEngine()

            // 7. Générer les séances via l'IA
            val generatedWorkouts = aiEngine.generateWeeklyWorkouts(
                vma = profile.vma,
                ftp = profile.ftp,
                gender = profile.gender,
                hasPowerMeter = hasPowerMeter,
                availableDays = availableDays,
                averageRPE = averageRPE,
                isFatigued = isFatigued,
                needsProgression = needsProgression
            )

            // 8. Enregistrer les séances en base de données
            if (generatedWorkouts.isNotEmpty()) {
                workoutRepository.createWorkouts(generatedWorkouts)
            }

            generatedWorkouts

        } catch (e: Exception) {
            // Erreur IA → Retourner liste vide (ou séances par défaut)
            emptyList()
        }
    }

    /**
     * Génère un message de motivation personnalisé
     *
     * @param workoutTitle Titre de la séance
     * @param isBeforeWorkout true = avant la séance, false = après
     *
     * @return Message d'encouragement adapté au genre de l'athlète
     *
     * User Story (context.json) :
     * "L'IA adapte son ton : Empathique/Bienveillant si je suis une femme,
     *  Analytique/Factuel si je suis un homme."
     *
     * Exemples :
     * - Femme avant : "Tu vas y arriver ! Cette séance est parfaite pour toi 💪"
     * - Homme avant : "Objectif : VMA courte. Concentre-toi sur la régularité."
     * - Femme après : "Bravo ! Tu as donné le meilleur de toi-même ❤️"
     * - Homme après : "Séance validée. Performance conforme."
     */
    suspend fun generateMotivationalMessage(
        workoutTitle: String,
        isBeforeWorkout: Boolean
    ): String {
        return try {
            // Récupérer le genre de l'athlète
            val profile = profileRepository.getProfile().first()
            val gender = profile?.gender ?: return "Bonne séance !"

            // Obtenir le moteur IA
            val aiEngine = aiEngineFactory.getEngine()

            // Générer le message
            aiEngine.generateMotivationalMessage(
                gender = gender,
                workoutTitle = workoutTitle,
                isBeforeWorkout = isBeforeWorkout
            )

        } catch (e: Exception) {
            // Fallback message si erreur
            if (isBeforeWorkout) "Bonne séance !" else "Bravo !"
        }
    }

    /**
     * Analyse les feedbacks récents et recommande des ajustements
     *
     * @return Recommandation textuelle personnalisée
     *
     * L'IA analyse :
     * - RPE moyen (indicateur de fatigue)
     * - Nombre de séances "trop difficiles"
     * - Nombre de séances "trop faciles"
     * - Genre (pour adapter le ton)
     *
     * Recommandations possibles :
     * - Réduction de l'intensité si fatigué
     * - Augmentation si trop facile
     * - Maintien si équilibré
     */
    suspend fun analyzeFeedbackAndRecommend(): String {
        return try {
            // Récupérer les feedbacks récents
            val recentFeedbacks = workoutRepository.getRecentFeedbacks().first()

            if (recentFeedbacks.isEmpty()) {
                return "Pas encore assez de données pour une analyse. Continue tes séances !"
            }

            val recentRPE = recentFeedbacks.map { it.rpe }
            val tooHardCount = recentFeedbacks.count { it.tooHard }
            val tooEasyCount = recentFeedbacks.count { it.tooEasy }

            // Récupérer le genre
            val profile = profileRepository.getProfile().first()
            val gender = profile?.gender ?: return "Continue comme ça !"

            // Obtenir le moteur IA
            val aiEngine = aiEngineFactory.getEngine()

            // Analyser et recommander
            aiEngine.analyzeFeedbackAndRecommend(
                recentRPE = recentRPE,
                tooHardCount = tooHardCount,
                tooEasyCount = tooEasyCount,
                gender = gender
            )

        } catch (e: Exception) {
            "Programme adapté. Poursuis tes entraînements !"
        }
    }

    /**
     * Retourne des informations sur le moteur IA actif
     *
     * @return Map avec les détails (type, local/cloud, etc.)
     *
     * Utile pour afficher dans l'écran Profil :
     * "Mode IA : Gemini Nano (Local)" ou "Mode IA : Gemini Flash (Cloud)"
     */
    suspend fun getAIEngineInfo(): Map<String, Any> {
        return aiEngineFactory.getEngineInfo()
    }

    /**
     * Vérifie si l'IA locale (Gemini Nano) est active
     *
     * @return true si IA locale, false si cloud
     */
    fun isUsingLocalAI(): Boolean {
        return aiEngineFactory.isUsingLocalAI()
    }

    /**
     * Force le basculement vers l'IA cloud (Gemini Flash)
     *
     * Cas d'usage :
     * - L'utilisateur préfère le mode cloud (modèle plus puissant)
     * - Debug/test
     *
     * @return true si basculement réussi
     */
    suspend fun forceCloudAI(): Boolean {
        return aiEngineFactory.forceFlashEngine()
    }

    /**
     * Réinitialise le moteur IA
     *
     * Redétecte automatiquement le moteur approprié (Nano → Flash)
     */
    suspend fun resetAIEngine() {
        aiEngineFactory.resetEngine()
    }
}
