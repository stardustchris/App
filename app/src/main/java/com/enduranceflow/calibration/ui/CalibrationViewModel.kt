package com.enduranceflow.calibration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enduranceflow.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran Calibration
 *
 * Responsabilités :
 * - Guider l'utilisateur dans les tests VMA/FTP
 * - Enregistrer les résultats de calibration
 * - Calculer les zones d'entraînement
 * - Déterminer quand la calibration est complète
 *
 * User Story (context.json) :
 * "Au démarrage, si je ne connais pas mon niveau (VMA/FTP),
 *  l'app génère une 'Semaine de Calibration' avec des tests."
 *
 * Injecté par Hilt (@HiltViewModel)
 */
@HiltViewModel
class CalibrationViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // État de l'UI
    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    init {
        loadCalibrationStatus()
    }

    /**
     * Charge l'état actuel de la calibration
     * (Vérifie quels tests ont déjà été réalisés)
     */
    private fun loadCalibrationStatus() {
        viewModelScope.launch {
            profileRepository.getProfile().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    vmaCompleted = profile?.vma != null,
                    ftpCompleted = profile?.ftp != null,
                    currentVMA = profile?.vma,
                    currentFTP = profile?.ftp
                )
            }
        }
    }

    /**
     * Enregistre le résultat du test VMA
     *
     * @param vma Résultat du test VMA en km/h
     *
     * Exemple : 18.5 km/h
     *
     * Calcul des zones d'entraînement :
     * - Zone 1 (Récupération) : 60-70% VMA
     * - Zone 2 (Endurance fondamentale) : 70-80% VMA
     * - Zone 3 (Seuil) : 80-90% VMA
     * - Zone 4 (VMA) : 90-100% VMA
     * - Zone 5 (Anaérobie) : 100-110% VMA
     */
    fun submitVMATest(vma: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                profileRepository.updateVMA(vma)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    vmaCompleted = true,
                    currentVMA = vma
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors de l'enregistrement de la VMA: ${e.message}"
                )
            }
        }
    }

    /**
     * Enregistre le résultat du test FTP
     *
     * @param ftp Résultat du test FTP en Watts
     *
     * Exemple : 280 W
     *
     * Calcul des zones d'entraînement :
     * - Zone 1 (Récupération active) : < 55% FTP
     * - Zone 2 (Endurance) : 56-75% FTP
     * - Zone 3 (Tempo) : 76-90% FTP
     * - Zone 4 (Seuil) : 91-105% FTP
     * - Zone 5 (VO2max) : 106-120% FTP
     * - Zone 6 (Anaérobie) : > 121% FTP
     */
    fun submitFTPTest(ftp: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                profileRepository.updateFTP(ftp)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    ftpCompleted = true,
                    currentFTP = ftp
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors de l'enregistrement du FTP: ${e.message}"
                )
            }
        }
    }

    /**
     * Vérifie si la calibration est complète
     *
     * @return true si VMA ET FTP ont été renseignés
     */
    fun isCalibrationComplete(): Boolean {
        return _uiState.value.vmaCompleted && _uiState.value.ftpCompleted
    }

    /**
     * Calcule la zone d'entraînement VMA
     *
     * @param percentage Pourcentage de VMA (ex: 85 pour 85% VMA)
     * @return Vitesse cible en km/h, ou null si VMA non calibrée
     */
    fun calculateVMAZone(percentage: Int): Double? {
        return _uiState.value.currentVMA?.let { vma ->
            vma * (percentage / 100.0)
        }
    }

    /**
     * Calcule la zone d'entraînement FTP
     *
     * @param percentage Pourcentage de FTP (ex: 90 pour 90% FTP)
     * @return Puissance cible en Watts, ou null si FTP non calibré
     */
    fun calculateFTPZone(percentage: Int): Int? {
        return _uiState.value.currentFTP?.let { ftp ->
            (ftp * (percentage / 100.0)).toInt()
        }
    }
}

/**
 * État de l'UI pour l'écran Calibration
 */
data class CalibrationUiState(
    val vmaCompleted: Boolean = false,
    val ftpCompleted: Boolean = false,
    val currentVMA: Double? = null,
    val currentFTP: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
