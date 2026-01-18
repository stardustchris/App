package com.enduranceflow.di

import android.content.Context
import androidx.room.Room
import com.enduranceflow.core.data.dao.AvailabilitySlotDao
import com.enduranceflow.core.data.database.EnduranceFlowDatabase
import com.enduranceflow.profile.data.dao.AthleteProfileDao
import com.enduranceflow.profile.data.dao.EquipmentConfigDao
import com.enduranceflow.workout.data.dao.DailyWorkoutDao
import com.enduranceflow.workout.data.dao.SessionFeedbackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour la base de données
 *
 * Dependency Injection (context.json) : "Hilt"
 *
 * Ce module fournit (Provides) :
 * 1. La base de données EnduranceFlowDatabase (singleton)
 * 2. Tous les DAOs nécessaires
 *
 * @Singleton : Une seule instance de la base pour toute l'app
 * @InstallIn(SingletonComponent::class) : Durée de vie = celle de l'app
 *
 * Avantage de Hilt :
 * - Pas besoin de passer la base manuellement partout
 * - Injection automatique dans les Repositories et ViewModels
 * - Facilite les tests (on peut mocker la base)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Fournit la base de données principale
     *
     * @param context Context Android (fourni automatiquement par Hilt)
     * @return Instance singleton de EnduranceFlowDatabase
     *
     * Room.databaseBuilder :
     * - Crée le fichier SQLite "endurance_flow_db"
     * - Applique les migrations si la version change
     * - Utilise les TypeConverters pour les enums
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): EnduranceFlowDatabase {
        return Room.databaseBuilder(
            context,
            EnduranceFlowDatabase::class.java,
            EnduranceFlowDatabase.DATABASE_NAME
        )
            // Stratégie si migration manquante : Supprimer et recréer
            // ⚠️ En production, toujours fournir des migrations !
            .fallbackToDestructiveMigration()
            .build()
    }

    // ========== Provides DAOs ==========

    @Provides
    @Singleton
    fun provideAthleteProfileDao(database: EnduranceFlowDatabase): AthleteProfileDao {
        return database.athleteProfileDao()
    }

    @Provides
    @Singleton
    fun provideEquipmentConfigDao(database: EnduranceFlowDatabase): EquipmentConfigDao {
        return database.equipmentConfigDao()
    }

    @Provides
    @Singleton
    fun provideDailyWorkoutDao(database: EnduranceFlowDatabase): DailyWorkoutDao {
        return database.dailyWorkoutDao()
    }

    @Provides
    @Singleton
    fun provideAvailabilitySlotDao(database: EnduranceFlowDatabase): AvailabilitySlotDao {
        return database.availabilitySlotDao()
    }

    @Provides
    @Singleton
    fun provideSessionFeedbackDao(database: EnduranceFlowDatabase): SessionFeedbackDao {
        return database.sessionFeedbackDao()
    }
}
