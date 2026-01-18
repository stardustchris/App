# 🎨 Intégration UI ↔ ViewModels - TERMINÉE ✅

Tous les écrans Jetpack Compose sont maintenant **entièrement connectés** aux ViewModels avec architecture MVVM complète.

---

## ✅ Écrans connectés (4/4)

### 1️⃣ OnboardingScreen → OnboardingViewModel

**Fonctionnalités implémentées :**
- ✅ Injection Hilt : `hiltViewModel()`
- ✅ Collecte du state : `uiState.collectAsState()`
- ✅ Sélection du genre (Femme/Homme) avec feedback visuel
- ✅ Toggle capteur de puissance avec Switch
- ✅ Champs VMA/FTP optionnels (TextFields)
- ✅ Validation de formulaire (bouton désactivé si genre non sélectionné)
- ✅ Navigation automatique (Calibration si VMA/FTP manquants, sinon Workouts)
- ✅ Affichage des erreurs avec Card rouge
- ✅ Indicateur de chargement (CircularProgressIndicator)

**Actions utilisateur :**
```kotlin
viewModel.onGenderSelected(Gender.FEMALE)  // Sélectionne le genre
viewModel.onPowerMeterChanged(true)         // Active le capteur
viewModel.completeOnboarding { ... }        // Finalise l'onboarding
```

---

### 2️⃣ CalibrationScreen → CalibrationViewModel

**Fonctionnalités implémentées :**
- ✅ Injection Hilt + State collection
- ✅ Formulaires interactifs VMA et FTP (OutlinedTextField)
- ✅ Validation des tests (toDoubleOrNull, toIntOrNull)
- ✅ Indicateurs visuels de complétion (icône Check ✓)
- ✅ Changement de couleur des cards (primary quand complété)
- ✅ Calcul et affichage des zones d'entraînement en temps réel
- ✅ Composant ZoneRow pour visualiser les zones
- ✅ Bouton "Terminer" activé uniquement si VMA ET FTP complétés
- ✅ Scroll support pour long contenu

**Actions utilisateur :**
```kotlin
viewModel.submitVMATest(18.5)    // Enregistre VMA = 18.5 km/h
viewModel.submitFTPTest(280)      // Enregistre FTP = 280 W
viewModel.isCalibrationComplete() // Vérifie si les deux tests sont faits
```

**Zones calculées automatiquement :**
- VMA : Zone 1 (60%), Zone 2 (80%), Zone 3 (90%), Zone 4 (100%)
- FTP : Zone 1 (50%), Zone 2 (70%), Zone 3 (85%), Zone 4 (100%)

---

### 3️⃣ WorkoutListScreen → WorkoutListViewModel

**Fonctionnalités implémentées :**
- ✅ Injection Hilt + State collection
- ✅ FloatingActionButton "+" pour générer les séances
- ✅ LazyColumn pour liste scrollable de séances
- ✅ État vide avec message informatif
- ✅ Indicateur de chargement pendant génération
- ✅ **Alertes de fatigue** (carte rouge si `isFatigued = true`)
- ✅ **Alertes de progression** (carte orange si `needsProgression = true`)
- ✅ WorkoutCard component avec :
  - Titre, date, sport, description
  - Durée et valeur cible (VMA, FTP, etc.)
  - Switch Indoor/Outdoor par séance
  - Bouton "Marquer comme terminée"
  - État visuel (complété = gris, actif = bleu)
- ✅ Snackbar pour afficher les erreurs
- ✅ Toutes les données proviennent de `uiState`

**Actions utilisateur :**
```kotlin
viewModel.generateWeeklyWorkouts()                // Génère séances via IA
viewModel.toggleIndoorMode(workoutId, true)       // Passe en Indoor
viewModel.markWorkoutAsCompleted(workoutId) {}    // Marque terminée
viewModel.submitFeedback(workoutId, rpe=7, ...)   // Enregistre RPE
```

**Détection intelligente :**
- Si RPE moyen > 8 ou ≥3 séances "difficiles" → Alerte fatigue
- Si ≥3 séances "faciles" → Alerte progression

---

### 4️⃣ ProfileScreen → ProfileViewModel

**Fonctionnalités implémentées :**
- ✅ Injection Hilt + State collection
- ✅ État de chargement avec CircularProgressIndicator
- ✅ Affichage des données du profil (VMA, FTP, MaxHR, Genre)
- ✅ **Ton de l'IA** adapté au genre :
  - Femme → "Empathique et Bienveillant"
  - Homme → "Analytique et Factuel"
- ✅ Toggle capteur de puissance avec mise à jour temps réel
- ✅ **Calcul dynamique des zones d'entraînement** :
  - Zones VMA (si VMA renseigné)
  - Zones FTP (si FTP renseigné)
- ✅ Affichage conditionnel (zones uniquement si données existent)
- ✅ Section IA avec informations sur le mode (Nano/Flash)
- ✅ Notice de privacy (données anonymes uniquement)
- ✅ Scroll support pour tout le contenu
- ✅ Formatage null-safe des valeurs

**Actions utilisateur :**
```kotlin
viewModel.updateVMA(19.2)            // Modifie la VMA
viewModel.updateFTP(290)             // Modifie le FTP
viewModel.togglePowerMeter(true)     // Active le capteur
viewModel.calculateVMAZones()        // Calcule les zones VMA
viewModel.getAITone()                // Retourne le ton de l'IA
```

