# Guide de Configuration Gemini AI

Ce guide vous accompagne dans la configuration de l'IA Gemini Flash pour la génération automatique de séances d'entraînement.

## Pourquoi Gemini Flash ?

L'application EnduranceFlow utilise une stratégie IA hybride :
- **Gemini Nano** : IA locale (prioritaire, disponible sur certains appareils Android récents)
- **Gemini Flash** : IA Cloud (fallback, nécessite une clé API)

Cette configuration concerne **Gemini Flash**, qui sera utilisée si Gemini Nano n'est pas disponible sur l'appareil.

## Étape 1 : Obtenir votre clé API Gemini

### 1.1 Créer un compte Google AI

1. Rendez-vous sur **Google AI Studio** : https://makersuite.google.com/app/apikey
2. Connectez-vous avec votre compte Google
3. Acceptez les conditions d'utilisation

### 1.2 Générer une clé API

1. Cliquez sur **"Get API Key"** ou **"Create API Key"**
2. Sélectionnez **"Create API key in new project"** ou utilisez un projet existant
3. Copiez la clé générée (format : `AIzaSy...`)

**⚠️ IMPORTANT : Ne partagez JAMAIS cette clé publiquement !**

### 1.3 Quotas gratuits

- **Gemini Flash 1.5** offre un quota gratuit généreux :
  - 15 requêtes par minute (RPM)
  - 1 million de tokens par jour
  - Largement suffisant pour un usage personnel

Plus d'infos : https://ai.google.dev/pricing

## Étape 2 : Configurer le projet Android

### 2.1 Créer le fichier `local.properties`

**Fichier déjà présent :** Un fichier `local.properties` existe probablement déjà dans votre projet (créé par Android Studio pour le chemin du SDK).

1. Ouvrez le fichier `local.properties` à la racine du projet
   - Si le fichier n'existe pas, créez-le manuellement
2. Ajoutez cette ligne (remplacez par votre vraie clé) :

```properties
# Android SDK location (déjà présent)
sdk.dir=/path/to/Android/sdk

# Gemini AI API Key
GEMINI_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

### 2.2 Vérifier le fichier `.gitignore`

Le fichier `.gitignore` doit contenir :

```gitignore
local.properties
```

**✅ C'est déjà configuré** - Votre clé API ne sera jamais commitée sur Git.

### 2.3 Exemple de configuration

Un fichier `local.properties.example` est fourni comme référence :

```properties
sdk.dir=/path/to/Android/sdk
GEMINI_API_KEY=YOUR_GEMINI_API_KEY_HERE
```

**Ne modifiez pas ce fichier** - Il sert uniquement de template.

## Étape 3 : Comprendre l'architecture de sécurité

### 3.1 Comment la clé est chargée

Le fichier `app/build.gradle.kts` charge la clé depuis `local.properties` :

```kotlin
val properties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}

val geminiApiKey = properties.getProperty("GEMINI_API_KEY") ?: "YOUR_GEMINI_API_KEY"
buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
```

### 3.2 Utilisation dans le code

La classe `GeminiFlashEngine` accède à la clé via `BuildConfig` :

```kotlin
import com.enduranceflow.BuildConfig

class GeminiFlashEngine @Inject constructor() : AIEngine {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    // ...
}
```

### 3.3 Sécurité et vie privée

**✅ Données envoyées à Gemini Flash (anonymes) :**
- VMA (Vitesse Maximale Aérobie)
- FTP (Functional Threshold Power)
- RPE moyen (Rating of Perceived Exertion)
- Genre (pour adapter le ton)
- Disponibilités (jours de la semaine)

**❌ Données JAMAIS envoyées :**
- Nom de l'utilisateur
- Adresse email
- Localisation GPS
- Identifiants personnels

## Étape 4 : Tester la configuration

### 4.1 Build du projet

1. Ouvrez le projet dans Android Studio
2. Lancez un **Gradle Sync** (`File > Sync Project with Gradle Files`)
3. Vérifiez qu'aucune erreur n'apparaît

### 4.2 Vérifier BuildConfig

Après le build, le fichier généré `BuildConfig.java` doit contenir :

```java
public final class BuildConfig {
    // ...
    public static final String GEMINI_API_KEY = "AIzaSy...";
}
```

**Localisation :** `app/build/generated/source/buildConfig/debug/com/enduranceflow/BuildConfig.java`

### 4.3 Tester sur l'appareil

1. Lancez l'application sur un émulateur ou appareil physique
2. Complétez l'onboarding (sélection du genre)
3. Réalisez les tests de calibration (VMA/FTP)
4. Sur l'écran **"Mes Séances"**, appuyez sur le bouton **+** (FloatingActionButton)
5. L'IA doit générer un programme d'entraînement personnalisé

**Résultat attendu :** Une ou plusieurs séances apparaissent dans la liste.

## Dépannage

### Erreur : "YOUR_GEMINI_API_KEY"

**Symptôme :** L'app ne génère pas de séances, ou affiche une erreur API.

**Cause :** La clé API n'est pas configurée dans `local.properties`.

**Solution :**
1. Vérifiez que `local.properties` contient `GEMINI_API_KEY=...`
2. Relancez un Gradle Sync
3. Rebuild le projet (`Build > Rebuild Project`)

### Erreur : "API key not valid"

**Symptôme :** Erreur lors de l'appel API Gemini.

**Cause :** Clé API invalide ou restrictions activées.

**Solution :**
1. Vérifiez la clé sur https://makersuite.google.com/app/apikey
2. Assurez-vous que l'API Gemini est activée
3. Vérifiez les restrictions IP/domaine (aucune recommandée pour mobile)

### Erreur : "Quota exceeded"

**Symptôme :** L'app fonctionne puis s'arrête après plusieurs requêtes.

**Cause :** Quota gratuit dépassé (rare pour un usage personnel).

**Solution :**
1. Attendez 1 minute (limite de 15 RPM)
2. Consultez votre usage sur https://console.cloud.google.com/

### Pas de connexion Internet

**Symptôme :** Aucune séance générée.

**Cause :** Gemini Flash nécessite Internet (contrairement à Gemini Nano).

**Solution :**
1. Vérifiez la connexion Wi-Fi/Mobile
2. Testez en mode avion désactivé

## Prochaines étapes

✅ Configuration terminée ! Vous pouvez maintenant :

1. **Utiliser l'application normalement**
   - Générer des programmes hebdomadaires
   - Recevoir des messages motivationnels
   - Obtenir des recommandations basées sur votre RPE

2. **Contribuer au projet**
   - Améliorer le parsing de réponses IA (voir `parseWorkoutsFromResponse`)
   - Ajouter des tests unitaires pour `GeminiFlashEngine`
   - Implémenter Gemini Nano pour les appareils compatibles

3. **Déployer en production**
   - Utiliser **Android Keystore** + **Secrets Gradle Plugin** pour sécuriser la clé
   - Mettre en place un backend pour centraliser les appels API (optionnel)

## Ressources

- **Documentation Gemini API** : https://ai.google.dev/docs
- **Kotlin SDK pour Gemini** : https://github.com/google/generative-ai-android
- **Google AI Studio** : https://makersuite.google.com/
- **Pricing et Quotas** : https://ai.google.dev/pricing

---

**Support** : Si vous rencontrez des problèmes, consultez les logs Android Studio (`Logcat`) pour identifier l'erreur exacte.
