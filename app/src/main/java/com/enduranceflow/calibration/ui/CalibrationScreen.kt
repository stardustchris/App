package com.enduranceflow.calibration.ui

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

/**
 * Écran de Calibration - Semaine de tests
 *
 * Connecté au CalibrationViewModel via Hilt
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
    onNavigateToWorkouts: () -> Unit,
    viewModel: CalibrationViewModel = hiltViewModel()
) {
    // Collecte du state depuis le ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // États locaux pour les champs de saisie
    var vmaText by remember { mutableStateOf("") }
    var ftpText by remember { mutableStateOf("") }

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

        // Test VMA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.vmaCompleted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊 Test VMA",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.vmaCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Terminé",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Test progressif pour déterminer votre Vitesse Maximale Aérobie",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (uiState.vmaCompleted && uiState.currentVMA != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Résultat : ${String.format("%.1f", uiState.currentVMA)} km/h",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = vmaText,
                        onValueChange = { vmaText = it },
                        label = { Text("VMA (km/h)") },
                        placeholder = { Text("Ex: 18.5") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            vmaText.toDoubleOrNull()?.let { vma ->
                                viewModel.submitVMATest(vma)
                                vmaText = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = vmaText.isNotEmpty() && !uiState.isLoading
                    ) {
                        Text("Valider le test VMA")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Test FTP
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.ftpCompleted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚴 Test FTP",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.ftpCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Terminé",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Test de 20 minutes pour déterminer votre seuil fonctionnel",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (uiState.ftpCompleted && uiState.currentFTP != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Résultat : ${uiState.currentFTP} W",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = ftpText,
                        onValueChange = { ftpText = it },
                        label = { Text("FTP (Watts)") },
                        placeholder = { Text("Ex: 280") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            ftpText.toIntOrNull()?.let { ftp ->
                                viewModel.submitFTPTest(ftp)
                                ftpText = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = ftpText.isNotEmpty() && !uiState.isLoading
                    ) {
                        Text("Valider le test FTP")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Affichage des zones d'entraînement si au moins un test complété
        if (uiState.vmaCompleted && uiState.currentVMA != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎯 Zones d'entraînement VMA",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val vma = uiState.currentVMA!!
                    ZoneRow("Zone 1 (Récup)", "${String.format("%.1f", vma * 0.6)} km/h")
                    ZoneRow("Zone 2 (Endurance)", "${String.format("%.1f", vma * 0.8)} km/h")
                    ZoneRow("Zone 3 (Seuil)", "${String.format("%.1f", vma * 0.9)} km/h")
                    ZoneRow("Zone 4 (VMA)", "${String.format("%.1f", vma * 1.0)} km/h")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.ftpCompleted && uiState.currentFTP != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎯 Zones d'entraînement FTP",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val ftp = uiState.currentFTP!!
                    ZoneRow("Zone 1 (Récup)", "${(ftp * 0.5).toInt()} W")
                    ZoneRow("Zone 2 (Endurance)", "${(ftp * 0.7).toInt()} W")
                    ZoneRow("Zone 3 (Tempo)", "${(ftp * 0.85).toInt()} W")
                    ZoneRow("Zone 4 (Seuil)", "${(ftp * 1.0).toInt()} W")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Affichage des erreurs
        if (uiState.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bouton de fin de calibration
        if (viewModel.isCalibrationComplete()) {
            Button(
                onClick = onNavigateToWorkouts,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Terminer la calibration et commencer")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Composant pour afficher une zone d'entraînement
 */
@Composable
private fun ZoneRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
