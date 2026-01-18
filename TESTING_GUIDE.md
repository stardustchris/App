# Guide de Test - Génération de Séances

Ce guide vous aide à tester la fonctionnalité de génération de séances par l'IA Gemini.

## 🔧 Bugs Corrigés

### ✅ Ce qui a été réparé :

1. **WorkoutListViewModel** - Appel à l'IA maintenant actif
   - Injection de `AIRepository` ajoutée
   - Méthode `generateWeeklyWorkouts()` implémentée (plus de TODO)

2. **AIRepository** - Sauvegarde des séances activée
   - Les séances générées sont maintenant enregistrées en base de données
   - Disponibilités par défaut (Lun, Mer, Ven) si non configurées

3. **WorkoutRepository** - Méthode batch insert créée
   - `createWorkouts()` pour insérer plusieurs séances d'un coup
   - Utilisée par l'IA après génération

4. **GeminiFlashEngine** - Parser robuste implémenté
   - Extraction structurée des champs de la réponse IA
   - Support FR/EN pour les noms de champs
   - Calcul automatique des dates

5. **Flow réactif** - Mise à jour UI automatique
   - Pas besoin de recharger : les séances apparaissent immédiatement

---

## 📱 Comment Tester dans Android Studio

### Étape 1 : Synchroniser le code

1. **Pull les changements** :
   ```bash
   git pull origin claude/setup-app-startup-auth-QW9JS
   ```

2. **Gradle Sync** dans Android Studio :
   - `File > Sync Project with Gradle Files`
   - Attendez la fin du sync (barre de progression en bas)

3. **Rebuild le projet** :
   - `Build > Rebuild Project`
   - Vérifiez qu'il n'y a pas d'erreurs de compilation

---

### Étape 2 : Vérifier la clé API

1. **Ouvrez `local.properties`** à la racine du projet

2. **Vérifiez que la clé est présente** :
   ```properties
   GEMINI_API_KEY=AIzaSyBY3Ra_060p7qlI_ZaPNCWdKnZ6LBfWIcs
   ```

3. **Si absente, ajoutez-la** (la clé est déjà fournie ci-dessus)

---

### Étape 3 : Lancer l'application

1. **Sélectionnez un émulateur** ou connectez un appareil physique

2. **Lancez l'app** :
   - Cliquez sur ▶️ (Run) ou `Shift+F10`
   - Attendez l'installation et le lancement

3. **Observez Logcat** pour voir les logs :
   - Ouvrez l'onglet `Logcat` en bas
   - Filtrez sur `EnduranceFlow` ou `Gemini`

---

### Étape 4 : Test du parcours complet

#### 4.1 Onboarding

1. **Sélectionnez votre genre** (Femme ou Homme)
2. **Activez le capteur de puissance** (optionnel)
3. **Remplissez VMA et FTP** :
   - VMA : `18.5`
   - FTP : `280`
4. **Appuyez sur "Continuer"**

#### 4.2 Calibration (si vous avez skip VMA/FTP)

1. **Entrez VMA** : `18.5`
2. **Validez le test VMA**
3. **Entrez FTP** : `280`
4. **Validez le test FTP**
5. **Appuyez sur "Terminer la calibration"**

#### 4.3 Écran Mes Séances

1. **Vous arrivez sur l'écran principal** (vide au début)
2. **Appuyez sur le bouton +** (FloatingActionButton en bas à droite)

---

### Étape 5 : Observer la génération

#### Ce qui devrait se passer :

1. **Loading** : Spinner circulaire s'affiche
2. **Appel API** : Dans Logcat, vous devriez voir :
   ```
   D/GeminiFlashEngine: Generating workouts with prompt: ...
   D/GeminiFlashEngine: Response received: ...
   ```

3. **Parsing** : L'IA parse la réponse
4. **Sauvegarde** : Les séances sont insérées en BDD
5. **Affichage** : Les séances apparaissent automatiquement

#### Résultat attendu :

**Vous devriez voir 1 à 3 cartes de séances** :

```
┌─────────────────────────────────┐
│ Endurance Fondamentale          │
│ 2026-01-20 • RUNNING            │
│                                 │
│ 40 min Zone 2 @ 60-70% VMA      │
│                                 │
│ 60 min      PACE: 5:30/km       │
│                                 │
│ Mode Extérieur         [⚪]     │
│                                 │
│ [Marquer comme terminée]        │
└─────────────────────────────────┘
```

---

## 🐛 En cas d'erreur

### Erreur : "Aucune séance générée"

**Cause possible** : Profil incomplet

**Solution** :
1. Vérifiez que VMA et FTP sont renseignés
2. Allez dans l'écran Profil (icône 👤)
3. Vérifiez que les données sont présentes

---

