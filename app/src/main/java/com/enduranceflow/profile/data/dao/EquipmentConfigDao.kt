package com.enduranceflow.profile.data.dao

import androidx.room.*
import com.enduranceflow.profile.data.entity.EquipmentConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) pour EquipmentConfig
 *
 * Gère la configuration de l'équipement de l'athlète
 * (capteur de puissance, tapis de course, home trainer)
 *
 * Utilisé pour déterminer les métriques disponibles
 * et adapter les séances en conséquence.
 */
@Dao
interface EquipmentConfigDao {

    /**
     * Récupère la configuration équipement (une seule config, id=1)
     *
     * @return Flow<EquipmentConfigEntity?> - null si pas encore créée
     *
     * Flow = Observable pour mise à jour automatique de l'UI
     */
    @Query("SELECT * FROM equipment_config WHERE id = 1")
    fun getConfig(): Flow<EquipmentConfigEntity?>

    /**
     * Insère ou met à jour la configuration équipement
     *
     * OnConflictStrategy.REPLACE : Si existe, on écrase
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: EquipmentConfigEntity)

    /**
     * Met à jour uniquement le statut du capteur de puissance
     *
     * @param hasPowerMeter true si l'utilisateur possède un capteur
     *
     * Impact : Si true, les séances vélo auront des cibles en POWER (Watts)
     *          Si false, fallback sur HEART_RATE
     *
     * Avoid List (context.json) :
     * "Ne pas proposer de Watts en vélo si l'utilisateur n'a pas de
     *  capteur de puissance (Fallback sur Fréquence Cardiaque)."
     */
    @Query("UPDATE equipment_config SET hasPowerMeter = :hasPowerMeter, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updatePowerMeter(hasPowerMeter: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * Met à jour le statut du tapis de course
     *
     * @param hasTreadmill true si l'utilisateur possède un tapis
     */
    @Query("UPDATE equipment_config SET hasTreadmill = :hasTreadmill, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateTreadmill(hasTreadmill: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * Met à jour le statut du home trainer
     *
     * @param hasHomeTrainer true si l'utilisateur possède un home trainer
     */
    @Query("UPDATE equipment_config SET hasHomeTrainer = :hasHomeTrainer, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateHomeTrainer(hasHomeTrainer: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * Vérifie si l'utilisateur possède un capteur de puissance
     * (requête synchrone pour usage dans la logique métier)
     *
     * @return true si capteur de puissance disponible
     */
    @Query("SELECT hasPowerMeter FROM equipment_config WHERE id = 1")
    suspend fun hasPowerMeter(): Boolean?

    /**
     * Supprime la configuration (pour debug/reset)
     */
    @Query("DELETE FROM equipment_config")
    suspend fun deleteConfig()
}
