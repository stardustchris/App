package com.enduranceflow.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enduranceflow.core.domain.model.Gender
import com.enduranceflow.profile.data.entity.AthleteProfileEntity
import com.enduranceflow.profile.data.entity.EquipmentConfigEntity
import com.enduranceflow.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran Profile
 *
 * Responsabilités :
 * - Afficher le profil athlète (VMA, FTP, Genre, MaxHR)
 * - Afficher la configuration équipement (capteur de puissance)
 * - Modifier les données physiologiques
 * - Modifier l'équipement
 * - Afficher le mode IA (Locale/Cloud)
 *
 * Core Entities (context.json) :
 * - AthleteProfile (Gender, VMA, FTP, MaxHR)
 * - EquipmentConfig (HasPowerMeter: Boolean)
 *
 * Injecté par Hilt (@HiltViewModel)
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // État de l'UI
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Charge le profil athlète et la configuration équipement
     *
     * Combine les deux Flows pour mettre à jour l'UI
     */
    private fun loadProfile() {
        viewModelScope.launch {
            combine(
                profileRepository.getProfile(),
                profileRepository.getEquipmentConfig()
            ) { profile, equipment ->
                _uiState.value = _uiState.value.copy(
                    athleteProfile = profile,
                    equipmentConfig = equipment,
                    isLoading = false
                )
            }.collect { /* Flow combiné */ }
        }
    }

    /**
     * Met à jour la VMA
     *
     * @param vma Nouvelle valeur de VMA en km/h
     */
    fun updateVMA(vma: Double) {
        viewModelScope.launch {
            try {
                profileRepository.updateVMA(vma)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la mise à jour de la VMA: ${e.message}"
                )
            }
        }
    }

    /**
     * Met à jour le FTP
     *
     * @param ftp Nouvelle valeur de FTP en Watts
     */
    fun updateFTP(ftp: Int) {
        viewModelScope.launch {
            try {
                profileRepository.updateFTP(ftp)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la mise à jour du FTP: ${e.message}"
                )
            }
        }
    }

    /**
     * Met à jour la FC Max
     *
     * @param maxHR Nouvelle valeur de FC Max en bpm
     */
    fun updateMaxHR(maxHR: Int) {
        viewModelScope.launch {
            try {
                profileRepository.updateMaxHR(maxHR)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la mise à jour de la FC Max: ${e.message}"
                )
            }
        }
    }

    /**
     * Bascule le statut du capteur de puissance
     *
     * @param hasPowerMeter true si l'utilisateur possède un capteur
     *
     * Impact :
     * - Si true → Les séances vélo auront des cibles en POWER (Watts)
     * - Si false → Fallback sur HEART_RATE (bpm)
     *
     * Avoid List (context.json) :
     * "Ne pas proposer de Watts en vélo si l'utilisateur n'a pas de
     *  capteur de puissance (Fallback sur Fréquence Cardiaque)."
     */
    fun togglePowerMeter(hasPowerMeter: Boolean) {
        viewModelScope.launch {
            try {
                profileRepository.updatePowerMeter(hasPowerMeter)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la mise à jour de l'équipement: ${e.message}"
                )
            }
        }
    }

    /**
     * Calcule les zones d'entraînement VMA
     *
     * @return Map<String, Double> - Zones avec leurs valeurs en km/h
     *
     * Exemple :
     * - "Zone 1 (Récupération)" → 11.1 km/h (60% VMA)
     * - "Zone 2 (Endurance)" → 14.8 km/h (80% VMA)
     * - "Zone 3 (Seuil)" → 16.7 km/h (90% VMA)
     * - "Zone 4 (VMA)" → 18.5 km/h (100% VMA)
     */
    fun calculateVMAZones(): Map<String, Double>? {
        val vma = _uiState.value.athleteProfile?.vma ?: return null

        return mapOf(
            "Zone 1 (Récupération)" to vma * 0.60,
            "Zone 2 (Endurance)" to vma * 0.80,
            "Zone 3 (Seuil)" to vma * 0.90,
            "Zone 4 (VMA)" to vma * 1.00,
            "Zone 5 (Anaérobie)" to vma * 1.05
        )
    }

    /**
     * Calcule les zones d'entraînement FTP
     *
     * @return Map<String, Int> - Zones avec leurs valeurs en Watts
     *
     * Exemple :
     * - "Zone 1 (Récupération)" → 140 W (< 55% FTP)
     * - "Zone 2 (Endurance)" → 196 W (70% FTP)
     * - "Zone 3 (Tempo)" → 238 W (85% FTP)
     * - "Zone 4 (Seuil)" → 280 W (100% FTP)
     * - "Zone 5 (VO2max)" → 308 W (110% FTP)
     */
    fun calculateFTPZones(): Map<String, Int>? {
        val ftp = _uiState.value.athleteProfile?.ftp ?: return null

        return mapOf(
            "Zone 1 (Récupération)" to (ftp * 0.50).toInt(),
            "Zone 2 (Endurance)" to (ftp * 0.70).toInt(),
            "Zone 3 (Tempo)" to (ftp * 0.85).toInt(),
            "Zone 4 (Seuil)" to (ftp * 1.00).toInt(),
            "Zone 5 (VO2max)" to (ftp * 1.10).toInt(),
            "Zone 6 (Anaérobie)" to (ftp * 1.25).toInt()
        )
    }

    /**
     * Récupère le ton de l'IA selon le genre
     *
     * @return Description du ton utilisé
     *
     * User Story (context.json) :
     * "L'IA adapte son ton : Empathique/Bienveillant si je suis une femme,
     *  Analytique/Factuel si je suis un homme."
     */
    fun getAITone(): String {
        return when (_uiState.value.athleteProfile?.gender) {
            Gender.FEMALE -> "Empathique et Bienveillant"
            Gender.MALE -> "Analytique et Factuel"
            null -> "Non défini"
        }
    }

    /**
     * Efface l'erreur affichée
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * État de l'UI pour l'écran Profile
 */
data class ProfileUiState(
    val athleteProfile: AthleteProfileEntity? = null,
    val equipmentConfig: EquipmentConfigEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
