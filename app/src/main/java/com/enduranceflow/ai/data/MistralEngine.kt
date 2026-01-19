package com.enduranceflow.ai.data

import com.enduranceflow.BuildConfig
import com.enduranceflow.ai.domain.AIEngine
import com.enduranceflow.core.domain.model.Gender
import com.enduranceflow.core.domain.model.Sport
import com.enduranceflow.core.domain.model.TargetType
import com.enduranceflow.workout.data.entity.DailyWorkoutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * Implémentation Mistral AI - IA cloud française 🇫🇷
 *
 * Avantages :
 * - Entreprise française (excellente compréhension du français)
 * - Très rapide et performant
 * - Quota gratuit généreux
 * - API REST simple et fiable
 *
 * Inconvénients :
 * - Nécessite connexion Internet
 * - Latence réseau (100-300ms)
 *
 * IMPORTANT : Seules les données anonymes (VMA, FTP, RPE) sont envoyées.
 *             Aucune donnée personnelle identifiable (nom, email, etc.)
 */
class MistralEngine @Inject constructor() : AIEngine {

    // Clé API Mistral chargée depuis local.properties via BuildConfig
    private val apiKey = BuildConfig.MISTRAL_API_KEY

    // URL de l'API Mistral
    private val apiUrl = "https://api.mistral.ai/v1/chat/completions"

    // Modèle à utiliser (mistral-small-latest = bon équilibre qualité/vitesse)
    private val modelName = "mistral-small-latest"

    override fun getEngineType(): AIEngine.EngineType {
        // On réutilise GEMINI_FLASH pour la compatibilité
        // (peut être renommé en CLOUD_AI plus tard)
        return AIEngine.EngineType.GEMINI_FLASH
    }

    /**
     * Vérifie la disponibilité de Mistral AI
     *
     * Conditions requises :
     * - Connexion Internet active
     * - Clé API valide
     */
    override suspend fun isAvailable(): Boolean {
        return apiKey != "YOUR_MISTRAL_API_KEY" && apiKey.isNotEmpty()
    }

    /**
     * Initialise le client Mistral AI
     */
    override suspend fun initialize(): Boolean {
        return try {
            // Vérifier que la clé API est configurée
            isAvailable()
        } catch (e: Exception) {
            android.util.Log.e("MistralEngine", "Initialization failed: ${e.message}", e)
            false
        }
    }

