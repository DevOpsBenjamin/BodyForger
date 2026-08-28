# 🗺️ BodyForger — Wayfinder & Architecture Map

Ce document cartographie l'ensemble des flux de données, machines à états et interactions entre les différents contextes de l'écosystème BodyForger.

---

## 🧭 1. Carte Topologique des Contextes Métier

```mermaid
flowchart TB
    subgraph Context_Gym [1. Contexte Entraînement / openGym]
        ExDB[(Catalogue 1300+ Exos)]
        Routines[Routines & Split Plans]
        Workout[Workout Session Engine]
        Volume[Heatmap & Calculs 1RM]
        
        ExDB --> Routines
        Routines --> Workout
        Workout --> Volume
    end

    subgraph Context_Wear [2. Contexte Wear OS Standalone]
        WearUI[Compose for Wear OS UI]
        HS_Client[Health Services ExerciseClient]
        Ambient[Ambient AOD Observer]
        HapticTimer[Haptic Rest Timer]
        WearDB[(Wear Local Room DB)]

        WearUI --> WearDB
        HS_Client -->|BPM / Calories| WearDB
        WearUI --> Ambient
        WearUI --> HapticTimer
    end

    subgraph Context_BIA [3. Contexte Biométrie & Balances BLE]
        ScaleDriver[GATT Driver Registry]
        Huawei3[Huawei Scale 3 / Pro Crypto]
        DexaEngine[Calculateur BIA DEXA 8-Electrode]
        Paliers[Validation Auto Paliers]
        
        ScaleDriver --> Huawei3
        Huawei3 -->|Trame 4 Fragments| DexaEngine
        DexaEngine --> Paliers
    end

    subgraph Context_Sync [4. Contexte Synchronisation & Écosystème]
        DataLayer[Wearable Data Layer API]
        MobileDB[(Phone Local Room DB)]
        HealthConnect[Google Health Connect Client]
        MCPServer[Serveur MCP pour Gemini / IA]

        MobileDB --> HealthConnect
        MobileDB <--> MCPServer
    end

    Workout <==>|Bluetooth / Wi-Fi Sync| WearDB
    ScaleDriver -.->|Direct BLE sur Montre| WearDB
    ScaleDriver -.->|Direct BLE sur Téléphone| MobileDB
    WearDB <==>|DataClient Sync| DataLayer
    DataLayer <==> MobileDB
```

---

## ⚡ 2. Machine à États : Déroulement d'une Séance (Workout Session)

```mermaid
stateDiagram-v2
    [*] --> Idle : Application au repos
    Idle --> RoutineSelected : Sélection d'un modèle ou Freestyle
    RoutineSelected --> SessionActive : Démarrage de la séance
    
    state SessionActive {
        [*] --> InExercise
        InExercise --> SetExecution : Préparation de la charge/reps
        SetExecution --> SetCompleted : Validation de la série
        
        SetCompleted --> RestTimerActive : Lancement automatique chrono repos
        state RestTimerActive {
            [*] --> CountdownRunning : Écran actif ou Ambient AOD
            CountdownRunning --> WarningVibration : -3 secondes (3 pulses)
            WarningVibration --> FinishVibration : 0 seconde (Burst long)
            FinishVibration --> [*]
        }
        RestTimerActive --> InExercise : Prêt pour la série suivante
        
        state Intensifiers {
            DropSetExecution : Série principale + Décharges immédiates
            RestPauseExecution : Série activation + Clusters décomposés
        }
    }

    SessionActive --> SessionFinalized : Arrêt de la séance
    SessionFinalized --> LocalPersisted : Enregistrement Room DB (Durée, Cardio, Séries)
    LocalPersisted --> HealthConnectExport : Écriture ExerciseSessionRecord + HeartRateRecord
    HealthConnectExport --> [*]
```

---

## ⚖️ 3. Machine à États : Pesée Connectée BLE (Huawei Scale 3 / Pro)

```mermaid
sequenceDiagram
    autonumber
    actor User as Athlète
    participant App as BodyForger (Montre ou Téléphone)
    participant Scale as Balance HUAWEI Scale 3 Pro
    participant BIA as Moteur DEXA BIA
    participant HC as Google Health Connect

    User->>App: Déclenche la pesée / Monte sur la balance
    App->>Scale: Scan BLE ciblé + Connexion GATT
    Scale-->>App: Étape 1 : REQUEST_AUTH (Échange Nonces)
    App->>Scale: Étape 2 : AUTH_TOKEN (HMAC-SHA256 avec RootKey)
    App->>Scale: Étape 3 : WORK_KEY (Injection clé de session chiffrée)
    App->>Scale: Étape 4 & 5 : Enable 8-Electrodes & TIME_SYNC
    App->>Scale: Étape 6 : SET_USER_INFO (Profil utilisateur 69 octets)
    Note over Scale: Pesée physique (~18s) - Zéro émission BLE
    Scale-->>App: Étape 7 : 4 fragments chiffrés sur 0x97 (Poids, Rythme, Impédances 5 zones)
    App->>BIA: Déchiffrement & Calcul DEXA (Gras %, Muscle SMM, Eau TBW, Somatotype)
    BIA-->>App: Rapport physiologique complet généré
    App->>HC: Écriture WeightRecord & BodyFatRecord
    App->>User: Confirmation haptique + Affichage du rapport
```

---

## 🤖 4. Flux d'Intégration IA (Serveur MCP)

```mermaid
sequenceDiagram
    autonumber
    actor User as Athlète
    participant AI as Gemini / Claude Assistant
    participant MCP as Serveur MCP BodyForger
    participant DB as Base de Données BodyForger
    participant Watch as Montre Wear OS

    User->>AI: "Génère-moi une routine Push optimisée selon mes progrès"
    AI->>MCP: get_body_metrics()
    MCP-->>AI: BIA: 17.5% BF, +1.5kg SMM, stagnation pectoraux
    AI->>MCP: search_exercises(target="chest", intensifier="dropset")
    MCP-->>AI: IDs réels du catalogue (ex: 0009, 0017, 1254)
    AI->>MCP: push_workout_routine(name="Push Hypertrophy", exercises=[...])
    MCP->>DB: Insertion de la nouvelle routine
    DB->>Watch: Sync automatique Bluetooth
    Watch-->>User: Notification sur la montre : "Nouvelle séance prête à être lancée !"
```

---

## 🎯 5. Résumé des Responsabilités par Module

| Module | Rôle & Responsabilité |
| :--- | :--- |
| `app-wear` | Interface Wear OS, Service d'arrière-plan `Health Services`, Gestion de l'affichage Always-On (AOD), Vibration haptique. |
| `app-mobile` | Interface Smartphone Android, Tableau de bord, Graphiques dynamiques néon, Éditeur de routines, Rapports BIA. |
| `core-model` | Entités de domaine immutables (`WorkoutSession`, `WorkoutSet`, `Exercise`, `BodyLog`, `BiaProfile`). |
| `core-database` | Persistance Room DB partagée, DAOs, migrations locales. |
| `core-bia` | Moteur mathématique pur DEXA (calculs de composition corporelle et compartiments hydriques). |
| `core-ble` | Registre des pilotes GATT (Huawei Scale 3 et standard Bluetooth). |
| `core-healthconnect` | Adaptateur de lecture/écriture Google Health Connect (`PlannedExercise`, `ExerciseSession`, `HeartRate`). |
| `server-mcp` | Serveur Model Context Protocol pour l'interopérabilité IA avec Gemini. |
| `web` | Site vitrine et documentation déployé sur Cloudflare Pages (`bodyforger.app`). |
