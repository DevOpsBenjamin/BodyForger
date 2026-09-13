# 🗺️ BodyForger — Wayfinder & Architecture Map

This document maps every data flow, state machine and interaction between the contexts of the BodyForger ecosystem.

---

## 🧭 1. Topology of the Business Contexts

```mermaid
flowchart TB
    subgraph Context_Gym [1. Training context / openGym]
        ExDB[(Exercise catalogue)]
        Routines[Routines & split plans]
        Workout[Workout session engine]
        Volume[Heatmap & 1RM calculations]

        ExDB --> Routines
        Routines --> Workout
        Workout --> Volume
    end

    subgraph Context_Wear [2. Standalone Wear OS context]
        WearUI[Compose for Wear OS UI]
        HS_Client[Health Services ExerciseClient]
        Ambient[Ambient AOD observer]
        HapticTimer[Haptic rest timer]
        WearDB[(Wear local Room DB)]

        WearUI --> WearDB
        HS_Client -->|BPM / calories| WearDB
        WearUI --> Ambient
        WearUI --> HapticTimer
    end

    subgraph Context_BIA [3. Biometrics & BLE scales context]
        ScaleDriver[GATT driver registry]
        Haige[Haige crypto driver]
        DexaEngine[8-electrode DEXA BIA engine]
        Milestones[Automatic milestone validation]

        ScaleDriver --> Haige
        Haige -->|4-fragment frame| DexaEngine
        DexaEngine --> Milestones
    end

    subgraph Context_Sync [4. Synchronisation & ecosystem context]
        DataLayer[Wearable Data Layer API]
        MobileDB[(Phone local Room DB)]
        HealthConnect[Google Health Connect client]
        MCPServer[MCP server for Gemini / AI]

        MobileDB --> HealthConnect
        MobileDB <--> MCPServer
    end

    Workout <==>|Bluetooth / Wi-Fi sync| WearDB
    ScaleDriver -.->|Direct BLE on the watch| WearDB
    ScaleDriver -.->|Direct BLE on the phone| MobileDB
    WearDB <==>|DataClient sync| DataLayer
    DataLayer <==> MobileDB
```

---

## ⚡ 2. State Machine: Running a Workout Session

```mermaid
stateDiagram-v2
    [*] --> Idle : Application at rest
    Idle --> RoutineSelected : A template is picked, or freestyle
    RoutineSelected --> SessionActive : Session started

    state SessionActive {
        [*] --> InExercise
        InExercise --> SetExecution : Load and reps prepared
        SetExecution --> SetCompleted : Set validated

        SetCompleted --> RestTimerActive : Rest timer starts automatically
        state RestTimerActive {
            [*] --> CountdownRunning : Screen active or Ambient AOD
            CountdownRunning --> WarningVibration : -3 seconds (3 pulses)
            WarningVibration --> FinishVibration : 0 seconds (long burst)
            FinishVibration --> [*]
        }
        RestTimerActive --> InExercise : Ready for the next set

        state Intensifiers {
            DropSetExecution : Main set + immediate drops
            RestPauseExecution : Activation set + decomposed clusters
        }
    }

    SessionActive --> SessionFinalized : Session stopped
    SessionFinalized --> LocalPersisted : Written to the Room DB (duration, heart rate, sets)
    LocalPersisted --> HealthConnectExport : ExerciseSessionRecord + HeartRateRecord written
    HealthConnectExport --> [*]
```

---

## ⚖️ 3. State Machine: Connected BLE Weigh-In

```mermaid
sequenceDiagram
    autonumber
    actor User as Athlete
    participant App as BodyForger (watch or phone)
    participant Scale as BLE body composition scale
    participant BIA as DEXA BIA engine
    participant HC as Google Health Connect

    User->>App: Triggers the weigh-in / steps onto the scale
    App->>Scale: Targeted BLE scan + GATT connection
    Scale-->>App: Step 1: REQUEST_AUTH (nonce exchange)
    App->>Scale: Step 2: AUTH_TOKEN (HMAC-SHA256 with the root key)
    App->>Scale: Step 3: WORK_KEY (encrypted session key injection)
    App->>Scale: Steps 4 & 5: enable 8 electrodes & TIME_SYNC
    App->>Scale: Step 6: SET_USER_INFO (69-byte user profile)
    Note over Scale: Physical weigh-in (~18s) - no BLE traffic
    Scale-->>App: Step 7: 4 encrypted fragments on 0x97 (mass, heart rate, 5-zone impedances)
    App->>BIA: Decryption & DEXA computation (fat %, SMM, TBW, somatotype)
    BIA-->>App: Full physiological report generated
    App->>HC: WeightRecord & BodyFatRecord written
    App->>User: Haptic confirmation + report shown
```

---

## 🤖 4. AI Integration Flow (MCP Server)

```mermaid
sequenceDiagram
    autonumber
    actor User as Athlete
    participant AI as Gemini / Claude assistant
    participant MCP as BodyForger MCP server
    participant DB as BodyForger database
    participant Watch as Wear OS watch

    User->>AI: "Build me a push routine tuned to my progress"
    AI->>MCP: get_body_metrics()
    MCP-->>AI: BIA: 17.5% BF, +1.5kg SMM, chest plateau
    AI->>MCP: search_exercises(target="chest", intensifier="dropset")
    MCP-->>AI: Real catalogue ids (e.g. 0009, 0017, 1254)
    AI->>MCP: push_workout_routine(name="Push Hypertrophy", exercises=[...])
    MCP->>DB: The new routine is inserted
    DB->>Watch: Automatic Bluetooth sync
    Watch-->>User: Notification on the watch: "New session ready to start!"
```

---

## 🎯 5. Responsibilities by Module

| Module | Role & responsibility |
| :--- | :--- |
| `app-wear` | Wear OS interface, `Health Services` background service, always-on display (AOD) handling, haptic vibration. |
| `app-mobile` | Android phone interface, dashboard, dynamic neon charts, routine editor, BIA reports. |
| `core-model` | Immutable domain entities (`WorkoutSession`, `WorkoutSet`, `Exercise`, `BodyLog`, `BiaProfile`). |
| `core-database` | Shared Room DB persistence, DAOs, local migrations. |
| `core-bia` | Pure DEXA mathematical engine (body composition and water compartments). |
| `core-ble` | GATT driver registry (Haige family and Bluetooth standard). |
| `core-healthconnect` | Google Health Connect read/write adapter (`PlannedExercise`, `ExerciseSession`, `HeartRate`). |
| `server-mcp` | Model Context Protocol server for AI interoperability with Gemini. |
| `web` | Showcase site and documentation, deployed on Cloudflare Pages (`bodyforger.app`). |

---

## 📑 6. Index of Architecture Decisions (ADR)

* [**ADR 001: Offline Synchronisation Architecture, Wear OS Independence & Cloud Backup Strategy**](adr/001-offline-sync-wear-phone-cloud.md)
