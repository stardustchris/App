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

/**
 * Écran de profil personnel
 *
 * Collecte les données physiologiques de base :
 * - Âge (années)
 * - Poids (kg)
 * - Taille (cm)
 * - FC Max (bpm) - optionnel, peut être calculé par formule (220 - âge)
 *
 * Ces données permettront :
 * - Calcul des zones d'entraînement plus précises
 * - Personnalisation des recommandations IA
 * - Estimation de la FC Max si non connue
 */
@Composable
fun PersonalProfileScreen(
    onNavigateNext: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var ageText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
    var maxHRText by remember { mutableStateOf("") }

    // Calculer FC Max estimée depuis l'âge
    val estimatedMaxHR = ageText.toIntOrNull()?.let { age ->
        if (age in 10..100) 220 - age else null
    }

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
            text = "Votre profil",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Ces informations nous aident à personnaliser vos séances",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Champ Âge
        OutlinedTextField(
            value = ageText,
            onValueChange = { ageText = it },
            label = { Text("Âge") },
            placeholder = { Text("Ex: 35") },
            suffix = { Text("ans") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ Poids
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("Poids") },
            placeholder = { Text("Ex: 75") },
            suffix = { Text("kg") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ Taille
        OutlinedTextField(
            value = heightText,
            onValueChange = { heightText = it },
            label = { Text("Taille") },
            placeholder = { Text("Ex: 175") },
            suffix = { Text("cm") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section FC Max avec aide
        Text(
            text = "Fréquence cardiaque maximale",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Info sur FC Max estimée
        if (estimatedMaxHR != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "FC Max estimée : $estimatedMaxHR bpm (formule : 220 - âge)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Champ FC Max (optionnel)
        OutlinedTextField(
            value = maxHRText,
            onValueChange = { maxHRText = it },
            label = { Text("FC Max (optionnel)") },
            placeholder = { Text(estimatedMaxHR?.toString() ?: "Ex: 185") },
            suffix = { Text("bpm") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Si vous ne connaissez pas votre FC Max, nous utiliserons la formule estimée",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Continuer
        val age = ageText.toIntOrNull()
        val weight = weightText.toDoubleOrNull()
        val height = heightText.toIntOrNull()
        val canContinue = age != null && weight != null && height != null

        Button(
            onClick = {
                val maxHR = maxHRText.toIntOrNull() ?: estimatedMaxHR
                viewModel.onPersonalProfileEntered(age!!, weight!!, height!!, maxHR)
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
