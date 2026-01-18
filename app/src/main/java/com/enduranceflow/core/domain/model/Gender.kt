package com.enduranceflow.core.domain.model

/**
 * Genre de l'athlète
 *
 * Utilisé pour adapter le ton de l'IA :
 * - FEMALE : Ton empathique/bienveillant
 * - MALE : Ton analytique/factuel
 *
 * User Story (context.json) :
 * "L'IA adapte son ton : Empathique/Bienveillant si je suis une femme,
 *  Analytique/Factuel si je suis un homme."
 */
enum class Gender {
    FEMALE,
    MALE
}
