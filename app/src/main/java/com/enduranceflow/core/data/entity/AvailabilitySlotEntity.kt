package com.enduranceflow.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enduranceflow.core.domain.model.Sport
import java.time.DayOfWeek

/**
 * Entité Room : Créneau de disponibilité pour l'entraînement
 *
 * Table : availability_slot
 *
 * Permet à l'utilisateur de définir ses disponibilités hebdomadaires.
 * L'IA génère les séances uniquement sur ces créneaux.
 *
 * Core Entity (context.json) :
 * "AvailabilitySlot (DayOfWeek, Sport)"
 *
 * Exemple :
 * - Lundi : RUNNING (disponible pour la course)
 * - Mardi : CYCLING (disponible pour le vélo)
 * - Mercredi : Repos (pas de slot)
 */
@Entity(tableName = "availability_slot")
data class AvailabilitySlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Jour de la semaine
     * MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
     */
    val dayOfWeek: DayOfWeek,

    /**
     * Sport prévu pour ce créneau
     * RUNNING ou CYCLING
     */
    val sport: Sport,

    /**
     * Durée disponible en minutes
     * Exemple : 60 minutes
     * Permet à l'IA de générer une séance adaptée à la durée
     */
    val durationMinutes: Int = 60,

    /**
     * Préférence Indoor pour ce créneau ?
     * true → L'utilisateur préfère s'entraîner en intérieur ce jour-là
     * false → Outdoor par défaut
     */
    val preferIndoor: Boolean = false,

    /**
     * Créneau actif ?
     * Permet de désactiver temporairement un créneau sans le supprimer
     */
    val isActive: Boolean = true,

    /**
     * Timestamp de création
     */
    val createdAt: Long = System.currentTimeMillis()
)
