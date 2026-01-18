package com.enduranceflow.core.domain.model

/**
 * Types de cibles pour les séances d'entraînement
 *
 * Adaptation automatique Indoor/Outdoor :
 * - POWER : Watts (vélo avec capteur de puissance)
 * - PACE : Allure (min/km) pour course outdoor
 * - SPEED : Vitesse (km/h) pour tapis de course indoor
 * - HEART_RATE : Fréquence cardiaque (fallback si pas de capteur puissance)
 *
 * User Story (context.json) :
 * "Je peux basculer une séance en mode 'Intérieur' :
 *  les cibles changent (Vitesse → Tapis, Allure → Watts/Cardio)."
 *
 * Avoid List (context.json) :
 * "Ne pas proposer de Watts en vélo si l'utilisateur n'a pas de capteur
 *  de puissance (Fallback sur Fréquence Cardiaque)."
 */
enum class TargetType {
    POWER,          // Watts (vélo avec capteur)
    PACE,           // Allure (min/km) - Course outdoor
    SPEED,          // Vitesse (km/h) - Tapis indoor
    HEART_RATE      // Fréquence cardiaque (bpm) - Fallback universel
}
