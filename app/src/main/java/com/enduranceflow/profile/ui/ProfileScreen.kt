package com.enduranceflow.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Écran de Profil - Paramètres athlète
 *
 * Fonctionnalités :
 * - Affichage des données athlète (VMA, FTP, FC Max)
 * - Configuration équipement (capteur de puissance)
 * - Gestion du genre (pour l'adaptation du ton de l'IA)
 * - Paramètres de l'IA (Locale/Cloud)
 * - Modification des disponibilités
 *
 * Entités (context.json) :
 * - AthleteProfile (Gender, VMA, FTP, MaxHR)
 * - EquipmentConfig (HasPowerMeter: Boolean)
 *
 * Navigation :
 * - Retour vers WorkoutList
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Section : Données physiologiques
            Text(
                text = "📊 Données physiologiques",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ProfileDataRow(label = "VMA", value = "18.5 km/h")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileDataRow(label = "FTP", value = "280 W")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileDataRow(label = "FC Max", value = "195 bpm")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section : Équipement
            Text(
                text = "⚙️ Équipement",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Capteur de puissance (vélo)",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(checked = true, onCheckedChange = {})
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section : IA
            Text(
                text = "🤖 Intelligence Artificielle",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Mode : Gemini Nano (Local)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fallback Cloud activé",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Composant réutilisable pour afficher une ligne de données de profil
 */
@Composable
private fun ProfileDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
