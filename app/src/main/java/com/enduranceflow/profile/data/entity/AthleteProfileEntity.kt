package com.enduranceflow.profile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enduranceflow.core.domain.model.Gender

/**
 * Entité Room : Profil de l'athlète
 *
 * Table : athlete_profile
 *
 * Données physiologiques principales :
 * - VMA (Vitesse Maximale Aérobie) en km/h
 * - FTP (Functional Threshold Power) en Watts
 * - MaxHR (Fréquence Cardiaque Maximale) en bpm
 * - Gender (pour adaptation du ton de l'IA)
 * - Age, Poids, Taille (pour personnalisation)
 *
 * Core Entity (context.json) :
 * "AthleteProfile (Gender, VMA, FTP, MaxHR, Age, Weight, Height)"
 *
 * Note : Un seul profil par utilisateur (id = 1)
 */
@Entity(tableName = "athlete_profile")
data class AthleteProfileEntity(
    @PrimaryKey
    val id: Int = 1,

    /**
     * Genre de l'athlète
     * Influence le ton de l'IA (empathique vs analytique)
     */
    val gender: Gender,

    /**
     * VMA (Vitesse Maximale Aérobie) en km/h
     * Exemple : 18.5 km/h
     * null si pas encore testé → déclenche la calibration
     */
    val vma: Double?,

    /**
     * FTP (Functional Threshold Power) en Watts
     * Exemple : 280 W
     * null si pas encore testé → déclenche la calibration
     */
    val ftp: Int?,

    /**
     * Fréquence Cardiaque Maximale en bpm
     * Exemple : 195 bpm
     * null si pas encore déterminée
     */
    val maxHR: Int?,

    /**
     * Âge de l'athlète en années
     * Exemple : 35
     * null si non renseigné
     */
    val age: Int? = null,

    /**
     * Poids de l'athlète en kilogrammes
     * Exemple : 75.5
     * null si non renseigné
     */
    val weight: Double? = null,

    /**
     * Taille de l'athlète en centimètres
     * Exemple : 175
     * null si non renseigné
     */
    val height: Int? = null,

    /**
     * Timestamp de création du profil
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Timestamp de dernière mise à jour
     */
    val updatedAt: Long = System.currentTimeMillis()
)
