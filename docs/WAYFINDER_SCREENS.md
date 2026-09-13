# 📱 BodyForger — Screen Wayfinder (UI Navigation & Interaction Map)

This document maps the complete navigation tree, the interface state machines and the interaction contracts for the **Mobile** (phone) and **Wear OS** (watch) applications.

---

## 🗺️ 1. Mobile Navigation Topology

```mermaid
flowchart TD
    AppStart([Application launch]) --> MainScaffold[4-tab scaffold]

    subgraph BottomNav [Bottom navigation bar]
        TabHome[🏠 Home]
        TabWorkout[🏋️ Sessions & routines]
        TabBIA[🧬 Biometrics & BIA]
        TabCatalog[📚 Exercise catalogue]
    end

    MainScaffold --> BottomNav

    %% --- TAB 1: HOME ---
    TabHome --> HomeView[Summary dashboard]
    HomeView -->|Tap 'Start session'| ActiveWorkoutView[Active session screen]
    HomeView -->|Tap BIA tile| TabBIA
    HomeView -->|Tap 'View history'| WorkoutHistoryView[Session history]

    %% --- TAB 2: WORKOUT ---
    TabWorkout --> WorkoutHub[Training hub]
    WorkoutHub -->|Select template| ActiveWorkoutView
    WorkoutHub -->|Create new routine| RoutineEditorView[Routine editor]
    WorkoutHub -->|View history| WorkoutHistoryView

    ActiveWorkoutView -->|Add exercise| ExercisePickerModal[Exercise picker modal]
    ActiveWorkoutView -->|Set completed| RestTimerModal[Rest timer bar/overlay]
    ActiveWorkoutView -->|Finish session| WorkoutSummaryView[End-of-session summary]
    WorkoutSummaryView -->|Save & export| HomeView

    %% --- TAB 3: BIOMETRICS ---
    TabBIA --> BIAHub[Body composition dashboard]
    BIAHub -->|'Weigh in' button| BLEScalePairing[Scan & connect overlay]
    BLEScalePairing -->|Weigh-in complete| BIAReportDetail[5-zone DEXA & water report]
    BIAHub -->|Manage goal| BodyGoalEditor[Milestone editor & 7-day trend]

    %% --- TAB 4: CATALOGUE ---
    TabCatalog --> CatalogListView[Exercise list]
    CatalogListView -->|Filter / search| CatalogListView
    CatalogListView -->|Tap exercise| ExerciseDetailView[Exercise detail sheet]
    ExerciseDetailView -->|Calculate| OneRMCalculator[Epley/Brzycki 1RM calculator]
```

---

## ⌚ 2. Wear OS Navigation Topology (Wrist-First)

```mermaid
flowchart TD
    WearLaunch([Watch launch]) --> WearHome[Home / selection screen]

    WearHome -->|Start a free session| WearLiveWorkout[Active session - live workout]
    WearHome -->|Select a synced routine| WearLiveWorkout

    subgraph WorkoutLifecycle [Session lifecycle on the wrist]
        WearLiveWorkout -->|Set validated| WearRestTimer[Circular rest timer]
        WearRestTimer -->|Time up or 'Skip'| WearLiveWorkout

        WearLiveWorkout -->|Crown / +- buttons| WearSetAdjust[Load & rep adjustment]
        WearLiveWorkout -->|Swipe left| WearExerciseList[Ordered exercise list]
        WearLiveWorkout -->|Wrist lowered| WearAmbientMode[Ambient AOD mode, 1 Hz]
        WearAmbientMode -->|Wrist raised| WearLiveWorkout
    end

    WearLiveWorkout -->|Long press 'Finish'| WearSummary[Session summary & heart rate]
    WearSummary -->|Sent to the phone over the Data Layer| WearHome
```

---

## 📋 3. Detailed Screen Specification & Interaction Contracts

### 📱 Mobile screens (Android Jetpack Compose)

| Screen | Role | Input data | Actions / events raised |
| :--- | :--- | :--- | :--- |
| **`HomeScreen`** | Daily overview, motivation and quick access. | `UserStats`, `NextScheduledWorkout`, `LastBodyLog`. | `onStartWorkout()`, `onOpenBIA()`, `onViewHistory()`. |
| **`WorkoutScreen` (active)** | Driving the live session, with timer and live heart rate. | `ActiveWorkoutSession`, `LiveHeartRate`. | `onLogSet(weight, reps, type)`, `onAddExercise()`, `onFinishWorkout()`. |
| **`RoutineEditorScreen`** | Creating and editing session templates (split, PPL, upper/lower). | `Routine?` (when editing). | `onAddExercise()`, `onReorderExercises()`, `onSaveRoutine()`. |
| **`ExerciseDetailScreen`** | Execution guide, primary/secondary target muscles, 1RM history. | `exerciseId: String`. | `onAddToCurrentWorkout()`, `onCalculate1RM()`. |
| **`BiometricsScreen`** | Clinical body-composition analysis and BLE weigh-in. | `BiaProfile`, `List<BodyLog>`. | `onTriggerBLEScan()`, `onUpdateGoal()`, `onExportHealthConnect()`. |
| **`CatalogScreen`** | Explorer for the exercise catalogue, with instant search. | `SearchFilter(muscle, equipment)`. | `onSelectExercise()`, `onApplyFilter()`. |

---

### ⌚ Wear OS screens (Compose for Wear OS)

| Screen | Role | Specific interactions | Edge cases |
| :--- | :--- | :--- | :--- |
| **`WearLiveWorkoutScreen`** | Large-format display of the current set, with live heart rate. | • Giant `VALIDATE SET` button<br>• Rotating crown to adjust the weight. | Screen lockable against sweat (`Water Lock`). |
| **`WearRestTimerScreen`** | Visual and haptic recovery countdown. | • Depleting circular ring<br>• `+30s` and `SKIP` buttons. | Vibration continues even in deep sleep, via `AlarmManager.setAlarmClock()`. |
| **`WearAmbientScreen`** | Low-power always-on display at 1 Hz. | • Absolute black background, 100 % OLED<br>• Minimal timer + BPM display. | Compose animations disabled to avoid burning CPU. |
| **`WearSummaryScreen`** | End-of-training summary on the wrist. | • Duration, average/max BPM, total volume (kg). | Persisted locally to Room immediately, before any Bluetooth sync attempt. |

---

## 🛡️ 4. Edge-Case Resolution Matrix

1. **Bluetooth link lost between watch and phone**:
   * *Behaviour*: watch and phone each carry on fully autonomously, writing to their own local Room database.
   * *Recovery*: synchronisation happens automatically on reconnection through `WearableDataLayerManager`, reconciled on UTC timestamps.
2. **Unexpected close or flat battery mid-session**:
   * *Behaviour*: every validated set is persisted transactionally to Room straight away (`atomic insert`).
   * *Recovery*: on restart, the application offers a *"Resume the session in progress"* button.
3. **BLE weigh-in interrupted (stepping off the scale early)**:
   * *Behaviour*: a safety timeout after 25 seconds if the four telemetry fragments do not arrive.
   * *User feedback*: a clear message — *"Incomplete weigh-in — stay still on the scale until the beep"*.