**Zones affichées dynamiquement :**
- VMA : Zone 1 (Récup) → Zone 5 (Anaérobie)
- FTP : Zone 1 (Récup) → Zone 6 (Anaérobie)

---

## 🔗 Architecture MVVM complète

```
┌─────────────────┐
│  Compose Screen │  ← L'utilisateur interagit
└────────┬────────┘
         │ hiltViewModel()
         │
┌────────▼────────┐
│   ViewModel     │  ← Gère le state et la logique
│   + UiState     │
└────────┬────────┘
         │ collectAsState()
         │
┌────────▼────────┐
│  Repository     │  ← Logique métier
└────────┬────────┘
         │
┌────────▼────────┐
│  DAO (Room)     │  ← Accès base de données
└─────────────────┘
```

---

## 🎯 Fonctionnalités clés implémentées

### ✅ Réactivité UI
Tous les écrans utilisent **StateFlow + collectAsState()** :
- L'UI se met à jour automatiquement quand les données changent
- Exemple : Quand VMA est enregistrée, les zones sont recalculées instantanément

### ✅ Gestion des états
Chaque écran gère 3 états :
1. **Loading** : CircularProgressIndicator
2. **Empty** : Message informatif + call-to-action
3. **Content** : Données affichées avec cards

### ✅ Validation de formulaires
- Boutons désactivés tant que les données requises ne sont pas saisies
- Validation des types (Double, Int) avant soumission
- Feedback visuel (couleurs, icônes, messages d'erreur)

### ✅ Navigation conditionnelle
- Onboarding → Calibration (si VMA/FTP manquants)
- Onboarding → Workouts (si VMA/FTP connus)
- Calibration → Workouts (après tests complétés)

### ✅ Adaptation contextuelle
- **Genre** → Ton de l'IA adapté (Empathique vs Analytique)
- **Équipement** → Type de cible (POWER vs HEART_RATE)
- **Fatigue** → Alertes visuelles + recommandations IA

---

## 🧪 Comment tester l'application

### 1. Lancer l'application
```bash
cd /home/user/App
./gradlew installDebug
# ou depuis Android Studio : Run 'app'
```

### 2. Parcours utilisateur complet

**Étape 1 : Onboarding**
1. Ouvrir l'app → Écran OnboardingScreen
2. Sélectionner genre (Femme ou Homme)
3. Activer/désactiver le capteur de puissance
4. (Optionnel) Saisir VMA + FTP si connus
5. Cliquer "Continuer" → Navigation automatique

**Étape 2 : Calibration** (si VMA/FTP non renseignés)
1. Saisir VMA (ex: 18.5)
2. Cliquer "Valider le test VMA" → Zones VMA apparaissent
3. Saisir FTP (ex: 280)
4. Cliquer "Valider le test FTP" → Zones FTP apparaissent
5. Cliquer "Terminer la calibration" → Navigation vers WorkoutList

**Étape 3 : Liste des séances**
1. Écran WorkoutListScreen (vide au début)
2. Cliquer sur le bouton "+" → Génère séances (TODO: IA à implémenter)
3. Toggle Indoor/Outdoor sur une séance
4. Cliquer "Marquer comme terminée"
5. Cliquer sur l'icône Profil (en haut à droite)

**Étape 4 : Profil**
1. Voir les données physiologiques (VMA, FTP, MaxHR, Genre)
2. Voir le ton de l'IA adapté au genre
3. Toggle capteur de puissance → Impact sur séances
4. Voir les zones d'entraînement calculées
5. Cliquer retour → Retour à WorkoutList

---

## 📊 Données testables

### Profil exemple
```kotlin
Gender: FEMALE
VMA: 18.5 km/h
FTP: 280 W
MaxHR: 195 bpm
PowerMeter: true
```

### Séance exemple
```kotlin
Date: "2026-01-20"
Sport: RUNNING
Title: "VMA Courte"
Description: "8x400m @ 100% VMA - Récup 1'30"
TargetType: PACE
Duration: 45 min
TargetValue: 18.5 km/h
IsIndoor: false
```

---

## 🚀 Prochaines étapes

### Implémentations restantes

1. **Intégration IA Gemini Flash**
   - Configurer la clé API (voir AI_SETUP.md)
   - Tester la génération de séances
   - Tester les messages de motivation

2. **Tests unitaires**
   - Tests des ViewModels
   - Tests des Repositories
   - Tests UI avec Compose Testing

3. **Intégration Health Connect** (optionnel)
   - Synchroniser les séances
   - Récupérer FC, distance, etc.

4. **Améliorations UI**
   - Animations de transition
   - Illustrations personnalisées
   - Mode sombre amélioré

---

## ✅ Récapitulatif final

**État actuel de l'application :**
- ✅ Architecture Clean complète (UI → ViewModel → Repository → DAO → Database)
- ✅ 4 écrans fonctionnels connectés aux ViewModels
- ✅ Navigation entre écrans opérationnelle
- ✅ Gestion d'état réactive (StateFlow)
- ✅ Base de données Room configurée
- ✅ Injection de dépendances Hilt
- ✅ Moteur IA (Gemini) prêt (nécessite clé API)
- ✅ Material 3 Design System
- ✅ Privacy-First (données anonymes)

**L'application est maintenant FONCTIONNELLE et peut être testée !** 🎉

Pour compiler et tester :
```bash
cd /home/user/App
./gradlew clean assembleDebug
./gradlew installDebug
```

Ou depuis Android Studio : **Run > Run 'app'**
