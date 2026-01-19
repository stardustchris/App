package com.enduranceflow.di

import android.content.Context
import com.enduranceflow.ai.data.MistralEngine
import com.enduranceflow.ai.data.GeminiNanoEngine
import com.enduranceflow.ai.domain.AIEngine
import com.enduranceflow.ai.domain.AIEngineFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour l'injection de dépendances de l'IA
 *
 * Tech Stack Rule (context.json) :
 * "Dependency Injection: Hilt"
 *
 * Fournit (Provides) :
 * 1. GeminiNanoEngine (IA locale)
 * 2. MistralEngine (IA cloud - Mistral AI français 🇫🇷)
 * 3. AIEngineFactory (sélection automatique)
 *
 * @Singleton : Instances uniques pour toute l'app (économie mémoire)
 * @InstallIn(SingletonComponent::class) : Durée de vie = celle de l'app
 */
@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    /**
     * Fournit l'instance de GeminiNanoEngine (IA locale)
     *
     * @param context Context Android pour accès aux ressources
     * @return Instance singleton de GeminiNanoEngine
     */
    @Provides
    @Singleton
    fun provideGeminiNanoEngine(
        @ApplicationContext context: Context
    ): GeminiNanoEngine {
        return GeminiNanoEngine(context)
    }

    /**
     * Fournit l'instance de MistralEngine (IA cloud)
     *
     * @return Instance singleton de MistralEngine
     */
    @Provides
    @Singleton
    fun provideMistralEngine(): MistralEngine {
        return MistralEngine()
    }

    /**
     * Fournit l'instance de AIEngineFactory (Strategy Pattern)
     *
     * La factory choisit automatiquement entre Nano et Mistral
     *
     * @param nanoEngine Instance de GeminiNanoEngine
     * @param mistralEngine Instance de MistralEngine
     * @return Instance singleton de AIEngineFactory
     */
    @Provides
    @Singleton
    fun provideAIEngineFactory(
        nanoEngine: GeminiNanoEngine,
        mistralEngine: MistralEngine
    ): AIEngineFactory {
        return AIEngineFactory(nanoEngine, mistralEngine)
    }
}
