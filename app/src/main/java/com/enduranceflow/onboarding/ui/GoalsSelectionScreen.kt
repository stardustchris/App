package com.enduranceflow.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Types d'objectifs d'entraînement
 */
enum class TrainingGoal {
    COMPETITION,      // Préparation compétition
    FITNESS,          // Améliorer forme générale
    WEIGHT_LOSS,      // Perte de poids
    MAINTENANCE       // Maintien forme
}

/**
 * Écran de sélection des objectifs
 *
 * Permet à l'utilisateur de choisir son objectif principal :
 * - Compétition : Préparer un événement sportif (marathon, triathlon, etc.)
 * - Forme : Améliorer sa condition physique générale
 * - Perte de poids : Objectif de perte de poids avec endurance
 * - Maintenance : Maintenir sa forme actuelle
 *
 * Si "Compétition" est choisi, l'écran suivant demandera les détails
 * de l'événement cible (type, date, distance).
 */
@Composable
fun GoalsSelectionScreen(
    onNavigateNext: (hasCompetition: Boolean) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Titre
        Text(
            text = "Quel est votre objectif ?",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Nous adapterons vos séances en fonction de votre objectif",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Carte Compétition
        GoalCard(
            icon = Icons.Default.EmojiEvents,
            title = "Compétition",
            description = "Préparer un événement sportif (marathon, triathlon, cyclosportive...)",
            isSelected = uiState.selectedGoal == TrainingGoal.COMPETITION,
            onSelect = { viewModel.onGoalSelected(TrainingGoal.COMPETITION) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Carte Forme
        GoalCard(
            icon = Icons.Default.FitnessCenter,
            title = "Améliorer ma forme",
            description = "Développer mon endurance et ma condition physique générale",
            isSelected = uiState.selectedGoal == TrainingGoal.FITNESS,
            onSelect = { viewModel.onGoalSelected(TrainingGoal.FITNESS) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Carte Perte de poids
        GoalCard(
            icon = Icons.Default.TrendingDown,
            title = "Perdre du poids",
            description = "Combiner endurance et dépense calorique pour atteindre mon poids cible",
            isSelected = uiState.selectedGoal == TrainingGoal.WEIGHT_LOSS,
            onSelect = { viewModel.onGoalSelected(TrainingGoal.WEIGHT_LOSS) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Carte Maintenance
        GoalCard(
            icon = Icons.Default.Favorite,
            title = "Maintenir ma forme",
            description = "Conserver mon niveau actuel avec un entraînement régulier",
            isSelected = uiState.selectedGoal == TrainingGoal.MAINTENANCE,
            onSelect = { viewModel.onGoalSelected(TrainingGoal.MAINTENANCE) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Continuer
        Button(
            onClick = {
                val hasCompetition = uiState.selectedGoal == TrainingGoal.COMPETITION
                onNavigateNext(hasCompetition)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedGoal != null
        ) {
            Text("Continuer")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Carte de sélection d'objectif
 */
@Composable
private fun GoalCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Texte
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Checkmark si sélectionné
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Sélectionné",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
