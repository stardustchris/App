package com.enduranceflow.core.data.dao

import androidx.room.*
import com.enduranceflow.core.data.entity.AvailabilitySlotEntity
import com.enduranceflow.core.domain.model.Sport
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

/**
 * DAO (Data Access Object) pour AvailabilitySlot
 *
 * Gère les créneaux de disponibilité hebdomadaires de l'athlète
 *
 * L'IA utilise ces créneaux pour générer les séances :
 * - Uniquement aux jours définis
 * - Respect de la durée disponible
 * - Respect du sport choisi
 * - Respect de la préférence Indoor/Outdoor
 */
@Dao
interface AvailabilitySlotDao {

    /**
     * Récupère tous les créneaux actifs
     * Triés par jour de la semaine (Lundi → Dimanche)
     *
     * @return Flow<List<AvailabilitySlotEntity>>
     */
    @Query("SELECT * FROM availability_slot WHERE isActive = 1 ORDER BY dayOfWeek ASC")
    fun getAllActiveSlots(): Flow<List<AvailabilitySlotEntity>>

    /**
     * Récupère les créneaux pour un jour spécifique
     *
     * @param dayOfWeek Jour de la semaine (MONDAY, TUESDAY, etc.)
     */
    @Query("SELECT * FROM availability_slot WHERE dayOfWeek = :dayOfWeek AND isActive = 1")
    fun getSlotsByDay(dayOfWeek: DayOfWeek): Flow<List<AvailabilitySlotEntity>>

    /**
     * Récupère les créneaux pour un sport spécifique
     *
     * @param sport RUNNING ou CYCLING
     */
    @Query("SELECT * FROM availability_slot WHERE sport = :sport AND isActive = 1 ORDER BY dayOfWeek ASC")
    fun getSlotsBySport(sport: Sport): Flow<List<AvailabilitySlotEntity>>

    /**
     * Insère un nouveau créneau de disponibilité
     *
     * @param slot Créneau à insérer
     * @return L'ID du créneau créé
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: AvailabilitySlotEntity): Long

    /**
     * Insère plusieurs créneaux en une fois
     * (Utilisé lors de l'initialisation du planning)
     *
     * @param slots Liste des créneaux à insérer
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<AvailabilitySlotEntity>)

    /**
     * Met à jour un créneau existant
     *
     * @param slot Créneau mis à jour
     */
    @Update
    suspend fun updateSlot(slot: AvailabilitySlotEntity)

    /**
     * Active ou désactive un créneau
     *
     * @param slotId ID du créneau
     * @param isActive true = actif, false = désactivé
     *
     * Permet de suspendre un créneau temporairement (blessure, vacances)
     * sans le supprimer
     */
    @Query("UPDATE availability_slot SET isActive = :isActive WHERE id = :slotId")
    suspend fun toggleSlot(slotId: Long, isActive: Boolean)

    /**
     * Supprime un créneau
     *
     * @param slotId ID du créneau à supprimer
     */
    @Query("DELETE FROM availability_slot WHERE id = :slotId")
    suspend fun deleteSlot(slotId: Long)

    /**
     * Supprime tous les créneaux (pour reset)
     */
    @Query("DELETE FROM availability_slot")
    suspend fun deleteAllSlots()

    /**
     * Compte le nombre total de créneaux actifs
     *
     * @return Nombre de créneaux d'entraînement par semaine
     */
    @Query("SELECT COUNT(*) FROM availability_slot WHERE isActive = 1")
    suspend fun getActiveSlotCount(): Int
}
