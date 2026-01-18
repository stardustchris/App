# 🤖 Configuration du moteur IA (Gemini)

EnduranceFlow utilise un **Strategy Pattern** pour basculer automatiquement entre IA locale et IA cloud.

## 📋 Stratégie IA

### Priority : Gemini Nano (IA locale / On-Device)
- **Avantages** : Gratuit, rapide, privacy absolu, fonctionne offline
- **Inconvénients** : Disponible uniquement sur certains appareils récents
- **Statut actuel** : 🚧 En attente de l'API publique Gemini Nano

### Fallback : Gemini Flash (IA cloud)
- **Avantages** : Toujours disponible (si Internet), modèle plus puissant
- **Inconvénients** : Nécessite connexion, latence plus élevée, coûts API
- **Statut actuel** : ✅ Prêt à l'utilisation (nécessite clé API)

---

## 🔑 Configuration de la clé API Gemini Flash

### Étape 1 : Obtenir une clé API

1. Accédez à [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Connectez-vous avec votre compte Google
3. Créez une nouvelle clé API
4. Copiez la clé (format : `AIza...`)

### Étape 2 : Configurer la clé dans le projet

**Option A : Fichier `local.properties` (recommandé pour dev)**

Ajoutez cette ligne dans le fichier `local.properties` (à la racine du projet) :

```properties
GEMINI_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

Puis modifiez `app/build.gradle.kts` :

```kotlin
android {
    defaultConfig {
        // ...

        // Charger la clé API depuis local.properties
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String", "GEMINI_API_KEY", "\"${properties.getProperty("GEMINI_API_KEY")}\"")
    }

    buildFeatures {
        buildConfig = true
    }
}
```

Puis dans `GeminiFlashEngine.kt`, remplacez :

```kotlin
private val apiKey = "YOUR_GEMINI_API_KEY"
```

par :

```kotlin
private val apiKey = BuildConfig.GEMINI_API_KEY
```

**Option B : Variables d'environnement (pour CI/CD)**

```bash
export GEMINI_API_KEY="AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
```

**Option C : Secrets Gradle Plugin (production)**

Pour la production, utilisez le [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin) pour sécuriser la clé.

---

## 📱 Détection automatique du moteur

L'application détecte automatiquement le moteur disponible :

```kotlin
// AIEngineFactory.kt
suspend fun getEngine(): AIEngine {
    // 1. Tentative Gemini Nano (local)
    if (geminiNanoEngine.isAvailable() && geminiNanoEngine.initialize()) {
        return geminiNanoEngine // ✅ Utiliser Nano
    }

    // 2. Fallback Gemini Flash (cloud)
    geminiFlashEngine.initialize()
    return geminiFlashEngine // ✅ Utiliser Flash
}
```

---

## 🔒 Privacy & Security

### Données envoyées à l'IA Cloud (Gemini Flash)

**✅ Données ANONYMES envoyées :**
- VMA (km/h)
- FTP (Watts)
- Genre (FEMALE/MALE)
- RPE (Rating of Perceived Exertion)
- Disponibilités (jours de la semaine)
- Équipement (capteur de puissance : oui/non)

**❌ Données JAMAIS envoyées :**
- Nom
- Email
- Adresse
- Localisation GPS
- Toute donnée personnelle identifiable

### Code de protection (context.json)

```json
"avoid_list": [
  "Ne jamais envoyer d'identifiants personnels (Nom, Email) à l'IA Cloud."
]
```

---

## 🧪 Test de l'IA

### Tester Gemini Flash (Cloud)

```kotlin
// Dans un ViewModel ou Repository
val aiRepository = AIRepository(...)

// Générer des séances
val workouts = aiRepository.generateWeeklyWorkouts()

// Générer un message de motivation
val message = aiRepository.generateMotivationalMessage(
    workoutTitle = "VMA Courte",
    isBeforeWorkout = true
)

// Analyser les feedbacks
val recommendation = aiRepository.analyzeFeedbackAndRecommend()
```

### Forcer le mode Cloud (pour test)

```kotlin
aiRepository.forceCloudAI() // Force Gemini Flash même si Nano disponible
```

### Vérifier le moteur actif

```kotlin
val engineInfo = aiRepository.getAIEngineInfo()
// {
//   "type": "GEMINI_FLASH",
//   "isLocal": false,
//   "nanoAvailable": false,
//   "flashAvailable": true
// }
```

---

## 🚀 Utilisation dans l'app

### Écran Profil : Afficher le mode IA

```kotlin
// ProfileViewModel.kt
val engineInfo = aiRepository.getAIEngineInfo()

val aiMode = if (engineInfo["isLocal"] == true) {
    "Gemini Nano (Local)"
} else {
    "Gemini Flash (Cloud)"
}

// Affichage UI
Text("Mode IA : $aiMode")
```

### Écran Workout : Générer la semaine

```kotlin
// WorkoutListViewModel.kt
fun generateWeeklyWorkouts() {
    viewModelScope.launch {
        val workouts = aiRepository.generateWeeklyWorkouts()
        // Workouts enregistrés automatiquement en BDD
    }
}
```

---

## 📊 Quotas et limites (Gemini Flash)

### Tier gratuit

- **15 requêtes/minute**
- **1 500 requêtes/jour**
- **1 million de tokens/mois**

Pour EnduranceFlow :
- Génération hebdomadaire : ~1 requête/semaine
- Messages de motivation : ~3-5 requêtes/semaine
- Analyse feedbacks : ~1 requête/semaine

**Total estimé : ~5-10 requêtes/semaine par utilisateur**

→ Largement dans les limites gratuites !

---

## 🔄 Roadmap

- [ ] **Phase 1** : Gemini Flash fonctionnel (fallback cloud) ✅ FAIT
- [ ] **Phase 2** : Intégration Gemini Nano quand API disponible
- [ ] **Phase 3** : Cache local des recommandations IA
- [ ] **Phase 4** : Fine-tuning du modèle avec données historiques
- [ ] **Phase 5** : Support de modèles alternatifs (Llama, Mistral)

---

## 🆘 Troubleshooting

### Erreur : "API key not configured"

→ Vérifiez que `GEMINI_API_KEY` est bien défini dans `local.properties`

### Erreur : "Network error"

→ Vérifiez la connexion Internet (Gemini Flash nécessite Internet)

### Workouts vides

→ Vérifiez que le profil est calibré (VMA + FTP renseignés)

### IA toujours en mode Cloud

→ Normal ! Gemini Nano n'est pas encore disponible publiquement.
