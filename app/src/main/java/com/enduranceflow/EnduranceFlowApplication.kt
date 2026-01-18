package com.enduranceflow

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe Application principale pour EnduranceFlow
 *
 * @HiltAndroidApp : Active Hilt (Dependency Injection) pour toute l'application
 * Permet d'injecter automatiquement les dépendances (Database, Repositories, AI Engine, etc.)
 */
@HiltAndroidApp
class EnduranceFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // TODO: Initialiser les composants essentiels
        // - Vérifier la disponibilité de Health Connect
        // - Détecter la disponibilité de Gemini Nano (AI locale)
        // - Configurer les logs et le monitoring
    }
}
