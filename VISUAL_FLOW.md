# Flux Visuel - EnduranceFlow Android

Ce document contient des diagrammes visuels interactifs de l'application.

## 🔄 Diagramme de Navigation

```mermaid
graph TB
    Start[🚀 Lancement App] --> Onboarding[📱 OnboardingScreen]

    Onboarding --> Decision{VMA/FTP<br/>connus ?}

    Decision -->|Non| Calibration[📊 CalibrationScreen]
    Decision -->|Oui| WorkoutList[🏃 WorkoutListScreen]

    Calibration --> WorkoutList

    WorkoutList --> Profile[👤 ProfileScreen]
    Profile --> WorkoutList

    WorkoutList --> AI{Appui sur +}
    AI --> GeminiFlash[🤖 Gemini Flash API]
    GeminiFlash --> Parse[📄 Parse JSON]
    Parse --> Database[(💾 Room Database)]
    Database --> WorkoutList

    style Start fill:#4285F4,stroke:#1967D2,color:#fff
    style Onboarding fill:#34A853,stroke:#137333,color:#fff
    style Calibration fill:#FBBC04,stroke:#F9AB00,color:#000
    style WorkoutList fill:#EA4335,stroke:#C5221F,color:#fff
    style Profile fill:#9334E6,stroke:#7627BB,color:#fff
    style AI fill:#FF6D00,stroke:#E65100,color:#fff
    style GeminiFlash fill:#00BCD4,stroke:#0097A7,color:#fff
    style Database fill:#607D8B,stroke:#455A64,color:#fff
```

## 🎨 Architecture des Composants UI

```mermaid
graph LR
    subgraph "🎨 UI Layer (Compose)"
        A[OnboardingScreen] --> B[OnboardingViewModel]
        C[CalibrationScreen] --> D[CalibrationViewModel]
        E[WorkoutListScreen] --> F[WorkoutListViewModel]
        G[ProfileScreen] --> H[ProfileViewModel]
    end

    subgraph "💼 Domain Layer"
        B --> I[ProfileRepository]
        D --> I
        F --> J[WorkoutRepository]
        F --> K[AIRepository]
        H --> I
    end

    subgraph "💾 Data Layer"
        I --> L[(Room Database)]
        J --> L
        K --> M[AIEngineFactory]
        M --> N[GeminiFlashEngine]
        M --> O[GeminiNanoEngine]
    end

    subgraph "☁️ External"
        N --> P[Gemini API]
        O --> Q[On-Device AI]
    end

    style A fill:#B2DFDB,stroke:#00897B
    style C fill:#C5CAE9,stroke:#5C6BC0
    style E fill:#FFCCBC,stroke:#FF7043
    style G fill:#E1BEE7,stroke:#AB47BC
    style P fill:#FFF9C4,stroke:#FBC02D
    style Q fill:#F8BBD0,stroke:#E91E63
```

## 📊 Cycle de Vie d'une Séance

```mermaid
stateDiagram-v2
    [*] --> Génération: Appui sur FAB +

    Génération --> Collecte: ViewModel.generateWeeklyWorkouts()

    Collecte --> Envoi: Récupération VMA, FTP, Genre, RPE

    Envoi --> API: AIRepository.generateWeeklyWorkouts()

    API --> Cloud: GeminiFlashEngine appelle API

    Cloud --> Parsing: Réponse JSON reçue

    Parsing --> Validation: parseWorkoutsFromResponse()

    Validation --> Sauvegarde: Données valides
    Validation --> Erreur: Données invalides

    Sauvegarde --> Database: Room.insert()

    Database --> Affichage: Flow<List<DailyWorkoutEntity>>

    Affichage --> EnAttente: Séance créée (isCompleted = false)

    EnAttente --> Indoor: Toggle Indoor/Outdoor
    Indoor --> EnAttente

    EnAttente --> Terminée: Marquer comme terminée

    Terminée --> Feedback: Saisie RPE

    Feedback --> Archive: sessionFeedback enregistré

    Archive --> [*]

    Erreur --> [*]: Snackbar d'erreur
```

## 🔄 Indoor/Outdoor - Transformation des Cibles

