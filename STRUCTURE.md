# 📁 Structure du projet EnduranceFlow_Android

## Organisation par Fonctionnalité

Ce projet utilise une architecture **Clean Architecture** organisée par **fonctionnalités**.

```
app/src/main/java/com/enduranceflow/
│
├── 🎯 core/                    # Code partagé par toute l'application
│   ├── data/                   # Sources de données communes (API, DB)
│   ├── domain/                 # Modèles et logique métier partagés
│   └── ui/                     # Composants UI réutilisables
│
├── 👤 onboarding/              # Fonctionnalité : Premier démarrage
│   ├── data/                   # Sauvegarde des infos utilisateur
│   ├── domain/                 # Validation du profil
│   └── ui/                     # Écrans de bienvenue
│
├── 📊 calibration/             # Fonctionnalité : Tests VMA/FTP
│   ├── data/                   # Enregistrement des résultats
│   ├── domain/                 # Calculs des zones d'entraînement
│   └── ui/                     # Écrans de test et résultats
│
├── 🏃 workout/                 # Fonctionnalité : Séances d'entraînement
│   ├── data/                   # Historique et synchronisation
│   ├── domain/                 # Adaptation Indoor/Outdoor
│   └── ui/                     # Écrans de séance en cours
│
├── 🎖️ profile/                 # Fonctionnalité : Profil athlète
│   ├── data/                   # AthleteProfile, EquipmentConfig
│   ├── domain/                 # Gestion du profil
│   └── ui/                     # Écran de paramètres
│
├── 🤖 ai/                      # Fonctionnalité : Moteur IA
│   ├── data/                   # Gemini Nano / Flash API
│   └── domain/                 # Strategy Pattern et fallback
│
└── 💉 di/                      # Dependency Injection (Hilt)
    └── Modules de configuration
```

## 📂 Autres dossiers importants

```
app/src/main/
├── res/                        # Ressources Android
│   ├── drawable/               # Images et icônes
│   ├── layout/                 # Layouts XML (si nécessaire)
│   ├── values/                 # Strings, colors, themes
│   └── navigation/             # Navigation graphs
│
└── assets/                     # Fichiers bruts (JSON, etc.)

app/src/test/                   # Tests unitaires
app/src/androidTest/            # Tests UI (Compose)
```

## 🎯 Principe de Clean Architecture

Chaque fonctionnalité suit 3 couches :

1. **UI (Presentation)** : Écrans Jetpack Compose + ViewModels
2. **Domain** : Logique métier pure (indépendante d'Android)
3. **Data** : Accès aux données (Room, API, Health Connect)

## 🔄 Flux de données

```
UI ➜ ViewModel ➜ UseCase (Domain) ➜ Repository (Data) ➜ DataSource
```

---

**Avantage** : Si demain on veut supprimer la fonctionnalité "Calibration", on supprime juste le dossier `calibration/` !
