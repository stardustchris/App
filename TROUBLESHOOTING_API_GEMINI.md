# Troubleshooting - API Gemini ne génère pas de séances

## 📊 État actuel

### ✅ Ce qui fonctionne
- ✅ Application compile et se lance
- ✅ Profil configuré : VMA=14.0 km/h, FTP=234W, Genre=MALE
- ✅ Bouton + déclenche bien le ViewModel
- ✅ Message d'erreur s'affiche : "Aucune séance générée"

### ❌ Ce qui ne fonctionne pas
- ❌ Gemini API ne génère aucune séance
- ❌ Liste vide retournée par `aiRepository.generateWeeklyWorkouts()`
- ❌ Logs de débogage n'apparaissent pas dans Logcat

---

## 🔍 Diagnostic

### Tests effectués

1. **Test curl de l'API** : Erreur 403 Forbidden
   ```bash
   curl -X POST \
     "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyBlOFkLRS4N6IaSKujEk8IPrqD-Vb7iX7M" \
     -H "Content-Type: application/json" \
     -d '{"contents": [{"parts": [{"text": "Test"}]}]}'

   # Résultat: Error 403 (Forbidden)
   ```

2. **Configuration Google Cloud Console** :
   - ✅ API "Gemini API" activée
   - ✅ Clé API créée
   - ✅ Restrictions configurées : "Generative Language API"
   - ❌ Mais l'API retourne toujours 403

### Cause probable

**L'API Gemini n'est pas vraiment fonctionnelle**, malgré l'activation dans Google Cloud Console.

Raisons possibles :
1. **Délai de propagation** : Peut prendre jusqu'à 24h
2. **Mauvais projet** : La clé n'est pas dans le bon projet Google Cloud
3. **API non activée** : "Gemini API" activée, mais pas "Generative Language API"
4. **Quota dépassé** : Impossible car aucune requête n'a réussi
5. **Restrictions trop strictes** : Mais aucune restriction IP/domaine configurée

---

## ✅ Solutions

### Solution 1 : Attendre 24h (Recommandé)

L'activation d'une API Google Cloud peut prendre du temps.

**Action** :
1. Attendez jusqu'à demain (2026-01-20)
2. Relancez l'app
3. Testez le bouton +

---

### Solution 2 : Tester sur Google AI Studio

Vérifiez si l'API fonctionne vraiment :

1. **Allez sur** : https://aistudio.google.com/
2. **Connectez-vous** avec votre compte Google
3. **Testez Gemini** dans le playground
4. **Si ça ne fonctionne pas** → L'API n'est pas active
5. **Si ça fonctionne** → Problème de clé ou de configuration

---

### Solution 3 : Créer une NOUVELLE clé dans un NOUVEAU projet

Parfois, créer un nouveau projet résout le problème :

1. **Allez sur** : https://aistudio.google.com/app/apikey
2. **Cliquez sur** "Create API key"
3. **Sélectionnez** "Create API key in **NEW** project" (pas "existing project")
4. **Copiez** la nouvelle clé
5. **Modifiez** `local.properties` :
   ```properties
   GEMINI_API_KEY=NOUVELLE_CLE_ICI
   ```
6. **Gradle Sync** dans Android Studio
7. **Testez**

---

### Solution 4 : Vérifier les quotas et facturation

1. **Quotas** : https://console.cloud.google.com/apis/api/generativelanguage.googleapis.com/quotas
   - Vérifiez que vous avez des quotas disponibles
   - Quotas gratuits : 15 requêtes/minute, 1M tokens/jour

2. **Facturation** : https://console.cloud.google.com/billing
   - Même avec un plan gratuit, Google Cloud nécessite parfois une carte bancaire enregistrée
   - Vérifiez qu'un compte de facturation est lié au projet

---

### Solution 5 : Vérifier que c'est bien "Generative Language API"

Il y a 2 API différentes :
- ❌ **Gemini API** (pour les apps Google)
- ✅ **Generative Language API** (pour les développeurs externes)

**Vérification** :
1. Allez sur : https://console.cloud.google.com/apis/library
2. Recherchez : "Generative Language API"
3. Vérifiez que le statut est "ENABLED" (pas "Gemini API")

---

## 🧪 Tests de validation

### Test 1 : curl depuis le terminal

```bash
curl -X POST \
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=VOTRE_CLE" \
  -H "Content-Type: application/json" \
  -d '{
    "contents": [{
      "parts": [{
        "text": "Dis bonjour"
      }]
    }]
  }'
```

**Résultat attendu si OK** :
```json
{
  "candidates": [{
    "content": {
      "parts": [{"text": "Bonjour !"}]
    }
  }]
}
```

**Résultat si erreur** :
```html
Error 403 (Forbidden)
```

---

### Test 2 : Google AI Studio

1. https://aistudio.google.com/
2. Playground
3. Tapez "Bonjour"
4. Si réponse → API OK
5. Si erreur → API pas activée

---

## 📁 Fichiers concernés

### Configuration
- `local.properties` : Contient la clé API
- `app/build.gradle.kts` : Charge la clé dans BuildConfig

### Code
- `WorkoutListViewModel.kt` : Appelle `aiRepository.generateWeeklyWorkouts()`
- `AIRepository.kt` : Orchestre la génération via `aiEngine.generateWeeklyWorkouts()`
- `GeminiFlashEngine.kt` : Appelle l'API Gemini

### Logs ajoutés (mais pas visibles actuellement)
- `WorkoutListViewModel:211` : Log "generateWeeklyWorkouts() called"
- `AIRepository:54` : Log "Starting workout generation"
- `AIRepository:62` : Log "Profile loaded: VMA=..., FTP=..."
- `GeminiFlashEngine:113` : Log "Generating workouts with prompt"
- `GeminiFlashEngine:128` : Log "Error generating workouts: ..."

---

## 🎯 Prochaines étapes recommandées

### Immédiat (aujourd'hui)
1. ✅ Tester sur Google AI Studio (https://aistudio.google.com/)
2. ✅ Vérifier "Generative Language API" activée (pas juste "Gemini API")
3. ✅ Créer une nouvelle clé dans un nouveau projet

### Si ça ne fonctionne toujours pas
4. ⏳ Attendre 24h (délai de propagation)
5. 💳 Vérifier la facturation Google Cloud
6. 📧 Contacter le support Google Cloud

---

## 📞 Support

- **Google AI Studio** : https://aistudio.google.com/
- **Documentation API** : https://ai.google.dev/docs
- **Console Google Cloud** : https://console.cloud.google.com/
- **Support Google Cloud** : https://cloud.google.com/support

---

## 📝 Notes

- Date du diagnostic : 2026-01-19
- Clé API utilisée : `AIzaSyBlOFkLRS4N6IaSKujEk8IPrqD-Vb7iX7M`
- Profil test : VMA=14.0 km/h, FTP=234W, Genre=MALE
- Erreur principale : 403 Forbidden lors de l'appel API
