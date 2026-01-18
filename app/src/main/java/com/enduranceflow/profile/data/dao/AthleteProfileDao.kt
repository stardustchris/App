package com.enduranceflow.profile.data.dao

import androidx.room.*
import com.enduranceflow.profile.data.entity.AthleteProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) pour AthleteProfile
 *
 * Interface de communication avec la table athlete_profile
 * Toutes les opérations sont asynchrones (suspend/Flow)
 *
 * Flow = Stream réactif : l'UI se met à jour automatiquement
 * quand les données changent en base
 */
@Dao
interface AthleteProfileDao {

    /**
     * Récupère le profil de l'athlète (un seul profil, id=1)
     *
     * @return Flow<AthleteProfileEntity?> - null si pas encore créé
     *
     * Flow = Observable : l'UI est notifiée automatiquement
     * quand le profil change (ex: après calibration VMA/FTP)
     */
    @Query("SELECT * FROM athlete_profile WHERE id = 1")
    fun getProfile(): Flow<AthleteProfileEntity?>

    /**
     * Insère ou met à jour le profil
     *
     * OnConflictStrategy.REPLACE : Si existe déjà, on écrase
     *
     * suspend = Fonction asynchrone (coroutine)
     * Ne bloque pas l'UI pendant l'écriture
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: AthleteProfileEntity)

    /**
     * Met à jour uniquement la VMA (après test de calibration)
     *
     * @param vma Nouvelle valeur de VMA en km/h
     * @param updatedAt Timestamp de mise à jour
     */
    @Query("UPDATE athlete_profile SET vma = :vma, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateVMA(vma: Double, updatedAt: Long = System.currentTimeMillis())

    /**
     * Met à jour uniquement le FTP (après test de calibration)
     *
     * @param ftp Nouvelle valeur de FTP en Watts
     * @param updatedAt Timestamp de mise à jour
     */
    @Query("UPDATE athlete_profile SET ftp = :ftp, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateFTP(ftp: Int, updatedAt: Long = System.currentTimeMillis())

    /**
     * Met à jour uniquement la FC Max
     *
     * @param maxHR Nouvelle valeur de FC Max en bpm
     * @param updatedAt Timestamp de mise à jour
     */
    @Query("UPDATE athlete_profile SET maxHR = :maxHR, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateMaxHR(maxHR: Int, updatedAt: Long = System.currentTimeMillis())

    /**
     * Vérifie si le profil existe
     *
     * @return true si le profil a été créé
     */
    @Query("SELECT COUNT(*) > 0 FROM athlete_profile WHERE id = 1")
    suspend fun profileExists(): Boolean

    /**
     * Supprime le profil (pour debug/reset uniquement)
     */
    @Query("DELETE FROM athlete_profile")
    suspend fun deleteProfile()
}