```mermaid
graph TB
    subgraph "🏃 Mode Course"
        A1[Mode Extérieur] -->|Course| B1[TargetType: PACE]
        B1 --> C1["Affichage: 5:30/km"]

        A2[Mode Intérieur] -->|Tapis| B2[TargetType: SPEED]
        B2 --> C2["Affichage: 10.9 km/h"]
    end

    subgraph "🚴 Mode Vélo"
        D1[Mode Extérieur] -->|Vélo route| E1[TargetType: POWER]
        E1 --> F1["Affichage: 280 W"]

        D2[Mode Intérieur] -->|Home Trainer| E2{Capteur de<br/>puissance ?}
        E2 -->|Oui| F2[TargetType: POWER]
        F2 --> G2["Affichage: 280 W"]
        E2 -->|Non| F3[TargetType: HEART_RATE]
        F3 --> G3["Affichage: 160 bpm"]
    end

    style A1 fill:#4CAF50,stroke:#388E3C,color:#fff
    style A2 fill:#2196F3,stroke:#1976D2,color:#fff
    style D1 fill:#FF9800,stroke:#F57C00,color:#fff
    style D2 fill:#9C27B0,stroke:#7B1FA2,color:#fff
```

## ⚠️ Détection de Fatigue - Flowchart

```mermaid
graph TD
    Start[Ouverture WorkoutListScreen] --> Load[ViewModel.loadWorkouts]

    Load --> CheckFatigue[WorkoutRepository.isFatigued]

    CheckFatigue --> Query[SessionFeedbackDao.countTooHardRecent 7 jours]

    Query --> Count{Nombre de séances<br/>avec RPE ≥ 8}

    Count -->|< 3| NoAlert[Pas d'alerte]
    Count -->|≥ 3| ShowAlert[Afficher alerte rouge]

    NoAlert --> CheckProgression[WorkoutRepository.needsProgression]
    ShowAlert --> CheckProgression

    CheckProgression --> QueryEasy[SessionFeedbackDao.countTooEasyRecent 7 jours]

    QueryEasy --> CountEasy{Nombre de séances<br/>avec RPE ≤ 5}

    CountEasy -->|< 3| NoSuggestion[Pas de suggestion]
    CountEasy -->|≥ 3| ShowSuggestion[Afficher suggestion orange]

    NoSuggestion --> Display[Afficher liste séances]
    ShowSuggestion --> Display

    Display --> End[Utilisateur voit l'état]

    style ShowAlert fill:#FFCDD2,stroke:#C62828,color:#000
    style ShowSuggestion fill:#FFE082,stroke:#F57F17,color:#000
    style NoAlert fill:#C8E6C9,stroke:#388E3C,color:#000
    style NoSuggestion fill:#C8E6C9,stroke:#388E3C,color:#000
```

## 🤖 Prompt IA - Structure de Données

```mermaid
graph LR
    subgraph "📤 Données Envoyées à Gemini"
        A[AthleteProfile] --> B{Genre}
        B -->|FEMALE| C[Ton: empathique]
        B -->|MALE| D[Ton: analytique]

        E[VMA: 18.5 km/h]
        F[FTP: 280 W]
        G[RPE moyen: 6.5]
        H[Disponibilités: Lun, Mer, Ven]

        C --> I[Prompt complet]
        D --> I
        E --> I
        F --> I
        G --> I
        H --> I
    end

    subgraph "📥 Réponse de Gemini"
        I --> J[JSON Response]
        J --> K[workouts: Array]
        K --> L[Workout 1: Endurance]
        K --> M[Workout 2: Fractionné]
        K --> N[Workout 3: Seuil]

        J --> O[motivational_message: String]
    end

    subgraph "💾 Parsing et Sauvegarde"
        L --> P[DailyWorkoutEntity 1]
        M --> Q[DailyWorkoutEntity 2]
        N --> R[DailyWorkoutEntity 3]

        P --> S[(Room Database)]
        Q --> S
        R --> S

        O --> T[Affichage Snackbar]
    end

    style I fill:#E3F2FD,stroke:#1976D2
    style J fill:#FFF3E0,stroke:#F57C00
    style S fill:#E8F5E9,stroke:#388E3C
```

## 🔐 Architecture Privacy-First

