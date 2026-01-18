package com.enduranceflow.profile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room : Configuration de l'équipement
 *
 * Table : equipment_config
 *
 * Détermine les métriques disponibles pour les séances :
 * - Capteur de puissance vélo → Permet les cibles en POWER (Watts)
 * - Pas de capteur → Fallback sur HEART_RATE (FC)
 *
 * Core Entity (context.json) :
 * "EquipmentConfig (HasPowerMeter: Boolean)"
 *
 * Avoid List (context.json) :
 * "Ne pas proposer de Watts en vélo si l'utilisateur n'a pas de
 *  capteur de puissance (Fallback sur Fréquence Cardiaque)."
 *
 * Note : Un seul config par utilisateur (id = 1)
 */
@Entity(tableName = "equipment_config")
data class EquipmentConfigEntity(
    @PrimaryKey
    val id: Int = 1,

    /**
     * L'utilisateur possède-t-il un capteur de puissance pour le vélo ?
     *
     * true → Séances vélo avec cibles en POWER (Watts)
     * false → Séances vélo avec cibles en HEART_RATE (FC)
     */
    val hasPowerMeter: Boolean = false,

    /**
     * L'utilisateur possède-t-il un tapis de course ?
     * Influence les séances en mode Indoor
     */
    val hasTreadmill: Boolean = false,

    /**
     * L'utilisateur possède-t-il un home trainer (vélo indoor) ?
     */
    val hasHomeTrainer: Boolean = false,

    /**
     * Timestamp de création
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Timestamp de dernière mise à jour
     */
    val updatedAt: Long = System.currentTimeMillis()
)