### Erreur : "Erreur lors de la génération"

**Causes possibles** :
- Clé API invalide
- Pas de connexion Internet
- Quota Gemini dépassé

**Solutions** :
1. **Vérifier la clé API** dans `local.properties`
2. **Activer Internet** sur l'émulateur/appareil
3. **Consulter Logcat** pour l'erreur exacte :
   ```
   Logcat > Filter: "Gemini" ou "Exception"
   ```

4. **Tester la clé API** manuellement :
   - Allez sur https://aistudio.google.com/
   - Testez la clé dans le playground

---

### Erreur : "API key not valid"

**Cause** : Clé API incorrecte ou expirée

**Solution** :
1. Générez une nouvelle clé sur https://makersuite.google.com/app/apikey
2. Remplacez dans `local.properties`
3. Gradle Sync + Rebuild

---

### Aucune séance ne s'affiche (pas d'erreur)

**Causes possibles** :
- Réponse de l'IA mal formatée
- Parsing échoué

**Solution - Activer les logs détaillés** :

Ajoutez dans `GeminiFlashEngine.kt:113` (après `generateContent`) :
```kotlin
val response = generativeModel?.generateContent(prompt)
val workoutsText = response?.text ?: return emptyList()

// ✅ Ajouter ce log
android.util.Log.d("GeminiFlashEngine", "Response from AI: $workoutsText")

parseWorkoutsFromResponse(workoutsText, availableDays)
```

Puis regardez dans Logcat pour voir la réponse brute de Gemini.

---

## 🎯 Test Avancé - Vérifier les données en BDD

### Option 1 : Database Inspector (Android Studio)

1. **Ouvrez Database Inspector** :
   - `View > Tool Windows > App Inspection`
   - Sélectionnez l'onglet `Database Inspector`

2. **Sélectionnez votre app** en cours d'exécution

3. **Naviguez vers la table** `daily_workout`

4. **Vérifiez les colonnes** :
   - `date` : Date de la séance (YYYY-MM-DD)
   - `sport` : RUNNING ou CYCLING
   - `title` : Titre généré par l'IA
   - `description` : Description détaillée
   - `targetType` : PACE, SPEED, POWER, ou HEART_RATE
   - `targetValue` : Valeur numérique
   - `durationMinutes` : Durée
   - `isCompleted` : false (par défaut)

---

### Option 2 : Logcat avec logs SQL

Ajoutez dans `app/build.gradle.kts` :
```kotlin
android {
    defaultConfig {
        // ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
}
```

Puis dans `DatabaseModule.kt`, ajoutez `.setQueryCallback()` :
```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "endurance_flow_db")
    .setQueryCallback(object : RoomDatabase.QueryCallback {
        override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
            Log.d("RoomQuery", "SQL: $sqlQuery | Args: $bindArgs")
        }
    }, Executors.newSingleThreadExecutor())
    .build()
```

---

## ✅ Checklist de Test

- [ ] Code synchronisé (git pull)
- [ ] Gradle Sync réussi
- [ ] Rebuild sans erreurs
- [ ] Clé API présente dans local.properties
- [ ] App lancée sur émulateur/appareil
- [ ] Onboarding complété (Genre + VMA/FTP)
- [ ] Bouton + visible sur écran Mes Séances
- [ ] Appui sur + déclenche le loading
- [ ] Séances apparaissent après quelques secondes
- [ ] Au moins 1 séance affichée
- [ ] Titre, description, durée corrects
- [ ] Switch Indoor/Outdoor fonctionne
- [ ] Pas d'erreur dans Logcat

---

## 📊 Données de Test Recommandées

Pour obtenir des séances réalistes :

| Profil | VMA | FTP | Genre | Résultat attendu |
|--------|-----|-----|-------|------------------|
| Débutant | 14.0 | 200 | FEMALE | Séances courtes, intensité modérée |
| Intermédiaire | 18.5 | 280 | MALE | Mix endurance + fractionné |
| Avancé | 22.0 | 350 | FEMALE | Séances longues, haute intensité |

---

## 🚀 Prochaines Étapes

Si tout fonctionne :
1. Testez le basculement Indoor/Outdoor
2. Marquez une séance comme terminée
3. Soumettez un feedback avec RPE
4. Générez une nouvelle semaine
5. Vérifiez l'alerte de fatigue (si RPE élevé)

---

## 📝 Rapport de Bug

Si ça ne fonctionne toujours pas, envoyez-moi :

1. **Capture Logcat** (filtré sur "Gemini", "Error", "Exception")
2. **Capture Database Inspector** (table daily_workout)
3. **Valeurs du profil** (VMA, FTP, Genre)
4. **Message d'erreur affiché** dans l'app

---

**Bonne chance pour les tests ! 🎉**
