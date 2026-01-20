package com.enduranceflow.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Écran de bienvenue - Premier écran de l'onboarding
 *
 * Présente l'application EnduranceFlow et ses fonctionnalités principales :
 * - Génération IA de séances personnalisées
 * - Adaptation au niveau et objectifs
 * - Coaching empathique
 * - Privacy-first (données anonymes uniquement)
 */
@Composable
fun WelcomeScreen(
    onNavigateNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo / Icône (placeholder emoji pour l'instant)
        Text(
            text = "🏃💨",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Titre principal
        Text(
            text = "Bienvenue sur EnduranceFlow",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sous-titre
        Text(
            text = "Votre coach d'endurance adaptatif propulsé par l'IA",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Fonctionnalités principales
        FeatureCard(
            emoji = "🤖",
            title = "IA Personnalisée",
            description = "Des séances générées selon votre niveau, vos objectifs et votre fatigue"
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureCard(
            emoji = "📊",
            title = "Suivi Intelligent",
            description = "L'IA s'adapte en temps réel à vos feedbacks et votre progression"
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureCard(
            emoji = "🔒",
            title = "Privacy-First",
            description = "Seules les données anonymes (VMA, FTP, RPE) sont utilisées pour l'IA"
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureCard(
            emoji = "💪",
            title = "Indoor & Outdoor",
            description = "Course, vélo, tapis, home trainer - adaptez vos séances partout"
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Commencer
        Button(
            onClick = onNavigateNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Commencer")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Carte de fonctionnalité avec emoji, titre et description
 */
@Composable
private fun FeatureCard(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
