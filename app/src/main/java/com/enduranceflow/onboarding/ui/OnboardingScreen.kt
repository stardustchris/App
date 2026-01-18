package com.enduranceflow.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Écran d'Onboarding - Premier démarrage
 *
 * Fonctionnalités :
 * - Présentation de l'application
 * - Collecte du genre (pour adaptation du ton de l'IA)
 * - Collecte de l'équipement (capteur de puissance, etc.)
 * - Détection si VMA/FTP sont connus
 *
 * Navigation :
 * - Si VMA/FTP inconnus → Calibration
 * - Si VMA/FTP connus → WorkoutList
 */
@Composable
fun OnboardingScreen(
    onNavigateToCalibration: () -> Unit,
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
            text = "Bienvenue sur EnduranceFlow",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Votre coach d'endurance adaptatif Indoor/Outdoor",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        // TODO: Formulaire de collecte des informations
        // - Genre (Homme/Femme) → Influence le ton de l'IA
        // - Équipement (Capteur puissance : Oui/Non)
        // - VMA connue ? FTP connu ?

        // Bouton temporaire pour la navigation
        Button(
            onClick = onNavigateToCalibration,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Commencer la calibration")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToWorkouts) {
            Text("J'ai déjà un profil")
        }
    }
}
