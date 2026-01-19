package com.enduranceflow.di

import com.enduranceflow.ai.data.MistralEngine
import com.enduranceflow.ai.domain.AIEngineFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour l'injection de dépendances de l'IA
 *
 * Tech Stack Rule (context.json) :
 * "Dependency Injection: Hilt"
 *
 * Fournit (Provides) :
 * 1. MistralEngine (IA cloud - Mistral AI français 🇫🇷)
 * 2. AIEngineFactory (gère l'instance Mistral)
 *
 * Simplifié : Utilise uniquement Mistral AI comme moteur IA
 * (suppression de Gemini Nano pour simplifier le code)
 *
 * @Singleton : Instances uniques pour toute l'app (économie mémoire)
 * @InstallIn(SingletonComponent::class) : Durée de vie = celle de l'app
 */
@Module
@InstallIn(SingletonComponent::class)
object AIModule {

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
     * Fournit l'instance de AIEngineFactory
     *
     * La factory gère l'instance unique de Mistral AI
     *
     * @param mistralEngine Instance de MistralEngine
     * @return Instance singleton de AIEngineFactory
     */
    @Provides
    @Singleton
    fun provideAIEngineFactory(
        mistralEngine: MistralEngine
    ): AIEngineFactory {
        return AIEngineFactory(mistralEngine)
    }
}
