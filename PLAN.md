# 📋 BodyForger — Master Plan & Technical Architecture

## 🌟 Vision & Background

**BodyForger** is a native, offline-first, wrist-first fitness and body composition suite designed to merge the best of two open-source projects into one unified ecosystem:

1. **SimpleBodyGraph** (Personal DEXA-calibrated BIA engine, Huawei Scale 3 reverse-engineered BLE GATT driver, tape measurements, body fat / lean mass goals).
2. **openGym** — for how it models a set: drop-sets, rest-pause, warm-ups and volume progression. Its exercise library is not imported; BodyForger seeds its own, deliberately small catalogue.

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
|  - Health Services    |  - BLE Scales + Standard      |  - Curated Exercise Catalogue  |
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
A set carries one kind, not two axes — a warm-up is a kind of set like any other:
```kotlin
enum class RoutineSetType { NORMAL, WARMUP, DROPSET, FAILURE, REST_PAUSE }

data class WorkoutSet(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String = "",
    val exerciseId: String,
    val type: RoutineSetType = RoutineSetType.NORMAL,
    val weightKg: Double = 0.0,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val reps: Int = 0,
    val rpe: Double? = null,
    val isCompleted: Boolean = false,
    val side: UnilateralSide = UnilateralSide.NONE,
    val restTimeSeconds: Int = 90,
    val completedAtEpochMs: Long? = null
)
```
A drop-set and a rest-pause are **kinds of set**, not sets containing sub-sets: each drop and
each cluster is its own row, ordered by `setIndex`. Tonnage and volume therefore count them
without special cases, and nothing has to be flattened before it is stored.

`side` exists for unilateral exercises, which are logged left then right in strict order.

### 🔄 4. Google Health Connect & MCP AI Sync

#### 4.0. Where the export runs — and where it cannot

⚠️ **Health Connect has no provider on Wear OS.** `HealthConnectClient.getSdkStatus()` returns
`SDK_UNAVAILABLE` on the watch, checked on a real device; Samsung's developer documentation states
the same. The watch therefore **cannot export anything itself**, whatever permissions it holds —
Wear OS 6 adopting Health Connect's granular `android.permission.health.*` names is about
permission vocabulary, not about hosting the datastore.

The export consequently runs on the phone, and the watch hands its sessions over:

1. The watch logs autonomously into its own Room database (ADR 001 §A).
2. On reconnection, the Data Layer carries the session across (ADR 001 §B).
3. A `WearableListenerService` on the phone receives it. The system binds that service when a
   data item arrives and unbinds it afterwards, and it can start the app if it is not running —
   so **no foreground service, no notification, and no battery cost between sessions**.
4. That service writes to Health Connect.

The athlete never opens the app. The phone is still required; taking it out is not.

#### 4.1. Health Connect Data Mapping
* **`PlannedExerciseSessionRecord`**: Structured workouts with exercise blocks and target sets/reps.
* **`ExerciseSessionRecord`**: Completed workouts mapped to `EXERCISE_TYPE_STRENGTH_TRAINING` or `CALISTHENICS`.
* **`HeartRateRecord`**: Continuous BPM time-series data points captured from the watch.

#### 4.2. Model Context Protocol (MCP) Server
* **Endpoints**:
  * `get_body_metrics()`: Return current BIA status (weight, body fat %, muscle mass, trend).
  * `search_exercises(query, muscle_group, equipment)`: Query the exercise catalogue.
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

Phases are listed in the order they are worked, which is no longer their numbering: the export
comes before the watch, because it is what teaches the record shapes the watch will later need.

- [x] **Phase 0** — Architecture & repository initialization as **BodyForger**.
- [x] **Phase 1** — BIA engine & BLE scale driver. *Done and past the original scope: the driver
  runs pairing, HUID engraving, the encrypted handshake, the tare and telemetry decoding, held by
  110 tests; ForgeFit MIT fills every field of `BodyCompositionReport` from published equations.*
- [x] **Phase 2** — Workout models & exercise catalogue. *The domain models, routines, the live
  session and its persistence are in place, and 124 exercises are seeded — a catalogue kept
  deliberately small, with the athlete free to add their own. Importing openGym's library was
  considered and dropped.*
- [ ] **Phase 5 — next** — Health Connect exporter, on the phone. *`core-healthconnect` is a stub.
  The mapping it needs already exists: `HealthConnectExerciseType` carries 46 canonical types with
  their `segmentTypeId`. The phone already produces real sessions and real weigh-ins that go
  nowhere. See §4.0 — the phone is the only device that can export at all.*
- [ ] **Phase 3** — Standalone Wear OS workout runner (Health Services HR + ambient AOD + haptics).
  *`app-wear` is an interface shell: it declares `core-ble`, `core-bia`, `core-database` and
  `core-sync` and calls none of them. The weigh-in screen is a `delay()` state machine that
  reports success without a scale in the room.*
- [ ] **Phase 4** — Wearable Data Layer synchronisation, and the `WearableListenerService` that
  exports on receipt. *`WearableDataLayerManager` exists and nothing emits to it. Waits on
  Phase 3 for something to carry.*
- [ ] **Phase 6** — BodyForger MCP server for Gemini workout generation. *`server-mcp` does not
  exist; it consumes everything above.*
- [ ] **Phase 7** — UI polish & release. *Blocked on removing `fallbackToDestructiveMigration`,
  and on the hardcoded demo figures still displayed as measurements.*
