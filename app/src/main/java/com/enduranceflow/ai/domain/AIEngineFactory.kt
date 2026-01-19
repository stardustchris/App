package com.enduranceflow.ai.domain

import com.enduranceflow.ai.data.MistralEngine
import com.enduranceflow.ai.data.GeminiNanoEngine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory pour créer le moteur IA approprié (Strategy Pattern)
 *
 * Tech Stack Rule (context.json) :
 * "Strategy Pattern: Priority = Gemini Nano (On-Device).
 *  Fallback = Mistral AI (Cloud)"
 *
 * Responsabilités :
 * 1. Détecter la disponibilité de Gemini Nano sur l'appareil
 * 2. Choisir automatiquement le moteur approprié :
 *    - Gemini Nano si disponible (préféré)
 *    - Mistral AI sinon (fallback)
 * 3. Initialiser le moteur sélectionné
 * 4. Gérer le basculement automatique si échec
 *
 * User Story (context.json) :
 * "Si mon téléphone ne supporte pas l'IA locale,
 *  l'app utilise une connexion sécurisée vers une IA Cloud (Fallback)."
 *
 * @Singleton : Une seule instance pour toute l'app (économie mémoire/perf)
 */
@Singleton
class AIEngineFactory @Inject constructor(
    private val geminiNanoEngine: GeminiNanoEngine,
    private val mistralEngine: MistralEngine
) {

    // Moteur IA actuellement actif
    private var activeEngine: AIEngine? = null

    /**
     * Obtient le moteur IA actif (ou le crée si nécessaire)
     *
     * Stratégie de sélection :
     * 1. Si Gemini Nano disponible → Utiliser Nano
     * 2. Sinon → Fallback sur Gemini Flash
     *
     * @return Instance du moteur IA actif
     */
    suspend fun getEngine(): AIEngine {
        // Si un moteur est déjà initialisé, le retourner
        activeEngine?.let { return it }

        // Sinon, sélectionner et initialiser le moteur approprié
        return selectAndInitializeEngine()
    }

    /**
     * Sélectionne et initialise le moteur IA approprié
     *
     * Algorithme :
     * 1. Tenter Gemini Nano (priority)
     *    - Vérifier disponibilité
     *    - Initialiser
     *    - Si succès → Utiliser Nano
     * 2. Sinon, utiliser Mistral AI (fallback)
     *    - Initialiser
     *    - Toujours disponible si Internet actif
     *
     * @return Instance du moteur IA initialisé
     */
    private suspend fun selectAndInitializeEngine(): AIEngine {
        // PRIORITY : Tentative Gemini Nano (on-device)
        if (geminiNanoEngine.isAvailable()) {
            val initialized = geminiNanoEngine.initialize()
            if (initialized) {
                activeEngine = geminiNanoEngine
                return geminiNanoEngine
            }
        }

        // FALLBACK : Mistral AI (cloud)
        mistralEngine.initialize()
        activeEngine = mistralEngine
        return mistralEngine
    }

    /**
     * Force le basculement vers Mistral AI (utile pour debug/tests)
     *
     * @return true si basculement réussi
     */
    suspend fun forceCloudEngine(): Boolean {
        return try {
            mistralEngine.initialize()
            activeEngine = mistralEngine
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retourne le type de moteur actuellement actif
     *
     * @return EngineType (GEMINI_NANO ou GEMINI_FLASH), ou null si pas initialisé
     *
     * Utile pour :
     * - Afficher dans l'UI ("Mode : IA Locale" vs "Mode : IA Cloud")
     * - Analytics/Monitoring
     * - Debug
     */
    fun getActiveEngineType(): AIEngine.EngineType? {
        return activeEngine?.getEngineType()
    }

    /**
     * Vérifie si le moteur actif est Gemini Nano (on-device)
     *
     * @return true si IA locale active, false si cloud
     */
    fun isUsingLocalAI(): Boolean {
        return activeEngine?.getEngineType() == AIEngine.EngineType.GEMINI_NANO
    }

    /**
     * Réinitialise le moteur (utile pour changer de mode)
     *
     * Cas d'usage :
     * - L'utilisateur préfère forcer le mode cloud
     * - Le modèle Nano devient disponible après mise à jour
     * - Erreur récurrente sur un moteur
     */
    suspend fun resetEngine() {
        activeEngine = null
        selectAndInitializeEngine()
    }

    /**
     * Retourne des informations sur le moteur actif (pour affichage UI)
     *
     * @return Map avec détails du moteur (type, disponibilité, etc.)
     */
    suspend fun getEngineInfo(): Map<String, Any> {
        val engine = getEngine()

        return mapOf(
            "type" to engine.getEngineType().name,
            "isLocal" to isUsingLocalAI(),
            "nanoAvailable" to geminiNanoEngine.isAvailable(),
            "mistralAvailable" to mistralEngine.isAvailable()
        )
    }
}
