# Configuration Mistral AI

## 🇫🇷 Présentation

**Endurance Flow** utilise **Mistral AI** comme moteur d'intelligence artificielle pour générer des plans d'entraînement personnalisés.

**Pourquoi Mistral AI ?**
- Entreprise française avec excellente compréhension du français
- Quota gratuit généreux
- API rapide et fiable
- Privacy-first : seules les données anonymes (VMA, FTP, RPE) sont envoyées

---

## 📋 Prérequis

1. Connexion Internet active
2. Compte Mistral AI (gratuit)
3. Clé API Mistral

---

## 🔑 Obtenir une clé API Mistral

### 1. Créer un compte

1. Allez sur : https://console.mistral.ai/
2. Cliquez sur **"Sign in with Google"** ou **"Sign up"**
3. Vérifiez votre email si nécessaire

### 2. Créer une clé API

1. Une fois connecté, allez dans **"API Keys"** (menu à gauche)
2. Cliquez sur **"Create new key"**
3. Donnez un nom : **"Endurance Flow App"**
4. Cliquez sur **"Create"**
5. **COPIEZ IMMÉDIATEMENT LA CLÉ** (elle ne sera plus affichée)

---

## ⚙️ Configuration dans l'app

### 1. Ouvrir le fichier `local.properties`

Dans Android Studio :
- Ouvrez `local.properties` à la racine du projet
- Si le fichier n'existe pas, créez-le

### 2. Ajouter la clé API

Ajoutez cette ligne (remplacez par votre clé) :

```properties
MISTRAL_API_KEY=votre_cle_api_ici
```

### 3. Sync du projet

1. `File` → `Sync Project with Gradle Files` 🐘
2. `Build` → `Rebuild Project`
3. Lancez l'app ▶️

---

## ✅ Vérification

Une fois configuré, l'app devrait :

1. **Générer des workouts** quand vous cliquez sur le bouton `+`
2. **Afficher "Mistral AI"** dans les informations du moteur IA
3. **Logs Logcat** montrant :
   ```
   D/MistralEngine: initialize() called
   D/MistralEngine: isAvailable() = true
   D/MistralEngine: Sending request to Mistral API...
   D/MistralEngine: Response code: 200
   ```

---

## 🔒 Sécurité

**IMPORTANT** :
- ❌ **NE JAMAIS** commiter `local.properties` sur Git
- ❌ **NE JAMAIS** partager votre clé API publiquement
- ✅ Le fichier `local.properties` est dans `.gitignore` par défaut

---

## 🐛 Problèmes courants

### "Aucune séance générée"

**Causes possibles :**
1. Clé API manquante ou invalide
2. Pas de connexion Internet
3. Profil non calibré (VMA/FTP manquants)

**Solution :**
- Vérifiez Logcat (filtre : `MistralEngine`)
- Vérifiez que `MISTRAL_API_KEY` est bien dans `local.properties`
- Vérifiez votre profil athlète (VMA et FTP configurés)

### Erreur 401 Unauthorized

**Cause :** Clé API invalide

**Solution :**
1. Re-créez une nouvelle clé sur https://console.mistral.ai/
2. Mettez à jour `local.properties`
3. Rebuild le projet

---

## 📚 Documentation

- API Mistral : https://docs.mistral.ai/
- Console Mistral : https://console.mistral.ai/
- Modèle utilisé : `mistral-small-latest`

---

## 🔄 Migration depuis Gemini

Si vous veniez de Gemini Flash :

1. Supprimez l'ancienne ligne `GEMINI_API_KEY` de `local.properties`
2. Ajoutez `MISTRAL_API_KEY`
3. Sync + Rebuild
4. C'est tout ! Le code gère automatiquement Mistral

---

## 💡 Astuce

Pour tester rapidement si votre clé API fonctionne :

```bash
curl -X POST "https://api.mistral.ai/v1/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_CLE_API" \
  -d '{
    "model": "mistral-small-latest",
    "messages": [{"role": "user", "content": "Dis bonjour"}]
  }'
```

Si la clé est valide, vous recevrez une réponse JSON avec "Bonjour".
