# 📋 BodyForger — Master Plan & Technical Architecture

## 🌟 Vision & Background

**BodyForger** is a native, offline-first, wrist-first fitness and body composition suite designed to merge the best of two open-source projects into one unified ecosystem:

1. **SimpleBodyGraph** (Personal DEXA-calibrated BIA engine, Huawei Scale 3 reverse-engineered BLE GATT driver, tape measurements, body fat / lean mass goals).
2. **openGym** (1,300+ exercise library, animations, comprehensive set mechanics with drop-sets, rest-pause, warmups, and volume progression).

The motivation stems from daily frustrations with commercial solutions like **Hevy**, particularly:
- Weak, dependent Wear OS companion apps that freeze or fail when the screen goes to sleep.
- Disconnected silos between workout logs and body composition (scales, body fat, hydration).
- Delayed or incomplete synchronization with **Google Health Connect**.

---

## 🏗️ The 4 Core Pillars

```
+---------------------------------------------------------------------------------------+
|                                      BodyForger                                       |
+-----------------------+-------------------------------+-------------------------------+
|  1. WRIST-FIRST WEAR  |  2. CLINICAL BIA & SCALES     |  3. ADVANCED WORKOUT CORE     |
|  - Health Services    |  - HUAWEI Scale 3 + Standard  |  - 1,300+ Exercises (openGym) |
|  - Continuous HR      |  - DEXA BIA 8-Electrode Model |  - Drop-Sets & Rest-Pause     |
|  - Ambient / Screen-Off| - Tape Measurements          |  - Heatmap, Volume & 1RM      |
|  - Rest Haptics       |  - Milestone Paliers          |  - Routine & Split Builder    |
+-----------------------+-------------------------------+-------------------------------+
|                               4. GOOGLE HEALTH & AI ECOSYSTEM                         |
|   PlannedExerciseSessionRecord • ExerciseSessionRecord (HR series) • MCP Routine Sync |
+---------------------------------------------------------------------------------------+
```

---

## 🔬 Deep Technical Specifications

### ⌚ 1. Wear OS Autonomous Engine & Screen-Off Management
* **`ExerciseClient` (Health Services API)**:
  * Manages the workout lifecycle through a native Android Foreground Service.
  * Captures real-time heart rate (BPM) directly via the low-power sensor hub coprocessor, ensuring continuous monitoring even in sleep mode.
  * Updates `OngoingActivityNotification` to display a live workout chip on the active watch face.
* **Ambient Mode Lifecycle (`AmbientLifecycleObserver`)**:
  * Switches automatically to a dimmed, pure-black 1 Hz interface when the wrist is lowered.
  * Prevents OS termination while saving OLED battery life.
* **Haptic Vibration Countdown (`Vibrator`)**:
  * Triggered via background coroutine timer: 3 distinct warning pulses at -3s, followed by 1 strong burst at 0s.

### 🧬 2. Clinical Bio-Impedance (BIA) & BLE Scale Driver

#### 2.1. HUAWEI Scale 3 Pro GATT Profile & Crypto Handshake
* **Protocol**: Proprietary HaigeBLE GATT service.
* **GATT Characteristic Map**:
  | Step | Handle | UUID | Access | Role |
  | :--- | :--- | :--- | :--- | :--- |
  | Sentinel | `0x71` | `ba216311-1787-472b-bef6-3eb29e62293e` | Notify | Global status sentinel |
  | Step 1 | `0x21` | `02b2a08e-f8b0-4047-b1fd-f4e0efeee679` | WriteCmd / Indicate | `REQUEST_AUTH` (Nonce exchange) |
  | Step 2 | `0x25` | `32330a04-15d9-421a-91c5-2a2d5c7525c9` | WriteCmd / Indicate | `AUTH_TOKEN` (HMAC-SHA256 mutual auth) |
  | Step 3 | `0x29` | `a3d330f8-b84f-4f48-a78c-f8d1e33b597a` | WriteCmd / Indicate | `WORK_KEY` (Session key injection) |
  | Step 4 | `0xd7` | `0000fe01-0000-1000-8000-00805f9b34fb` | WriteCmd / Notify | Enable 8-electrode capability (`5a0005...`) |
  | Step 5 | `0x52` | `00002a2b-0000-1000-8000-00805f9b34fb` | WriteCmd / Indicate | `TIME_SYNC` (Epoch synchronization) |
  | Step 6 | `0x31` | `8cc61d7d-66c0-4802-89c3-38c5a163592e` | WriteCmd / Indicate | `SET_USER_INFO` (User profile payload 69B) |
  | Step 7 | `0x97` | `46797c17-d639-488d-9476-4789e8472878` | Indicate | `REALTIME_WEIGHT` (4 encrypted telemetry packets) |

