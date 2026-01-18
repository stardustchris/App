package com.enduranceflow.calibration.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Écran de Calibration - Semaine de tests
 *
 * Fonctionnalités :
 * - Affichage des tests à réaliser (VMA, FTP)
 * - Instructions pour chaque test
 * - Enregistrement des résultats
 * - Calcul des zones d'entraînement
 *
 * User Story (context.json) :
 * "Au démarrage, si je ne connais pas mon niveau (VMA/FTP),
 *  l'app génère une 'Semaine de Calibration' avec des tests."
 *
 * Navigation :
 * - Une fois les tests terminés → WorkoutList
 */
@Composable
fun CalibrationScreen(
    onNavigateToWorkouts: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Titre
        Text(
            text = "Semaine de Calibration",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Réalisez ces tests pour déterminer vos zones d'entraînement personnalisées",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        // TODO: Liste des tests
        // - Test VMA (Course à pied)
        // - Test FTP (Vélo)
        // - Test FC Max (si pas de capteur de puissance)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📊 Test VMA",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Test progressif pour déterminer votre Vitesse Maximale Aérobie",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🚴 Test FTP",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Test de 20 minutes pour déterminer votre seuil fonctionnel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Bouton temporaire
        Button(
            onClick = onNavigateToWorkouts,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Terminer la calibration")
        }
    }
}
