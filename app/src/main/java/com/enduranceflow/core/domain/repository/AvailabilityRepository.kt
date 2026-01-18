package com.enduranceflow.core.domain.repository

import com.enduranceflow.core.data.dao.AvailabilitySlotDao
import com.enduranceflow.core.data.entity.AvailabilitySlotEntity
import com.enduranceflow.core.domain.model.Sport
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour la gestion des créneaux de disponibilité
 *
 * Responsabilités :
 * - Gestion des disponibilités hebdomadaires de l'athlète
 * - Activation/désactivation temporaire de créneaux
 * - Fournir les créneaux à l'IA pour la génération de séances
 *
 * Core Entity (context.json) : "AvailabilitySlot (DayOfWeek, Sport)"
 *
 * Injecté par Hilt dans les ViewModels
 */
@Singleton
class AvailabilityRepository @Inject constructor(
    private val availabilitySlotDao: AvailabilitySlotDao
) {

    /**
     * Récupère tous les créneaux actifs
     * Triés par jour de la semaine
     *
     * @return Flow<List<AvailabilitySlotEntity>>
     */
    fun getAllActiveSlots(): Flow<List<AvailabilitySlotEntity>> {
        return availabilitySlotDao.getAllActiveSlots()
    }

    /**
     * Récupère les créneaux d'un jour spécifique
     *
     * @param dayOfWeek Jour de la semaine (MONDAY, TUESDAY, etc.)
     */
    fun getSlotsByDay(dayOfWeek: DayOfWeek): Flow<List<AvailabilitySlotEntity>> {
        return availabilitySlotDao.getSlotsByDay(dayOfWeek)
    }

    /**
     * Récupère les créneaux pour un sport spécifique
     *
     * @param sport RUNNING ou CYCLING
     */
    fun getSlotsBySport(sport: Sport): Flow<List<AvailabilitySlotEntity>> {
        return availabilitySlotDao.getSlotsBySport(sport)
    }

    /**
     * Ajoute un nouveau créneau de disponibilité
     *
     * @param dayOfWeek Jour de la semaine
     * @param sport Sport prévu (RUNNING/CYCLING)
     * @param durationMinutes Durée disponible en minutes
     * @param preferIndoor Préférence pour l'indoor ?
     *
     * @return L'ID du créneau créé
     *
     * Exemple : Lundi, RUNNING, 60 min, Outdoor
     */
    suspend fun addSlot(
        dayOfWeek: DayOfWeek,
        sport: Sport,
        durationMinutes: Int = 60,
        preferIndoor: Boolean = false
    ): Long {
        val slot = AvailabilitySlotEntity(
            dayOfWeek = dayOfWeek,
            sport = sport,
            durationMinutes = durationMinutes,
            preferIndoor = preferIndoor,
            isActive = true
        )
        return availabilitySlotDao.insertSlot(slot)
    }

    /**
     * Crée un planning hebdomadaire par défaut
     * (Appelé lors du premier démarrage)
     *
     * Exemple :
     * - Lundi : RUNNING, 60 min
     * - Mercredi : RUNNING, 45 min
     * - Vendredi : CYCLING, 60 min
     * - Samedi : RUNNING, 90 min (sortie longue)
     */
    suspend fun createDefaultWeeklyPlan() {
        val defaultSlots = listOf(
            AvailabilitySlotEntity(
                dayOfWeek = DayOfWeek.MONDAY,
                sport = Sport.RUNNING,
                durationMinutes = 60,
                preferIndoor = false
            ),
            AvailabilitySlotEntity(
                dayOfWeek = DayOfWeek.WEDNESDAY,
                sport = Sport.RUNNING,
                durationMinutes = 45,
                preferIndoor = false
            ),
            AvailabilitySlotEntity(
                dayOfWeek = DayOfWeek.FRIDAY,
                sport = Sport.CYCLING,
                durationMinutes = 60,
                preferIndoor = false
            ),
            AvailabilitySlotEntity(
                dayOfWeek = DayOfWeek.SATURDAY,
                sport = Sport.RUNNING,
                durationMinutes = 90,
                preferIndoor = false
            )
        )
        availabilitySlotDao.insertSlots(defaultSlots)
    }

    /**
     * Active ou désactive un créneau
     *
     * @param slotId ID du créneau
     * @param isActive true = actif, false = désactivé
     *
     * Permet de suspendre un créneau temporairement (blessure, vacances)
     * sans le supprimer
     */
    suspend fun toggleSlot(slotId: Long, isActive: Boolean) {
        availabilitySlotDao.toggleSlot(slotId, isActive)
    }

    /**
     * Supprime un créneau définitivement
     *
     * @param slotId ID du créneau à supprimer
     */
    suspend fun deleteSlot(slotId: Long) {
        availabilitySlotDao.deleteSlot(slotId)
    }

    /**
     * Compte le nombre de créneaux actifs dans la semaine
     *
     * @return Nombre de séances par semaine
     *
     * Utilisé pour vérifier que l'utilisateur a bien configuré
     * ses disponibilités avant la génération de séances
     */
    suspend fun getActiveSlotCount(): Int {
        return availabilitySlotDao.getActiveSlotCount()
    }

    /**
     * Vérifie si l'utilisateur a au moins un créneau configuré
     *
     * @return true si au moins 1 créneau actif
     */
    suspend fun hasConfiguredSlots(): Boolean {
        return getActiveSlotCount() > 0
    }
}