```mermaid
graph TB
    subgraph "📱 Appareil Local"
        A[Profil utilisateur] --> B{Préparation données}
        B --> C[✅ VMA: 18.5]
        B --> D[✅ FTP: 280]
        B --> E[✅ Genre: FEMALE]
        B --> F[✅ RPE moyen: 6.5]

        B --> G[❌ Nom: filtré]
        B --> H[❌ Email: filtré]
        B --> I[❌ GPS: filtré]
        B --> J[❌ Identifiants: filtrés]
    end

    subgraph "☁️ Cloud Gemini"
        K[Données anonymes uniquement]
        C --> K
        D --> K
        E --> K
        F --> K

        K --> L[Génération séances]
        L --> M[Réponse JSON]
    end

    subgraph "🔒 Sécurité"
        N[BuildConfig.GEMINI_API_KEY]
        N --> O[Lecture local.properties]
        O --> P[Injection au build]
        P --> Q[.gitignore protège le fichier]
    end

    M --> R[Retour à l'appareil]

    style G fill:#FFCDD2,stroke:#C62828,color:#000
    style H fill:#FFCDD2,stroke:#C62828,color:#000
    style I fill:#FFCDD2,stroke:#C62828,color:#000
    style J fill:#FFCDD2,stroke:#C62828,color:#000
    style C fill:#C8E6C9,stroke:#388E3C,color:#000
    style D fill:#C8E6C9,stroke:#388E3C,color:#000
    style E fill:#C8E6C9,stroke:#388E3C,color:#000
    style F fill:#C8E6C9,stroke:#388E3C,color:#000
    style Q fill:#FFF9C4,stroke:#F57F17,color:#000
```

## 📈 Timeline d'Utilisation

```mermaid
gantt
    title Parcours Utilisateur Type (Première Semaine)
    dateFormat YYYY-MM-DD
    section Jour 1
    Installation & Onboarding     :done, 2026-01-18, 5m
    Tests de Calibration (VMA/FTP):done, 2026-01-18, 30m
    section Jour 2
    Génération programme semaine  :done, 2026-01-19, 1m
    Séance 1: Endurance           :active, 2026-01-19, 60m
    Feedback RPE                  :done, 2026-01-19, 1m
    section Jour 4
    Séance 2: Fractionné 30/30    :2026-01-21, 45m
    Feedback RPE                  :2026-01-21, 1m
    section Jour 6
    Séance 3: Seuil vélo          :2026-01-23, 50m
    Feedback RPE                  :2026-01-23, 1m
    section Jour 7
    Consultation profil & zones   :2026-01-24, 5m
    Génération nouvelle semaine   :2026-01-24, 1m
```

## 🎨 Composants Material 3

```mermaid
graph TB
    subgraph "Material Design 3"
        A[Theme Material You] --> B[Color Scheme]
        B --> C[Primary: #4285F4]
        B --> D[Secondary: #34A853]
        B --> E[Tertiary: #FBBC04]
        B --> F[Error: #EA4335]

        A --> G[Typography Scale]
        G --> H[Display: 28sp]
        G --> I[Headline: 24sp]
        G --> J[Title: 22sp]
        G --> K[Body: 16sp]

        A --> L[Components]
        L --> M[Button with ripple]
        L --> N[Card with elevation]
        L --> O[Switch animated]
        L --> P[FAB with scale]
        L --> Q[TextField outlined]

        A --> R[Motion]
        R --> S[Transitions slide]
        R --> T[Animations fade]
        R --> U[Gestures swipe]
    end

    style A fill:#BB86FC,stroke:#6200EA,color:#000
    style C fill:#4285F4,stroke:#1967D2,color:#fff
    style D fill:#34A853,stroke:#137333,color:#fff
    style E fill:#FBBC04,stroke:#F9AB00,color:#000
    style F fill:#EA4335,stroke:#C5221F,color:#fff
```

---

## 📊 Visualisation GitHub

Ces diagrammes sont rendus automatiquement sur GitHub grâce à Mermaid.
Pour les voir en couleur :

1. **Sur GitHub** : Ouvrez ce fichier dans votre repository
2. **Localement** : Utilisez [Mermaid Live Editor](https://mermaid.live/)
3. **VS Code** : Installez l'extension "Markdown Preview Mermaid Support"

## 🚀 Pour Voir l'Application Réelle

### Option 1 : Android Studio
```bash
cd /home/user/App
# Ouvrir dans Android Studio
# Sync Gradle
# Lancer sur émulateur (Ctrl+R)
```

### Option 2 : Build APK
```bash
./gradlew assembleDebug
# APK dans : app/build/outputs/apk/debug/app-debug.apk
```

### Option 3 : Instant Run
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

**Ces diagrammes montrent visuellement comment l'application fonctionne ! 🎨**
