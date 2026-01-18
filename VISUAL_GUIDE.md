# Guide Visuel - EnduranceFlow Android

Ce document décrit l'apparence et le comportement de l'application EnduranceFlow.

## 🎨 Design System

L'application utilise **Material Design 3** avec les caractéristiques suivantes :
- **Thème** : Couleurs Material You (adaptatives selon l'appareil)
- **Typographie** : Material Typography Scale (displaySmall, headlineSmall, titleLarge, bodyLarge, etc.)
- **Composants** : 100% Jetpack Compose
- **Navigation** : Fluide avec transitions automatiques

---

## 📱 Parcours Utilisateur Complet

### Étape 1 : Écran d'Onboarding

**OnboardingScreen.kt** - Premier lancement de l'application

#### Apparence visuelle

```
┌─────────────────────────────────────┐
│                                     │
│       Bienvenue sur                 │
│     EnduranceFlow                   │
│  (Grand titre en couleur primaire)  │
│                                     │
│  Votre coach d'endurance adaptatif  │
│      Indoor/Outdoor                 │
│                                     │
├─────────────────────────────────────┤
│                                     │
│  1. Quel est votre genre ?          │
│  L'IA adaptera son ton de coaching  │
│                                     │
│  ┌────────────┐  ┌────────────┐    │
│  │   Femme    │  │   Homme    │    │
│  │    Ton     │  │    Ton     │    │
│  │ empathique │  │ analytique │    │
│  └────────────┘  └────────────┘    │
│   (Boutons avec feedback visuel)   │
│                                     │
├─────────────────────────────────────┤
│                                     │
│  2. Équipement                      │
│                                     │
│  Capteur de puissance (vélo)   [⚪] │
│  Permet les séances en Watts        │
│                                     │
├─────────────────────────────────────┤
│                                     │
│  3. Connaissez-vous vos données ?   │
│  Optionnel : Si non, calibration    │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ VMA (km/h)      Ex: 18.5    │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ FTP (Watts)     Ex: 280     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Continuer vers calibration  │   │
│  └─────────────────────────────┘   │
│     (Bouton principal Material)     │
└─────────────────────────────────────┘
```

#### Comportement

- **Sélection genre** : Les boutons changent de couleur (primaryContainer) quand sélectionnés
- **Switch équipement** : Active/désactive le capteur de puissance
- **Champs optionnels** : Si VMA/FTP renseignés → Skip calibration, sinon → Calibration obligatoire
- **Bouton dynamique** : Texte change selon les données saisies
- **Loading state** : Spinner circulaire pendant la sauvegarde

---

### Étape 2 : Écran de Calibration

**CalibrationScreen.kt** - Tests physiologiques (si nécessaire)

#### Apparence visuelle

```
┌─────────────────────────────────────┐
│                                     │
│    Semaine de Calibration           │
│   (Grand titre en primaire)         │
│                                     │
│  Réalisez ces tests pour déterminer │
│  vos zones d'entraînement           │
│                                     │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 📊 Test VMA             ✓   │   │
│  │                             │   │
│  │ Test progressif pour        │   │
│  │ déterminer votre VMA        │   │
│  │                             │   │
│  │ ┌───────────────────────┐   │   │
│  │ │ VMA (km/h)  Ex: 18.5  │   │   │
│  │ └───────────────────────┘   │   │
│  │                             │   │
│  │       [Valider le test VMA] │   │
│  └─────────────────────────────┘   │
│   (Carte avec fond secondaryContainer)│
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🚴 Test FTP             ✓   │   │
│  │                             │   │
│  │ Test de 20 minutes pour     │   │
│  │ déterminer votre seuil      │   │
│  │                             │   │
│  │ ┌───────────────────────┐   │   │
│  │ │ FTP (Watts) Ex: 280   │   │   │
│  │ └───────────────────────┘   │   │
│  │                             │   │
│  │       [Valider le test FTP] │   │
│  └─────────────────────────────┘   │
│   (Carte avec fond tertiaryContainer)│
│                                     │
├─────────────────────────────────────┤
│                                     │
│  🎯 Zones d'entraînement VMA        │
│  ┌─────────────────────────────┐   │
│  │ Zone 1 (Récup)    11.1 km/h │   │
│  │ ───────────────────────────  │   │
│  │ Zone 2 (Endurance) 14.8 km/h│   │
│  │ ───────────────────────────  │   │
│  │ Zone 3 (Seuil)    16.7 km/h │   │
│  │ ───────────────────────────  │   │
│  │ Zone 4 (VMA)      18.5 km/h │   │
│  └─────────────────────────────┘   │
│   (Carte avec fond surfaceVariant)  │
│                                     │
│  🎯 Zones d'entraînement FTP        │
│  ┌─────────────────────────────┐   │
│  │ Zone 1 (Récup)       140 W  │   │
│  │ Zone 2 (Endurance)   196 W  │   │
│  │ Zone 3 (Tempo)       238 W  │   │
│  │ Zone 4 (Seuil)       280 W  │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Terminer la calibration     │   │
│  │    et commencer             │   │
│  └─────────────────────────────┘   │
│     (Apparaît quand tests OK)       │
└─────────────────────────────────────┘
```

#### Comportement

- **Progression visuelle** : Icône ✓ apparaît quand un test est validé
- **Changement de couleur** : Cartes passent en primaryContainer une fois complétées
- **Calcul automatique** : Zones d'entraînement s'affichent immédiatement après validation
- **Bouton conditionnel** : "Terminer la calibration" n'apparaît que si au moins un test est fait
- **Loading** : CircularProgressIndicator pendant la sauvegarde

---

### Étape 3 : Écran Principal - Liste des Séances

**WorkoutListScreen.kt** - Écran principal avec programme hebdomadaire

#### Apparence visuelle (avec séances)

```
┌─────────────────────────────────────┐
│  Mes Séances                    👤  │  ← TopBar Material
│  (Fond primaire, texte blanc)       │
├─────────────────────────────────────┤
│                                     │
│  ⚠️ Fatigue détectée : RPE 8.2/10   │  ← Alerte en errorContainer
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Endurance Fondamentale      │   │  ← WorkoutCard
│  │ 2026-01-18 • RUNNING        │   │
│  │                             │   │
│  │ Échauffement 10 min + 40    │   │
│  │ min Zone 2 + retour calme   │   │
│  │                             │   │
│  │ 60 min      PACE: 5:30/km   │   │
│  │                             │   │
│  │ Mode Extérieur         [⚪]  │   │  ← Switch Indoor/Outdoor
│  │                             │   │
│  │ [Marquer comme terminée]    │   │
│  └─────────────────────────────┘   │
│   (Carte primaryContainer)          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Fractionné 30/30            │   │
│  │ 2026-01-19 • RUNNING        │   │
│  │                             │   │
│  │ 8x (30s à VMA + 30s récup)  │   │
│  │                             │   │
│  │ 45 min      SPEED: 18.5 km/h│   │
│  │                             │   │
│  │ Mode Extérieur         [⚪]  │   │
│  │                             │   │
│  │ [Marquer comme terminée]    │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Seuil sur Home Trainer      │   │  ← Séance Indoor
│  │ 2026-01-20 • CYCLING        │   │
│  │                             │   │
│  │ 3x10 min @ FTP avec 3 min   │   │
│  │ récupération                │   │
│  │                             │   │
│  │ 50 min      POWER: 280 W    │   │
│  │                             │   │
│  │ Mode Intérieur         [🔵]  │   │  ← Switch activé
│  │                             │   │
│  │ [Marquer comme terminée]    │   │
│  └─────────────────────────────┘   │
│                                     │
│                            [+]      │  ← FloatingActionButton
└─────────────────────────────────────┘
```

#### Apparence visuelle (sans séances)

```
┌─────────────────────────────────────┐
│  Mes Séances                    👤  │
├─────────────────────────────────────┤
│                                     │
│                                     │
│          🏃                         │
│     Aucune séance programmée        │
│                                     │
│   Appuyez sur + pour générer        │
│  votre programme de la semaine      │
│                                     │
│                                     │
│                            [+]      │
└─────────────────────────────────────┘
```

#### Comportement

- **TopBar** : Titre "Mes Séances" + icône profil en haut à droite
- **Alertes intelligentes** :
  - **Fatigue** (errorContainer) : Si ≥3 séances RPE ≥8 sur 7 derniers jours
  - **Progression** (tertiaryContainer) : Si plusieurs séances RPE ≤5
- **Cards dynamiques** :
  - Fond **primaryContainer** pour séances à venir
  - Fond **surfaceVariant** pour séances terminées
  - Badge "✅ Séance terminée" si complétée
- **Switch Indoor/Outdoor** :
  - Change les cibles (PACE → SPEED, POWER → HEART_RATE)
  - Désactivé si séance terminée
- **FAB (+)** : Génère les séances via l'IA Gemini
- **Loading** : CircularProgressIndicator centré pendant la génération
- **Snackbar** : Messages d'erreur en bas de l'écran

---

### Étape 4 : Écran de Profil

**ProfileScreen.kt** - Paramètres et données athlète

#### Apparence visuelle

```
┌─────────────────────────────────────┐
│  ← Mon Profil                       │  ← TopBar avec retour
│  (Fond primaire, texte blanc)       │
├─────────────────────────────────────┤
│                                     │
│  📊 Données physiologiques          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ VMA            18.5 km/h    │   │
│  │ ─────────────────────────── │   │
│  │ FTP            280 W        │   │
│  │ ─────────────────────────── │   │
│  │ FC Max         185 bpm      │   │
│  │ ─────────────────────────── │   │
│  │ Genre          FEMALE       │   │
│  └─────────────────────────────┘   │
│   (Carte surfaceVariant)            │
│                                     │
│  💬 Ton du coaching                 │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ L'IA utilise un ton         │   │
│  │ empathique et encourageant  │   │
│  │ adapté aux athlètes féminins│   │
│  └─────────────────────────────┘   │
│   (Carte secondaryContainer)        │
│                                     │
│  ⚙️ Équipement                      │
│                                     │
│  Capteur de puissance (vélo)  [🔵] │
│  Séances vélo en Watts              │
│                                     │
│  🎯 Zones d'entraînement VMA        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Zone 1 (Récup 60%)  11.1    │   │
│  │ Zone 2 (Endurance)  14.8    │   │
│  │ Zone 3 (Tempo)      16.7    │   │
│  │ Zone 4 (Seuil)      17.8    │   │
│  │ Zone 5 (VMA 100%)   18.5    │   │
│  │ Zone 6 (Anaérobie)  20.4    │   │
│  └─────────────────────────────┘   │
│                                     │
│  🎯 Zones d'entraînement FTP        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Zone 1 (Active Rec)  140 W  │   │
│  │ Zone 2 (Endurance)   196 W  │   │
│  │ Zone 3 (Tempo)       238 W  │   │
│  │ Zone 4 (Seuil)       280 W  │   │
│  │ Zone 5 (VO2max)      322 W  │   │
│  │ Zone 6 (Anaérobie)   392 W  │   │
│  └─────────────────────────────┘   │
│                                     │
│  🤖 Intelligence Artificielle       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Mode : Gemini Flash (Cloud) │   │
│  │                             │   │
│  │ L'IA locale (Gemini Nano)   │   │
│  │ sera disponible prochainement│   │
│  │                             │   │
│  │ Privacy: Seules les données │   │
│  │ anonymes (VMA, FTP, RPE)    │   │
│  │ sont envoyées               │   │
│  └─────────────────────────────┘   │
│   (Carte tertiaryContainer)         │
│                                     │
└─────────────────────────────────────┘
```

#### Comportement

- **TopBar** : Flèche retour + titre "Mon Profil"
- **Sections organisées** : Données physiologiques, ton IA, équipement, zones, IA
- **Calculs dynamiques** : Zones d'entraînement calculées automatiquement par le ViewModel
- **Ton adaptatif** :
  - Genre FEMALE → "Ton empathique et encourageant"
  - Genre MALE → "Ton analytique et direct"
- **Switch équipement** : Modifie le type de cibles pour les séances vélo
- **Informations Privacy** : Rappel que seules les données anonymes sont envoyées au cloud
- **Scroll** : L'écran est scrollable pour voir toutes les sections

---

## 🎨 Palette de Couleurs Material 3

L'application utilise le système de couleurs Material You :

| Élément | Couleur utilisée | Usage |
|---------|------------------|-------|
| TopBar | `primary` | Barre supérieure avec texte blanc |
| Boutons principaux | `primary` | Boutons d'action |
| Cartes actives | `primaryContainer` | Séances à venir, tests complétés |
| Cartes secondaires | `secondaryContainer` | Ton de coaching |
| Cartes tertiaires | `tertiaryContainer` | Information IA, suggestions |
| Alertes erreur | `errorContainer` | Fatigue détectée |
| Fond cartes standard | `surfaceVariant` | Données profil, zones |
| Texte principal | `onSurface` | Texte standard |
| Texte secondaire | `onSurfaceVariant` | Descriptions, labels |

---

## 🔄 Flux de Navigation

```
OnboardingScreen
       ↓
       ├──→ (Si VMA/FTP connus) → WorkoutListScreen
       │
       └──→ (Si VMA/FTP inconnus) → CalibrationScreen
                                           ↓
                                    WorkoutListScreen
                                           ↓
                                           ↔ ProfileScreen
                                         (retour)
```

---

## 🎯 Interactions Clés

### 1. Génération de Séances (WorkoutListScreen)

**Action** : Appui sur le FloatingActionButton (+)

**Comportement** :
1. CircularProgressIndicator s'affiche
2. Appel à l'IA Gemini Flash avec les données anonymes :
   - VMA, FTP, Genre, Disponibilités, RPE moyen
3. Parsing de la réponse JSON
4. Insertion dans la base de données Room
5. Affichage automatique des séances (Flow réactif)
6. Snackbar de succès ou d'erreur

**Prompt envoyé à l'IA** :
```json
{
  "vma": 18.5,
  "ftp": 280,
  "gender": "FEMALE",
  "tone": "empathique et encourageant",
  "last_rpe_avg": 6.5,
  "available_days": ["Monday", "Wednesday", "Friday", "Sunday"]
}
```

**Réponse attendue de l'IA** :
```json
{
  "workouts": [
    {
      "date": "2026-01-20",
      "sport": "RUNNING",
      "title": "Endurance Fondamentale",
      "description": "Échauffement 10 min + 40 min Zone 2 + retour calme",
      "durationMinutes": 60,
      "targetType": "PACE",
      "targetValue": "5:30/km",
      "isIndoor": false
    }
  ],
  "motivational_message": "Belle progression cette semaine ! Continue comme ça 💪"
}
```

### 2. Basculement Indoor/Outdoor

**Action** : Toggle du Switch sur une WorkoutCard

**Comportement** :
1. Mise à jour du champ `isIndoor` dans la base de données
2. Changement automatique du `targetType` :
   - **Outdoor** : PACE (course) / POWER (vélo)
   - **Indoor** : SPEED (tapis) / HEART_RATE (home trainer sans capteur)
3. Mise à jour instantanée de l'UI (Flow réactif)

**Exemple** :
- **Avant** : Mode Extérieur → `PACE: 5:30/km`
- **Après** : Mode Intérieur → `SPEED: 10.9 km/h`

### 3. Détection de Fatigue

**Déclencheur** : Ouverture de WorkoutListScreen

**Logique** (WorkoutRepository.kt:58) :
```kotlin
suspend fun isFatigued(limit: Int = 7): Boolean {
    val tooHardCount = sessionFeedbackDao.countTooHardRecent(limit)
    return tooHardCount >= 3 // 3+ séances avec RPE ≥8
}
```

**Affichage** :
- Carte rouge (errorContainer) en haut de la liste
- Texte : "⚠️ Fatigue détectée : RPE moyen 8.2/10"
- L'IA reçoit cette information pour ajuster les prochaines séances

---

## 📐 Responsive Design

- **Padding standard** : 24dp pour les écrans, 16dp pour les cartes
- **Spacing** : 8dp, 16dp, 24dp, 32dp, 48dp (système cohérent)
- **Scroll automatique** : Tous les écrans sont scrollables verticalement
- **Material 3** : Coins arrondis, élévations subtiles, transitions fluides

---

## 🔒 Sécurité Visuelle

L'utilisateur voit clairement dans ProfileScreen :

> **🤖 Intelligence Artificielle**
>
> Mode : Gemini Flash (Cloud)
>
> Privacy: Seules les données anonymes (VMA, FTP, RPE) sont envoyées

Cela rassure l'utilisateur que :
- Pas de nom
- Pas d'email
- Pas de localisation GPS
- Seulement des métriques d'entraînement

---

## 🎬 Animations Material

- **Transitions d'écran** : Slide horizontal automatique
- **Boutons** : Ripple effect au tap
- **Switch** : Animation fluide de l'indicateur
- **Cards** : Subtle elevation avec ombre
- **FAB** : Animation d'apparition avec scale
- **Loading** : CircularProgressIndicator avec rotation infinie

---

## 📝 Typographie

**Hiérarchie visuelle** (Material Typography Scale) :

- `displaySmall` : Titres principaux (28sp)
- `headlineSmall` : Sections (24sp)
- `titleLarge` : Titres de cartes (22sp)
- `titleMedium` : Sous-titres (16sp)
- `bodyLarge` : Texte principal (16sp)
- `bodyMedium` : Descriptions (14sp)
- `bodySmall` : Hints (12sp)
- `labelLarge` : Labels de données (14sp)

---

## 🎯 Résumé Visuel

L'application **EnduranceFlow** offre :

✅ **Design moderne** avec Material 3
✅ **Navigation intuitive** avec 4 écrans principaux
✅ **Feedback visuel** : Couleurs, icônes, animations
✅ **Adaptabilité** : Indoor/Outdoor, Genre, Équipement
✅ **Intelligence** : Alertes fatigue, suggestions progression
✅ **Privacy-first** : Transparence sur les données envoyées
✅ **Responsive** : Scroll fluide, layout adaptatif

**Le tout propulsé par l'IA Gemini Flash pour des séances personnalisées ! 🚀**
