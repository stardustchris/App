package com.enduranceflow.profile.domain.repository

import com.enduranceflow.core.domain.model.Gender
import com.enduranceflow.profile.data.dao.AthleteProfileDao
import com.enduranceflow.profile.data.dao.EquipmentConfigDao
import com.enduranceflow.profile.data.entity.AthleteProfileEntity
import com.enduranceflow.profile.data.entity.EquipmentConfigEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour la gestion du profil athlète
 *
 * Responsabilités :
 * - Gestion du profil AthleteProfile (VMA, FTP, Gender, MaxHR)
 * - Gestion de la configuration équipement (capteurs)
 * - Logique de validation des données
 * - Détection si calibration nécessaire (VMA/FTP null)
 *
 * Injecté par Hilt dans les ViewModels
 */
@Singleton
class ProfileRepository @Inject constructor(
    private val athleteProfileDao: AthleteProfileDao,
    private val equipmentConfigDao: EquipmentConfigDao
) {

    // ========== AthleteProfile ==========

    /**
     * Récupère le profil de l'athlète (Flow réactif)
     *
     * @return Flow<AthleteProfileEntity?> - null si profil pas encore créé
     */
    fun getProfile(): Flow<AthleteProfileEntity?> {
        return athleteProfileDao.getProfile()
    }

    /**
     * Crée ou met à jour le profil complet
     *
     * @param gender Genre de l'athlète (FEMALE/MALE)
     * @param vma VMA en km/h (nullable si pas encore testé)
     * @param ftp FTP en Watts (nullable si pas encore testé)
     * @param maxHR FC Max en bpm (nullable)
     */
    suspend fun saveProfile(
        gender: Gender,
        vma: Double? = null,
        ftp: Int? = null,
        maxHR: Int? = null
    ) {
        val profile = AthleteProfileEntity(
            gender = gender,
            vma = vma,
            ftp = ftp,
            maxHR = maxHR,
            updatedAt = System.currentTimeMillis()
        )
        athleteProfileDao.insertOrUpdateProfile(profile)
    }

    /**
     * Met à jour uniquement la VMA (après test de calibration)
     *
     * @param vma Nouvelle valeur de VMA en km/h
     */
    suspend fun updateVMA(vma: Double) {
        athleteProfileDao.updateVMA(vma)
    }

    /**
     * Met à jour uniquement le FTP (après test de calibration)
     *
     * @param ftp Nouvelle valeur de FTP en Watts
     */
    suspend fun updateFTP(ftp: Int) {
        athleteProfileDao.updateFTP(ftp)
    }

    /**
     * Met à jour uniquement la FC Max
     *
     * @param maxHR Nouvelle valeur de FC Max en bpm
     */
    suspend fun updateMaxHR(maxHR: Int) {
        athleteProfileDao.updateMaxHR(maxHR)
    }

    /**
     * Vérifie si une calibration est nécessaire
     *
     * @return true si VMA ou FTP sont null
     *
     * User Story (context.json) :
     * "Au démarrage, si je ne connais pas mon niveau (VMA/FTP),
     *  l'app génère une 'Semaine de Calibration' avec des tests."
     *
     * Avoid List (context.json) :
     * "Ne pas bloquer l'utilisateur s'il ne connaît pas sa VMA
     *  (lancer le protocole de test)."
     */
    suspend fun needsCalibration(): Boolean {
        val profile = athleteProfileDao.getProfile()
        // TODO: Récupérer la valeur actuelle et vérifier si vma == null || ftp == null
        // Pour l'instant, on retourne true si le profil n'existe pas
        return !athleteProfileDao.profileExists()
    }

    // ========== EquipmentConfig ==========

    /**
     * Récupère la configuration équipement (Flow réactif)
     *
     * @return Flow<EquipmentConfigEntity?> - null si pas encore créé
     */
    fun getEquipmentConfig(): Flow<EquipmentConfigEntity?> {
        return equipmentConfigDao.getConfig()
    }

    /**
     * Crée ou met à jour la configuration équipement
     *
     * @param hasPowerMeter Capteur de puissance vélo ?
     * @param hasTreadmill Tapis de course ?
     * @param hasHomeTrainer Home trainer vélo ?
     */
    suspend fun saveEquipmentConfig(
        hasPowerMeter: Boolean = false,
        hasTreadmill: Boolean = false,
        hasHomeTrainer: Boolean = false
    ) {
        val config = EquipmentConfigEntity(
            hasPowerMeter = hasPowerMeter,
            hasTreadmill = hasTreadmill,
            hasHomeTrainer = hasHomeTrainer,
            updatedAt = System.currentTimeMillis()
        )
        equipmentConfigDao.insertOrUpdateConfig(config)
    }

    /**
     * Met à jour uniquement le statut du capteur de puissance
     *
     * @param hasPowerMeter true si l'utilisateur possède un capteur
     *
     * Impact : Détermine si les séances vélo utilisent POWER ou HEART_RATE
     */
    suspend fun updatePowerMeter(hasPowerMeter: Boolean) {
        equipmentConfigDao.updatePowerMeter(hasPowerMeter)
    }

    /**
     * Vérifie si l'utilisateur possède un capteur de puissance
     *
     * @return true si capteur disponible
     *
     * Utilisé pour déterminer le TargetType des séances vélo
     */
    suspend fun hasPowerMeter(): Boolean {
        return equipmentConfigDao.hasPowerMeter() ?: false
    }
}
