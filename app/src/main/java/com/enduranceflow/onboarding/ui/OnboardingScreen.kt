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
import androidx.hilt.navigation.compose.hiltViewModel
import com.enduranceflow.core.domain.model.Gender

/**
 * Écran d'Onboarding - Premier démarrage
 *
 * Connecté au OnboardingViewModel via Hilt
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
    onNavigateToWorkouts: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    // Collecte du state depuis le ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // État local pour les champs de saisie
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

        // Section 1 : Genre
        Text(
            text = "1. Quel est votre genre ?",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "L'IA adaptera son ton de coaching selon votre préférence",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bouton Femme
            OutlinedButton(
                onClick = { viewModel.onGenderSelected(Gender.FEMALE) },
                modifier = Modifier.weight(1f),
                colors = if (uiState.selectedGender == Gender.FEMALE) {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    ButtonDefaults.outlinedButtonColors()
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Femme", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Ton empathique",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bouton Homme
            OutlinedButton(
                onClick = { viewModel.onGenderSelected(Gender.MALE) },
                modifier = Modifier.weight(1f),
                colors = if (uiState.selectedGender == Gender.MALE) {
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    ButtonDefaults.outlinedButtonColors()
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Homme", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Ton analytique",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 2 : Équipement
        Text(
            text = "2. Équipement",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Capteur de puissance (vélo)",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Permet les séances en Watts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = uiState.hasPowerMeter,
                onCheckedChange = { viewModel.onPowerMeterChanged(it) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 3 : Données physiologiques (optionnel)
        Text(
            text = "3. Connaissez-vous vos données ?",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Optionnel : Si vous ne connaissez pas, nous ferons des tests de calibration",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ VMA
        OutlinedTextField(
            value = vmaText,
            onValueChange = { vmaText = it },
            label = { Text("VMA (km/h)") },
            placeholder = { Text("Ex: 18.5") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ FTP
        OutlinedTextField(
            value = ftpText,
            onValueChange = { ftpText = it },
            label = { Text("FTP (Watts)") },
            placeholder = { Text("Ex: 280") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(48.dp))

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

        // Bouton principal
        Button(
            onClick = {
                // Valider les données saisies
                val vma = vmaText.toDoubleOrNull()
                val ftp = ftpText.toIntOrNull()
                viewModel.onPhysioDataEntered(vma, ftp)

                // Compléter l'onboarding
                viewModel.completeOnboarding { needsCalibration ->
                    if (needsCalibration) {
                        onNavigateToCalibration()
                    } else {
                        onNavigateToWorkouts()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.selectedGender != null
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                if (vmaText.isNotEmpty() && ftpText.isNotEmpty()) {
                    "Commencer"
                } else {
                    "Continuer vers la calibration"
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