#### 2.2. DEXA BIA Mathematical Engine
* **Multifrequency Compartment Modeling**:
  * $TBW = \alpha \cdot \frac{H^2}{Z_{50}} + \beta \cdot M + \gamma \cdot \text{Age} + \delta$
  * $ECW = f(Z_{low}, H, M)$, $ICW = TBW - ECW$, Ratio $\frac{ECW}{TBW} \approx 0.38 - 0.40$ (clinical norm).
  * $FFM = \frac{TBW}{0.732}$, $BF\% = \frac{M - FFM}{M} \times 100$.
  * $SMM$ (Skeletal Muscle Mass), $SMI = \frac{ASMM}{H^2}$.
  * 5-Zone Segmental Distribution (Trunk, Right/Left Arms, Right/Left Legs).

### 🏋️ 3. Strength & Exercise Mechanics (openGym)

#### 3.1. Set Data Model
Sets use a dual-axis discriminator:
```kotlin
enum class SetPhase { WORK, WARMUP }
enum class SetType { STRAIGHT, DROPSET, RESTPAUSE }

data class WorkoutSet(
    val id: String,
    val phase: SetPhase = SetPhase.WORK,
    val type: SetType = SetType.STRAIGHT,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double? = null,
    val isCompleted: Boolean = false,
    val drops: List<DropSubSet> = emptyList(),      // Additional drops for dropsets
    val clusters: List<ClusterSubSet> = emptyList() // Decomposition for rest-pause
)
```

### 🔄 4. Google Health Connect & MCP AI Sync

#### 4.1. Health Connect Data Mapping
* **`PlannedExerciseSessionRecord`**: Structured workouts with exercise blocks and target sets/reps.
* **`ExerciseSessionRecord`**: Completed workouts mapped to `EXERCISE_TYPE_STRENGTH_TRAINING` or `CALISTHENICS`.
* **`HeartRateRecord`**: Continuous BPM time-series data points captured from the watch.

#### 4.2. Model Context Protocol (MCP) Server
* **Endpoints**:
  * `get_body_metrics()`: Return current BIA status (weight, body fat %, muscle mass, trend).
  * `search_exercises(query, muscle_group, equipment)`: Query 1,300+ catalogue exercises.
  * `push_workout_routine(plan_json)`: Inject AI-generated routines into BodyForger's Room/Cloud DB.

---

## 🗂️ Project Structure

```
BodyForger/
├── web/                       # 🌐 Showcase Landing Page (Vue 3 + Tailwind v4 for Cloudflare)
├── app-mobile/                # 📱 Android Mobile Application (Jetpack Compose)
├── app-wear/                  # ⌚ Wear OS Standalone Application (Compose for Wear OS)
├── core-model/                # 🧱 Shared Kotlin Data Models (Workout, Set, Exercise, BIA, Log)
├── core-database/             # 💾 Shared Room DB entities, DAOs, and migrations
├── core-ble/                  # 📡 BLE GATT drivers (Huawei Scale 3, Generic 0x181D)
├── core-bia/                  # 🧬 DEXA BIA Mathematical Engine in Kotlin
├── core-healthconnect/        # 💓 Health Connect Read/Write Adapters
├── core-sync/                 # 🔄 Wearable Data Layer Sync Engine
├── server-mcp/                # 🤖 Model Context Protocol Server for Gemini / AI routines
├── AGENTS.md                  # 📜 Development directives and CI/GH protocols
├── PLAN.md                    # 📋 Master technical architecture
└── README.md
```

---

## 🚀 Roadmap

- [x] **Phase 0**: Architecture & repository initialization as **BodyForger**.
- [ ] **Phase 1**: Port BIA Engine & Scale 3 BLE Driver to Kotlin Android/Wear module.
- [ ] **Phase 2**: Import openGym exercise database (1,300+ exercises) & workout models into `core-model`.
- [ ] **Phase 3**: Build standalone Wear OS workout runner (Health Services HR + Ambient AOD + Haptics).
- [ ] **Phase 4**: Wearable Data Layer bidirectional synchronization (Watch ↔ Phone).
- [ ] **Phase 5**: Google Health Connect exporter (Completed Sessions, HR series, Planned Exercises).
- [ ] **Phase 6**: BodyForger MCP Server for Gemini workout generation.
- [ ] **Phase 7**: UI Polish & Release.
