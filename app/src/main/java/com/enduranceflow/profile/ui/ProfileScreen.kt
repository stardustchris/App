package com.enduranceflow.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Écran de Profil - Paramètres athlète
 *
 * Connecté au ProfileViewModel via Hilt
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
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // Collecte du state depuis le ViewModel
    val uiState by viewModel.uiState.collectAsState()

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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
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
                        ProfileDataRow(
                            label = "VMA",
                            value = uiState.athleteProfile?.vma?.let { "%.1f km/h".format(it) }
                                ?: "Non renseigné"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDataRow(
                            label = "FTP",
                            value = uiState.athleteProfile?.ftp?.let { "$it W" }
                                ?: "Non renseigné"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDataRow(
                            label = "FC Max",
                            value = uiState.athleteProfile?.maxHR?.let { "$it bpm" }
                                ?: "Non renseigné"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDataRow(
                            label = "Genre",
                            value = uiState.athleteProfile?.gender?.name ?: "Non renseigné"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section : Ton de l'IA
                Text(
                    text = "💬 Ton du coaching",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = viewModel.getAITone(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Capteur de puissance (vélo)",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (uiState.equipmentConfig?.hasPowerMeter == true)
                                "Séances vélo en Watts"
                            else
                                "Séances vélo en fréquence cardiaque",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.equipmentConfig?.hasPowerMeter ?: false,
                        onCheckedChange = { viewModel.togglePowerMeter(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section : Zones d'entraînement VMA
                if (uiState.athleteProfile?.vma != null) {
                    Text(
                        text = "🎯 Zones d'entraînement VMA",
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            viewModel.calculateVMAZones()?.forEach { (zone, value) ->
                                ProfileDataRow(zone, "%.1f km/h".format(value))
                                if (zone != "Zone 5 (Anaérobie)") {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section : Zones d'entraînement FTP
                if (uiState.athleteProfile?.ftp != null) {
                    Text(
                        text = "🎯 Zones d'entraînement FTP",
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            viewModel.calculateFTPZones()?.forEach { (zone, value) ->
                                ProfileDataRow(zone, "$value W")
                                if (zone != "Zone 6 (Anaérobie)") {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

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
                            text = "Mode : Mistral AI 🇫🇷",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "IA cloud française avec excellente compréhension du français",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Privacy: Seules les données anonymes (VMA, FTP, RPE) sont envoyées",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
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
