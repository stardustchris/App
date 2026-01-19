package com.enduranceflow.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enduranceflow.core.domain.model.Sport

/**
 * Écran de sélection des sports pratiqués
 *
 * Permet à l'utilisateur de choisir quel(s) sport(s) il pratique :
 * - Course à pied
 * - Vélo
 * - Les deux
 *
 * Cette information sera utilisée par l'IA pour générer uniquement
 * des séances pour les sports sélectionnés.
 */
@Composable
fun SportsSelectionScreen(
    onNavigateNext: () -> Unit,
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
            text = "Quel(s) sport(s) pratiquez-vous ?",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Sélectionnez un ou plusieurs sports pour personnaliser votre programme",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Carte Course à pied
        SportSelectionCard(
            sportName = "Course à pied",
            sportDescription = "Running indoor/outdoor • VMA • Allure",
            icon = Icons.Default.DirectionsRun,
            isSelected = uiState.selectedSports.contains(Sport.RUNNING),
            onToggle = { viewModel.onSportToggled(Sport.RUNNING) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Carte Vélo
        SportSelectionCard(
            sportName = "Vélo",
            sportDescription = "Cyclisme indoor/outdoor • FTP • Puissance",
            icon = Icons.Default.DirectionsBike,
            isSelected = uiState.selectedSports.contains(Sport.CYCLING),
            onToggle = { viewModel.onSportToggled(Sport.CYCLING) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Info : au moins un sport requis
        if (uiState.selectedSports.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "Sélectionnez au moins un sport pour continuer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Continuer
        Button(
            onClick = { onNavigateNext() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedSports.isNotEmpty()
        ) {
            Text("Continuer")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Carte de sélection d'un sport
 */
@Composable
private fun SportSelectionCard(
    sportName: String,
    sportDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
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
            // Icône du sport
            Icon(
                imageVector = icon,
                contentDescription = sportName,
                modifier = Modifier.size(48.dp),
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
                    text = sportName,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sportDescription,
                    style = MaterialTheme.typography.bodyMedium,
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