    override suspend fun generateWeeklyWorkouts(
        vma: Double?,
        ftp: Int?,
        gender: Gender,
        hasPowerMeter: Boolean,
        availableDays: List<String>,
        averageRPE: Double?,
        isFatigued: Boolean,
        needsProgression: Boolean
    ): List<DailyWorkoutEntity> {
        val prompt = buildWorkoutGenerationPrompt(
            vma, ftp, gender, hasPowerMeter,
            availableDays, averageRPE, isFatigued, needsProgression
        )

        return try {
            android.util.Log.d("MistralEngine", "Generating workouts with prompt: ${prompt.take(200)}...")

            val response = callMistralAPI(prompt)

            android.util.Log.d("MistralEngine", "Response received: ${response.take(200)}...")

            // Parser la réponse de l'IA et créer les entités DailyWorkoutEntity
            parseWorkoutsFromResponse(response, availableDays)

        } catch (e: Exception) {
            // Erreur API → Logger l'erreur et retourner liste vide
            android.util.Log.e("MistralEngine", "Error generating workouts: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun generateMotivationalMessage(
        gender: Gender,
        workoutTitle: String,
        isBeforeWorkout: Boolean
    ): String {
        val tone = if (gender == Gender.FEMALE) {
            "empathique et bienveillant"
        } else {
            "analytique et factuel"
        }

        val moment = if (isBeforeWorkout) "avant" else "après"

        val prompt = """
            Tu es un coach d'endurance avec un ton $tone.
            ${if (isBeforeWorkout) "Motive" else "Félicite"} l'athlète $moment la séance : "$workoutTitle".
            Réponse en 1-2 phrases maximum.
        """.trimIndent()

        return try {
            val response = callMistralAPI(prompt)
            response.ifEmpty { getDefaultMessage(gender, isBeforeWorkout, workoutTitle) }
        } catch (e: Exception) {
            getDefaultMessage(gender, isBeforeWorkout, workoutTitle)
        }
    }

    override suspend fun analyzeFeedbackAndRecommend(
        recentRPE: List<Int>,
        tooHardCount: Int,
        tooEasyCount: Int,
        gender: Gender
    ): String {
        val averageRPE = if (recentRPE.isNotEmpty()) {
            recentRPE.average()
        } else {
            null
        }

        val tone = if (gender == Gender.FEMALE) {
            "empathique et bienveillant"
        } else {
            "analytique et factuel"
        }

        val prompt = """
            Tu es un coach d'endurance avec un ton $tone.
            Analyse ces données :
            - RPE moyen des 7 dernières séances : ${averageRPE?.let { "%.1f/10".format(it) } ?: "Non disponible"}
            - Nombre de séances jugées trop difficiles : $tooHardCount
            - Nombre de séances jugées trop faciles : $tooEasyCount

            Donne une recommandation personnalisée en 2-3 phrases.
        """.trimIndent()

        return try {
            val response = callMistralAPI(prompt)
            response.ifEmpty { getDefaultRecommendation(averageRPE, tooHardCount, tooEasyCount, gender) }
        } catch (e: Exception) {
            getDefaultRecommendation(averageRPE, tooHardCount, tooEasyCount, gender)
        }
    }

    // ========== Helpers ==========

    /**
     * Appelle l'API Mistral avec un prompt
     *
     * @param prompt Le prompt à envoyer
     * @return La réponse de l'IA
     */
    private suspend fun callMistralAPI(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection

            // Configuration de la requête
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            connection.connectTimeout = 30000 // 30 secondes
            connection.readTimeout = 30000

            // Construction du JSON de la requête
            val requestBody = JSONObject().apply {
                put("model", modelName)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.7)
                put("max_tokens", 2048)
            }

            android.util.Log.d("MistralEngine", "Sending request to Mistral API...")

            // Envoi de la requête
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            // Lecture de la réponse
            val responseCode = connection.responseCode
            android.util.Log.d("MistralEngine", "Response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }

                android.util.Log.d("MistralEngine", "Raw response: ${response.take(500)}...")

                // Parser la réponse JSON
                val jsonResponse = JSONObject(response)
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val message = choices.getJSONObject(0).getJSONObject("message")
                    val content = message.getString("content")
                    return@withContext content
                }
            } else {
                // Lire le message d'erreur
                val errorStream = connection.errorStream
                val errorMessage = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else {
                    "Unknown error"
                }
                android.util.Log.e("MistralEngine", "API error ($responseCode): $errorMessage")
            }

            return@withContext ""
        } catch (e: Exception) {
            android.util.Log.e("MistralEngine", "Exception calling Mistral API: ${e.message}", e)
            throw e
        }
    }

    /**
     * Construit le prompt pour la génération de séances
     *
     * IMPORTANT : Respect de la vie privée
     * - Seules les données anonymes sont envoyées (VMA, FTP, RPE)
     * - AUCUNE donnée personnelle (nom, email, adresse)
     */
    private fun buildWorkoutGenerationPrompt(
        vma: Double?,
        ftp: Int?,
        gender: Gender,
        hasPowerMeter: Boolean,
        availableDays: List<String>,
        averageRPE: Double?,
        isFatigued: Boolean,
        needsProgression: Boolean
    ): String {
        val tone = if (gender == Gender.FEMALE) {
            "empathique et bienveillant"
        } else {
            "analytique et factuel"
        }

        return """
            Tu es un coach d'endurance expert avec un ton $tone.

            Profil athlète (données anonymes) :
            - VMA : ${vma?.let { "%.1f km/h".format(it) } ?: "Non calibré"}
            - FTP : ${ftp?.let { "$it W" } ?: "Non calibré"}
            - Capteur de puissance vélo : ${if (hasPowerMeter) "Oui" else "Non (utiliser fréquence cardiaque)"}

            État de forme récent :
            - RPE moyen : ${averageRPE?.let { "%.1f/10".format(it) } ?: "Pas de données"}
            - Fatigue détectée : ${if (isFatigued) "Oui (>=3 séances difficiles)" else "Non"}
            - Besoin de progression : ${if (needsProgression) "Oui (>=3 séances faciles)" else "Non"}

            Disponibilités cette semaine : ${availableDays.joinToString(", ")}

            Génère un programme d'entraînement structuré pour cette semaine.
            Pour chaque jour disponible, crée une séance avec ce format exact :

            JOUR: [Lundi/Mardi/etc.]
            SPORT: [RUNNING ou CYCLING]
            TITRE: [Titre court de la séance]
            DESCRIPTION: [Description détaillée avec répétitions, intensité, récupération]
            DUREE: [Durée en minutes, nombre entier]
            CIBLE: [PACE si running outdoor, SPEED si running indoor, POWER si vélo avec capteur, HEART_RATE si vélo sans capteur]
            VALEUR: [Valeur numérique de la cible]

            Adapte l'intensité selon l'état de fatigue et besoin de progression.
        """.trimIndent()
    }

    /**
     * Parse la réponse de l'IA et crée les entités DailyWorkoutEntity
     *
     * Format attendu de Mistral (par séance) :
     * JOUR: Lundi
     * SPORT: RUNNING
     * TITRE: Endurance Fondamentale
     * DESCRIPTION: 40 min Zone 2 @ 60-70% VMA
     * DUREE: 60
     * CIBLE: PACE
     * VALEUR: 5.5
     */
    private fun parseWorkoutsFromResponse(response: String, availableDays: List<String>): List<DailyWorkoutEntity> {
        val workouts = mutableListOf<DailyWorkoutEntity>()

        try {
            // Découper la réponse en blocs (un bloc = une séance)
            val workoutBlocks = response.split("JOUR:").filter { it.trim().isNotEmpty() }

            // Map des jours FR → EN pour conversion
            val dayMap = mapOf(
                "lundi" to "MONDAY", "monday" to "MONDAY",
                "mardi" to "TUESDAY", "tuesday" to "TUESDAY",
                "mercredi" to "WEDNESDAY", "wednesday" to "WEDNESDAY",
                "jeudi" to "THURSDAY", "thursday" to "THURSDAY",
                "vendredi" to "FRIDAY", "friday" to "FRIDAY",
                "samedi" to "SATURDAY", "saturday" to "SATURDAY",
                "dimanche" to "SUNDAY", "sunday" to "SUNDAY"
            )

            workoutBlocks.forEach { block ->
                try {
                    // Extraire les champs
                    val day = extractField(block, "JOUR")?.lowercase()?.trim()
                    val sport = extractField(block, "SPORT")?.uppercase()?.trim()
                    val title = extractField(block, "TITRE") ?: extractField(block, "TITLE") ?: "Séance"
                    val description = extractField(block, "DESCRIPTION") ?: ""
                    val duration = extractField(block, "DUREE")?.toIntOrNull()
                        ?: extractField(block, "DURATION")?.toIntOrNull()
                        ?: 60
                    val targetType = extractField(block, "CIBLE")?.uppercase()?.trim()
                        ?: extractField(block, "TARGET")?.uppercase()?.trim()
                    val targetValue = extractField(block, "VALEUR")?.toDoubleOrNull()
                        ?: extractField(block, "VALUE")?.toDoubleOrNull()
                        ?: 0.0

                    // Valider les champs obligatoires
                    if (day != null && sport != null && targetType != null) {
                        val dayOfWeek = dayMap[day]

                        // Vérifier que le jour fait partie des disponibilités
                        if (dayOfWeek != null && availableDays.contains(dayOfWeek)) {
                            val workout = DailyWorkoutEntity(
                                date = getNextDateForDay(dayOfWeek),
                                sport = Sport.valueOf(sport),
                                title = title,
                                description = description,
                                targetType = TargetType.valueOf(targetType),
                                isIndoor = false,
                                durationMinutes = duration,
                                targetValue = targetValue
                            )
                            workouts.add(workout)
                        }
                    }
                } catch (e: Exception) {
                    // Ignorer les blocs mal formatés
                    android.util.Log.w("MistralEngine", "Failed to parse workout block: ${e.message}")
                }
            }

        } catch (e: Exception) {
            // Parsing échoué, retourner liste vide
            android.util.Log.e("MistralEngine", "Failed to parse workouts: ${e.message}", e)
        }

        return workouts
    }

    /**
     * Extrait la valeur d'un champ depuis un bloc de texte
     * Format: "CHAMP: valeur"
     */
    private fun extractField(block: String, fieldName: String): String? {
        val pattern = "$fieldName:\\s*(.+?)(?=\\n[A-Z]+:|$)".toRegex(RegexOption.DOT_MATCHES_ALL)
        return pattern.find(block)?.groupValues?.get(1)?.trim()
    }

    /**
     * Calcule la prochaine date pour un jour donné (ex: prochain lundi)
     * Format retourné: YYYY-MM-DD
     */
    private fun getNextDateForDay(dayOfWeek: String): String {
        val today = java.time.LocalDate.now()
        val daysToAdd = when(dayOfWeek) {
            "MONDAY" -> 1
            "TUESDAY" -> 2
            "WEDNESDAY" -> 3
            "THURSDAY" -> 4
            "FRIDAY" -> 5
            "SATURDAY" -> 6
            "SUNDAY" -> 7
            else -> 1
        }
        return today.plusDays(daysToAdd.toLong()).toString()
    }

    /**
     * Messages par défaut si API échoue
     */
    private fun getDefaultMessage(gender: Gender, isBeforeWorkout: Boolean, workoutTitle: String): String {
        return when {
            isBeforeWorkout && gender == Gender.FEMALE ->
                "Tu vas y arriver ! Cette séance est parfaite pour toi 💪"
            isBeforeWorkout && gender == Gender.MALE ->
                "Objectif : $workoutTitle. Concentre-toi sur la régularité."
            !isBeforeWorkout && gender == Gender.FEMALE ->
                "Bravo ! Tu as donné le meilleur de toi-même aujourd'hui ❤️"
            else ->
                "Séance validée. Performance conforme aux attentes."
        }
    }

    /**
     * Recommandations par défaut si API échoue
     */
    private fun getDefaultRecommendation(
        averageRPE: Double?,
        tooHardCount: Int,
        tooEasyCount: Int,
        gender: Gender
    ): String {
        return when {
            tooHardCount >= 3 && gender == Gender.FEMALE ->
                "Je vois que les dernières séances ont été difficiles. Prenons le temps de récupérer ensemble cette semaine. 🌸"
            tooHardCount >= 3 && gender == Gender.MALE ->
                "RPE moyen : ${averageRPE?.let { "%.1f/10".format(it) } ?: "N/A"}. Réduction de 15% de l'intensité recommandée."
            tooEasyCount >= 3 && gender == Gender.FEMALE ->
                "Tu es en super forme ! Prête pour un nouveau défi ? 💪"
            tooEasyCount >= 3 && gender == Gender.MALE ->
                "Performance stable. Progression de 5% proposée."
            gender == Gender.FEMALE ->
                "Continue comme ça, tu progresses bien ! 🌟"
            else ->
                "Programme actuel adapté. Poursuis."
        }
    }
}
