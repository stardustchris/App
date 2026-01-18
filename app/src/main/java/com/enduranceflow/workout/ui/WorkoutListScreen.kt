package com.enduranceflow.workout.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enduranceflow.workout.data.entity.DailyWorkoutEntity

/**
 * Écran principal - Liste des séances d'entraînement
 *
 * Connecté au WorkoutListViewModel via Hilt
 *
 * Fonctionnalités :
 * - Affichage du programme de la semaine
 * - Basculement Indoor/Outdoor par séance
 * - Accès aux détails de chaque séance
 * - Historique des séances passées
 *
 * User Story (context.json) :
 * "Je peux basculer une séance en mode 'Intérieur' :
 *  les cibles changent (Vitesse → Tapis, Allure → Watts/Cardio)."
 *
 * Navigation :
 * - Accès au profil via le bouton en haut à droite
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    onNavigateToProfile: () -> Unit,
    viewModel: WorkoutListViewModel = hiltViewModel()
) {
    // Collecte du state depuis le ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Séances") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.generateWeeklyWorkouts() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Générer séances")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // État de chargement
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.upcomingWorkouts.isEmpty()) {
                // Pas de séances
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🏃 Aucune séance programmée",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Appuyez sur + pour générer votre programme de la semaine",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                // Liste des séances
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Alertes de fatigue
                    if (uiState.isFatigued) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ Fatigue détectée : ${uiState.averageRPE?.let { "RPE moyen %.1f/10".format(it) } ?: ""}",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    if (uiState.needsProgression) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📈 Progression possible : Les séances semblent faciles",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // Liste des séances
                    items(uiState.upcomingWorkouts, key = { it.id }) { workout ->
                        WorkoutCard(
                            workout = workout,
                            onToggleIndoor = { viewModel.toggleIndoorMode(workout.id, !workout.isIndoor) },
                            onMarkCompleted = { viewModel.markWorkoutAsCompleted(workout.id) {} }
                        )
                    }
                }
            }

            // Affichage des erreurs
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(uiState.error!!)
                }
            }
        }
    }
}

/**
 * Carte pour afficher une séance d'entraînement
 */
@Composable
private fun WorkoutCard(
    workout: DailyWorkoutEntity,
    onToggleIndoor: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (workout.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Titre
            Text(
                text = workout.title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Date + Sport
            Text(
                text = "${workout.date} • ${workout.sport.name}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = workout.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Détails
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${workout.durationMinutes} min",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${workout.targetType.name}: ${workout.targetValue}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switch Indoor/Outdoor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (workout.isIndoor) "Mode Intérieur" else "Mode Extérieur",
                    style = MaterialTheme.typography.labelLarge
                )
                Switch(
                    checked = workout.isIndoor,
                    onCheckedChange = { onToggleIndoor() },
                    enabled = !workout.isCompleted
                )
            }

            if (!workout.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onMarkCompleted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Marquer comme terminée")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✅ Séance terminée",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
