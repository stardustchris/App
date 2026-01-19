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
        val available = apiKey != "YOUR_MISTRAL_API_KEY" && apiKey.isNotEmpty()
        android.util.Log.d("MistralEngine", "isAvailable() = $available (apiKey = ${apiKey.take(10)}...)")
        return available
    }

    /**
     * Initialise le client Mistral AI
     */
    override suspend fun initialize(): Boolean {
        return try {
            android.util.Log.d("MistralEngine", "initialize() called")
            // Vérifier que la clé API est configurée
            val result = isAvailable()
            android.util.Log.d("MistralEngine", "initialize() result = $result")
            result
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
        availableSports: List<String>,
        averageRPE: Double?,
        isFatigued: Boolean,
        needsProgression: Boolean
    ): List<DailyWorkoutEntity> {
        android.util.Log.d("MistralEngine", "generateWeeklyWorkouts() called - vma=$vma, ftp=$ftp, availableDays=$availableDays, availableSports=$availableSports")

        val prompt = buildWorkoutGenerationPrompt(
            vma, ftp, gender, hasPowerMeter,
            availableDays, availableSports, averageRPE, isFatigued, needsProgression
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
        availableSports: List<String>,
        averageRPE: Double?,
        isFatigued: Boolean,
        needsProgression: Boolean
    ): String {
        val tone = if (gender == Gender.FEMALE) {
            "empathique et bienveillant"
        } else {
            "analytique et factuel"
        }

        // Déterminer quels sports générer
        val sportsText = when {
            availableSports.contains("RUNNING") && availableSports.contains("CYCLING") ->
                "Génère un mélange équilibré de séances de RUNNING et CYCLING."
            availableSports.contains("RUNNING") ->
                "Génère UNIQUEMENT des séances de RUNNING (course à pied)."
            availableSports.contains("CYCLING") ->
                "Génère UNIQUEMENT des séances de CYCLING (vélo)."
            else ->
                "Génère un mélange équilibré de séances de RUNNING et CYCLING."
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

            Sports pratiqués : ${availableSports.joinToString(", ")}
            Disponibilités cette semaine : ${availableDays.distinct().joinToString(", ")}

            IMPORTANT : $sportsText

            Réponds UNIQUEMENT avec le format structuré ci-dessous, SANS introduction ni conclusion.

            Pour chaque jour disponible, crée une séance en suivant EXACTEMENT ce format :

            JOUR: Lundi
            SPORT: RUNNING
            TITRE: Endurance Fondamentale
            DESCRIPTION: 40 minutes en zone 2 (60-70% VMA). Allure confortable permettant de parler.
            DUREE: 60
            CIBLE: PACE
            VALEUR: 5.5

            JOUR: Mercredi
            SPORT: CYCLING
            TITRE: Seuil FTP
            DESCRIPTION: 10 min échauffement + 3x10 min @ FTP avec 5 min récup + 10 min retour au calme
            DUREE: 75
            CIBLE: POWER
            VALEUR: 273

            Règles strictes :
            - Un bloc par séance
            - SPORT = ${availableSports.joinToString(" ou ")} UNIQUEMENT (majuscules)
            - CIBLE = PACE (course), SPEED (tapis), POWER (vélo avec capteur), HEART_RATE (vélo sans capteur)
            - DUREE = nombre entier de minutes
            - VALEUR = nombre décimal (vitesse km/h, puissance watts, ou FC bpm)

            Génère maintenant les séances pour les jours disponibles.
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
            android.util.Log.d("MistralEngine", "=== PARSING WORKOUTS ===")
            android.util.Log.d("MistralEngine", "Full response length: ${response.length} chars")
            android.util.Log.d("MistralEngine", "Response preview: ${response.take(300)}...")

            // Découper la réponse en blocs (un bloc = une séance)
            val workoutBlocks = response.split("JOUR:").filter { it.trim().isNotEmpty() }
            android.util.Log.d("MistralEngine", "Found ${workoutBlocks.size} workout blocks")

            // Map des jours FR + EN → EN pour conversion
            val dayMap = mapOf(
                "lundi" to "MONDAY", "monday" to "MONDAY",
                "mardi" to "TUESDAY", "tuesday" to "TUESDAY",
                "mercredi" to "WEDNESDAY", "wednesday" to "WEDNESDAY",
                "jeudi" to "THURSDAY", "thursday" to "THURSDAY",
                "vendredi" to "FRIDAY", "friday" to "FRIDAY",
                "samedi" to "SATURDAY", "saturday" to "SATURDAY",
                "dimanche" to "SUNDAY", "sunday" to "SUNDAY"
            )

            workoutBlocks.forEachIndexed { index, block ->
                try {
                    android.util.Log.d("MistralEngine", "--- Parsing block $index ---")
                    android.util.Log.d("MistralEngine", "Block content: ${block.take(150)}...")

                    // Extraire le jour depuis la première ligne (car on a split par "JOUR:")
                    // Le bloc commence par: " Friday\nSPORT: RUNNING\n..."
                    val lines = block.lines()
                    android.util.Log.d("MistralEngine", "Block has ${lines.size} lines, first 3: [${lines.take(3).joinToString(" | ")}]")
                    val day = lines.firstOrNull { it.trim().isNotEmpty() }?.trim()?.lowercase()
                    android.util.Log.d("MistralEngine", "Extracted day from first non-empty line: '$day'")

                    // Extraire les autres champs normalement
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

                    android.util.Log.d("MistralEngine", "Extracted: day=$day, sport=$sport, title=$title, targetType=$targetType")

                    // Valider les champs obligatoires
                    if (day != null && sport != null && targetType != null) {
                        val dayOfWeek = dayMap[day]
                        android.util.Log.d("MistralEngine", "Day mapped: $day -> $dayOfWeek")

                        // Vérifier que le jour fait partie des disponibilités
                        if (dayOfWeek != null && availableDays.distinct().contains(dayOfWeek)) {
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
                            android.util.Log.d("MistralEngine", "✅ Workout added: $title for $dayOfWeek")
                        } else {
                            android.util.Log.w("MistralEngine", "❌ Day $dayOfWeek not in available days")
                        }
                    } else {
                        android.util.Log.w("MistralEngine", "❌ Missing required fields: day=$day, sport=$sport, targetType=$targetType")
                    }
                } catch (e: Exception) {
                    // Ignorer les blocs mal formatés
                    android.util.Log.e("MistralEngine", "❌ Failed to parse workout block $index: ${e.message}", e)
                }
            }

            android.util.Log.d("MistralEngine", "=== PARSING COMPLETE: ${workouts.size} workouts created ===")


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
