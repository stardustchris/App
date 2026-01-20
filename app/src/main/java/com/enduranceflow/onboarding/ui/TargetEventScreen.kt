package com.enduranceflow.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Types d'événements sportifs
 */
enum class EventType {
    MARATHON,           // 42.195 km
    HALF_MARATHON,      // 21.1 km
    TEN_K,              // 10 km
    FIVE_K,             // 5 km
    TRAIL,              // Trail running (distance variable)
    TRIATHLON,          // Triathlon (S, M, L, XL)
    CYCLING_RACE,       // Course cycliste
    CENTURY_RIDE,       // 100 km+ à vélo
    OTHER               // Autre événement
}

/**
 * Écran d'événement cible (compétition)
 *
 * Affiché uniquement si l'utilisateur a choisi "Compétition" comme objectif.
 *
 * Collecte les détails de l'événement :
 * - Type d'événement (Marathon, 10k, Triathlon, etc.)
 * - Date de l'événement
 * - Distance (si applicable)
 * - Nom de l'événement (optionnel)
 *
 * Ces informations permettront à l'IA de :
 * - Calculer le temps restant avant l'événement
 * - Adapter la périodisation de l'entraînement
 * - Ajuster progressivement l'intensité
 */
@Composable
fun TargetEventScreen(
    onNavigateNext: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedEventType by remember { mutableStateOf<EventType?>(null) }
    var eventDateText by remember { mutableStateOf("") }
    var eventNameText by remember { mutableStateOf("") }
    var customDistanceText by remember { mutableStateOf("") }

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
            text = "Votre objectif de compétition",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Nous adapterons votre programme pour que vous soyez prêt le jour J",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Type d'événement
        Text(
            text = "Type d'événement",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dropdown pour type d'événement
        EventTypeDropdown(
            selectedType = selectedEventType,
            onTypeSelected = { selectedEventType = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Date de l'événement
        Text(
            text = "Date de l'événement",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = eventDateText,
            onValueChange = { eventDateText = it },
            label = { Text("Date") },
            placeholder = { Text("AAAA-MM-JJ (ex: 2026-06-15)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Calculer le temps restant
        val daysUntilEvent = try {
            if (eventDateText.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                val eventDate = LocalDate.parse(eventDateText, DateTimeFormatter.ISO_LOCAL_DATE)
                val today = LocalDate.now()
                java.time.temporal.ChronoUnit.DAYS.between(today, eventDate).toInt()
            } else null
        } catch (e: Exception) {
            null
        }

        if (daysUntilEvent != null && daysUntilEvent > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏰",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "$daysUntilEvent jours restants (${"%.1f".format(daysUntilEvent / 7.0)} semaines)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Distance (si nécessaire)
        if (selectedEventType == EventType.TRAIL ||
            selectedEventType == EventType.CYCLING_RACE ||
            selectedEventType == EventType.OTHER) {
            Text(
                text = "Distance",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = customDistanceText,
                onValueChange = { customDistanceText = it },
                label = { Text("Distance") },
                placeholder = { Text("Ex: 25") },
                suffix = { Text("km") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Nom de l'événement (optionnel)
        Text(
            text = "Nom de l'événement (optionnel)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = eventNameText,
            onValueChange = { eventNameText = it },
            label = { Text("Nom") },
            placeholder = { Text("Ex: Marathon de Paris") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Continuer
        val canContinue = selectedEventType != null && eventDateText.isNotEmpty() && daysUntilEvent != null && daysUntilEvent > 0

        Button(
            onClick = {
                val distance = when (selectedEventType) {
                    EventType.MARATHON -> 42.195
                    EventType.HALF_MARATHON -> 21.1
                    EventType.TEN_K -> 10.0
                    EventType.FIVE_K -> 5.0
                    EventType.CENTURY_RIDE -> 100.0
                    else -> customDistanceText.toDoubleOrNull() ?: 0.0
                }

                viewModel.onTargetEventEntered(
                    eventType = selectedEventType!!,
                    eventDate = eventDateText,
                    eventName = eventNameText.ifEmpty { null },
                    distance = distance
                )
                onNavigateNext()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = canContinue
        ) {
            Text("Continuer")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Dropdown pour sélectionner le type d'événement
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTypeDropdown(
    selectedType: EventType?,
    onTypeSelected: (EventType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val eventTypeLabels = mapOf(
        EventType.MARATHON to "Marathon (42.195 km)",
        EventType.HALF_MARATHON to "Semi-marathon (21.1 km)",
        EventType.TEN_K to "10 km",
        EventType.FIVE_K to "5 km",
        EventType.TRAIL to "Trail (distance variable)",
        EventType.TRIATHLON to "Triathlon",
        EventType.CYCLING_RACE to "Course cycliste",
        EventType.CENTURY_RIDE to "Century Ride (100+ km)",
        EventType.OTHER to "Autre"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedType?.let { eventTypeLabels[it] } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Sélectionnez un type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            eventTypeLabels.forEach { (type, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
