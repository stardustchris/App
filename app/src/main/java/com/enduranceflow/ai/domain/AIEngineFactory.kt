package com.enduranceflow.ai.domain

import com.enduranceflow.ai.data.MistralEngine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory pour le moteur IA Mistral
 *
 * Utilise uniquement Mistral AI (entreprise française 🇫🇷) pour :
 * - Génération de plans d'entraînement
 * - Messages motivationnels personnalisés
 * - Analyse des feedbacks et recommandations
 *
 * Avantages Mistral AI :
 * - Excellente compréhension du français
 * - Quota gratuit généreux
 * - API rapide et fiable
 * - Privacy-first (seules données anonymes envoyées)
 *
 * @Singleton : Une seule instance pour toute l'app (économie mémoire/perf)
 */
@Singleton
class AIEngineFactory @Inject constructor(
    private val mistralEngine: MistralEngine
) {

    // Moteur IA actuellement actif
    private var activeEngine: AIEngine? = null

    /**
     * Obtient le moteur IA Mistral (ou le crée si nécessaire)
     *
     * @return Instance du moteur IA Mistral
     */
    suspend fun getEngine(): AIEngine {
        // Si le moteur est déjà initialisé, le retourner
        activeEngine?.let { return it }

        // Sinon, initialiser Mistral
        mistralEngine.initialize()
        activeEngine = mistralEngine
        return mistralEngine
    }

    /**
     * Retourne le type de moteur actuellement actif
     *
     * @return EngineType (toujours GEMINI_FLASH qui est réutilisé pour cloud AI)
     */
    fun getActiveEngineType(): AIEngine.EngineType? {
        return activeEngine?.getEngineType()
    }

    /**
     * Vérifie si Mistral est disponible
     *
     * @return true si connexion Internet disponible
     */
    suspend fun isAvailable(): Boolean {
        return mistralEngine.isAvailable()
    }

    /**
     * Réinitialise le moteur (utile pour forcer une reconnexion)
     *
     * Cas d'usage :
     * - Changement de clé API
     * - Erreur de connexion persistante
     * - Tests
     */
    suspend fun resetEngine() {
        activeEngine = null
        mistralEngine.initialize()
        activeEngine = mistralEngine
    }

    /**
     * Retourne des informations sur le moteur actif (pour affichage UI)
     *
     * @return Map avec détails du moteur
     */
    suspend fun getEngineInfo(): Map<String, Any> {
        val engine = getEngine()

        return mapOf(
            "type" to "Mistral AI",
            "model" to "mistral-small-latest",
            "isAvailable" to mistralEngine.isAvailable()
        )
    }
}
