package com.enduranceflow.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.enduranceflow.core.data.dao.AvailabilitySlotDao
import com.enduranceflow.core.data.entity.AvailabilitySlotEntity
import com.enduranceflow.profile.data.dao.AthleteProfileDao
import com.enduranceflow.profile.data.dao.EquipmentConfigDao
import com.enduranceflow.profile.data.entity.AthleteProfileEntity
import com.enduranceflow.profile.data.entity.EquipmentConfigEntity
import com.enduranceflow.workout.data.dao.DailyWorkoutDao
import com.enduranceflow.workout.data.dao.SessionFeedbackDao
import com.enduranceflow.workout.data.entity.DailyWorkoutEntity
import com.enduranceflow.workout.data.entity.SessionFeedbackEntity

/**
 * Base de données principale de EnduranceFlow
 *
 * Architecture : Offline-First (context.json)
 * - Toutes les données sont stockées localement en SQLite via Room
 * - Synchronisation avec Health Connect pour les métriques
 * - Aucune donnée personnelle envoyée au Cloud (Privacy-First)
 *
 * Entités incluses :
 * 1. AthleteProfileEntity - Profil athlète (VMA, FTP, Gender, Age, Weight, Height, etc.)
 * 2. EquipmentConfigEntity - Configuration équipement (capteurs)
 * 3. DailyWorkoutEntity - Séances d'entraînement
 * 4. AvailabilitySlotEntity - Créneaux de disponibilité
 * 5. SessionFeedbackEntity - Feedbacks post-séance (RPE)
 *
 * Version : 2 (ajout de age, weight, height à AthleteProfile)
 * Si modification du schéma → Incrémenter version + Migration
 */
@Database(
    entities = [
        AthleteProfileEntity::class,
        EquipmentConfigEntity::class,
        DailyWorkoutEntity::class,
        AvailabilitySlotEntity::class,
        SessionFeedbackEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class EnduranceFlowDatabase : RoomDatabase() {

    // DAOs (Data Access Objects)
    abstract fun athleteProfileDao(): AthleteProfileDao
    abstract fun equipmentConfigDao(): EquipmentConfigDao
    abstract fun dailyWorkoutDao(): DailyWorkoutDao
    abstract fun availabilitySlotDao(): AvailabilitySlotDao
    abstract fun sessionFeedbackDao(): SessionFeedbackDao

    companion object {
        const val DATABASE_NAME = "endurance_flow_db"
    }
}
