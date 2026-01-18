package com.enduranceflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.enduranceflow.core.ui.theme.EnduranceFlowTheme
import com.enduranceflow.core.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activité principale de l'application EnduranceFlow
 *
 * @AndroidEntryPoint : Permet l'injection de dépendances Hilt dans cette Activity
 *
 * Point d'entrée de l'application :
 * 1. Applique le thème Material 3 (EnduranceFlowTheme)
 * 2. Initialise la navigation Compose (AppNavigation)
 * 3. Gère le cycle de vie de l'application
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EnduranceFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Navigation principale de l'application
                    AppNavigation()
                }
            }
        }
    }
}
