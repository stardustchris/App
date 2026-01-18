package com.enduranceflow.workout.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enduranceflow.core.domain.model.Sport
import com.enduranceflow.core.domain.model.TargetType

/**
 * Entité Room : Séance d'entraînement quotidienne
 *
 * Table : daily_workout
 *
 * Représente une séance générée par l'IA adaptée au profil de l'athlète.
 * Peut basculer entre mode Indoor et Outdoor.
 *
 * Core Entity (context.json) :
 * "DailyWorkout (TargetType: POWER, PACE, HR, SPEED)"
 *
 * User Story (context.json) :
 * "Je peux basculer une séance en mode 'Intérieur' :
 *  les cibles changent (Vitesse → Tapis, Allure → Watts/Cardio)."
 */
@Entity(tableName = "daily_workout")
data class DailyWorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Date de la séance (format : YYYY-MM-DD)
     * Exemple : "2026-01-20"
     */
    val date: String,

    /**
     * Sport concerné (Course ou Vélo)
     */
    val sport: Sport,

    /**
     * Titre de la séance
     * Exemple : "VMA Courte", "Seuil Vélo", "Sortie Longue"
     */
    val title: String,

    /**
     * Description détaillée de la séance
     * Exemple : "8x400m @ 100% VMA - Récup 1'30"
     */
    val description: String,

    /**
     * Type de cible pour cette séance
     * POWER, PACE, SPEED, ou HEART_RATE
     *
     * Déterminé automatiquement selon :
     * - Le sport (Running/Cycling)
     * - Le mode (Indoor/Outdoor)
     * - L'équipement disponible (capteur de puissance)
     */
    val targetType: TargetType,

    /**
     * Mode Indoor activé ?
     *
     * true → Séance en intérieur (tapis, home trainer)
     * false → Séance en extérieur (route, piste)
     *
     * Impact :
     * - Running Outdoor : PACE (allure min/km)
     * - Running Indoor : SPEED (vitesse km/h sur tapis)
     * - Cycling Outdoor avec capteur : POWER (Watts)
     * - Cycling Indoor sans capteur : HEART_RATE (bpm)
     */
    val isIndoor: Boolean = false,

    /**
     * Durée estimée de la séance en minutes
     * Exemple : 45 minutes
     */
    val durationMinutes: Int,

    /**
     * Valeur cible principale
     * Exemple : 18.5 km/h (VMA), 280 W (FTP), 165 bpm (FC)
     */
    val targetValue: Double,

    /**
     * Séance complétée ?
     */
    val isCompleted: Boolean = false,

    /**
     * Timestamp de création
     */
    val createdAt: Long = System.currentTimeMillis()
)
