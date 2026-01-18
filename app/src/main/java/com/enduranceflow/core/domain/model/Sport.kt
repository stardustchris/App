package com.enduranceflow.core.domain.model

/**
 * Types de sport supportés par EnduranceFlow
 *
 * Chaque sport a ses propres métriques :
 * - RUNNING : VMA, Allure (min/km), FC
 * - CYCLING : FTP, Puissance (Watts), FC
 */
enum class Sport {
    RUNNING,
    CYCLING
}
