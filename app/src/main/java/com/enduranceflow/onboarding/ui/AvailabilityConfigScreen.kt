package com.enduranceflow.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.DayOfWeek

/**
 * Écran de configuration des disponibilités
 *
 * Permet à l'utilisateur de choisir :
 * - Les jours de la semaine où il peut s'entraîner
 * - Les créneaux horaires disponibles (futur)
 *
 * Ces informations seront utilisées par l'IA pour planifier
 * les séances aux moments appropriés.
 */
@Composable
fun AvailabilityConfigScreen(
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
            text = "Quand êtes-vous disponible ?",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Sélectionnez les jours où vous pouvez vous entraîner",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Liste des jours de la semaine
        DayOfWeek.values().forEach { day ->
            DaySelectionCard(
                dayOfWeek = day,
                isSelected = uiState.selectedDays.contains(day),
                onToggle = { viewModel.onDayToggled(day) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info : au moins un jour requis
        if (uiState.selectedDays.isEmpty()) {
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
                        text = "Sélectionnez au moins un jour pour continuer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        } else {
            // Résumé
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📅 Résumé",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${uiState.selectedDays.size} jour(s) sélectionné(s) pour ${uiState.selectedDays.size * 60} min d'entraînement par semaine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Continuer
        Button(
            onClick = { onNavigateNext() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedDays.isNotEmpty()
        ) {
            Text("Continuer")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Carte de sélection d'un jour
 */
@Composable
private fun DaySelectionCard(
    dayOfWeek: DayOfWeek,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val dayNames = mapOf(
        DayOfWeek.MONDAY to "Lundi",
        DayOfWeek.TUESDAY to "Mardi",
        DayOfWeek.WEDNESDAY to "Mercredi",
        DayOfWeek.THURSDAY to "Jeudi",
        DayOfWeek.FRIDAY to "Vendredi",
        DayOfWeek.SATURDAY to "Samedi",
        DayOfWeek.SUNDAY to "Dimanche"
    )

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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dayNames[dayOfWeek] ?: dayOfWeek.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Sélectionné",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
