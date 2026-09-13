# Google Play Store Listing — BodyForger

This file holds **two listings**, and the difference matters on submission day.

§3 is the **target listing**, describing BodyForger at its 1.0 release. It is the vision, and it
is what the roadmap is working towards. §4 is the **shippable listing**, describing what an
installer gets today. Each section of §3 is tagged with its status.

⚠️ **Do not paste §3 into the Play Console until every section reads `shipped`.** Google enforces
its Misrepresentation policy on functionality a listing claims and an app does not have, and the
penalty is suspension rather than a request to edit. Until then, §4 is what goes in the console.

---

## 1. App Name
**Max:** 30 characters

BodyForger

---

## 2. Short Description
**Max:** 80 characters

| | Text | Length |
| --- | --- | --- |
| **Target** | Wrist-first strength training & DEXA-grade body composition tracker. | 68 |
| **Today** | Strength training and honest body composition, from your own BLE scale. | 71 |

---

## 3. Full Description — target, at 1.0
**Max:** 4000 characters

Forge your physique with honest measurement, standalone watch execution, and complete data privacy.

BodyForger is a personal strength-training and body-composition tracker built for athletes who
train wrist-first. It pairs a fully autonomous Wear OS workout engine with multi-frequency
bio-impedance analysis and clean Google Health Connect export.

**1. STANDALONE WEAR OS ENGINE** — *not yet built (Phase 3)*
Train phone-free. Your entire session runs directly on your watch as a foreground service:
• Always-On Display (AOD 1 Hz) optimised for minimal battery drain
• Continuous heart-rate monitoring on the low-power sensor coprocessor
• Rest timer with distinct haptic alerts you feel without looking
• Fast set, rep and weight adjustments using the rotating crown
• Offline-first: sessions are recorded locally on the watch

**2. BODY COMPOSITION FROM YOUR OWN SCALE** — *shipped*
Connect directly to smart scales over Bluetooth Low Energy:
• Native GATT integration, including the encrypted handshake proprietary scales require
• Multi-frequency capture at 50 and 250 kHz, across up to six impedance paths
• Every raw resistance stored verbatim, by name, exactly as the scale reported it
• Segmental muscle and fat across five zones, plus hydration compartments and a visceral index
• Composition computed from published, DXA- and 4C-validated equations — every coefficient has a
  citation, and none is copied from a manufacturer's app

**3. NOTHING INVENTED, EVER** — *shipped*
A quantity the hardware did not measure is absent, never substituted:
• Step on without gripping the handle and you get a mass, not a fabricated body fat percentage
• A four-electrode scale reports the foot-to-foot path and says so, rather than inventing five more
• No default value is ever stored — a made-up figure would be indistinguishable from a real
  measurement once it is in your history

**4. WORKOUT LOGGING** — *shipped*
• A curated exercise catalogue, searchable by muscle group and equipment, extendable with your own
  movements
• Advanced set types: warm-up, drop set, rest-pause, to failure, and RPE
• Automatic 1RM estimation (Epley)
• Routine and split builder, session history, muscle heatmap and training consistency
• Every validated set is written to the database as you go, so a crash or a flat battery costs you
  nothing

**5. GOOGLE HEALTH CONNECT & AI** — *not yet built (Phases 5 and 6)*
• Properly typed session export to Google Health Connect
• Heart-rate series and workout logs written cleanly, never as opaque blobs
• Model Context Protocol (MCP) support, so an AI assistant can read your metrics and write routines

**6. PRIVACY & LOCAL-FIRST COMMITMENT** — *shipped*
• Your data belongs to you: stored locally on your device (Room / SQLite)
• Bluetooth is used only to talk to your scale, and sensor data is processed on-device
• Zero advertising, zero tracking, zero data brokers
• Open source under the MIT License

Forge your body. Master your metrics.

---

## 4. Full Description — shippable today

Identical to §3 with sections 1 and 5 removed, and the opening line dropping "standalone watch
execution". Everything else already reads true.

---

## 5. Claims that are not coming back

These were in an earlier draft. They are not pending work — they are wrong or abandoned, and
coding will not make them true.

| Claim | Why |
| --- | --- |
| "1,300+ exercises with execution instructions and animations" | The catalogue holds 124, with neither instructions nor animations. The figure was openGym's, and importing their library has been dropped by decision: the catalogue stays deliberately small. |
| "Myo-reps" | No such set type. `RoutineSetType` is `NORMAL`, `WARMUP`, `DROPSET`, `FAILURE`, `REST_PAUSE`. Restore it only once a value exists and the UI can log it. |
| "Brzycki" 1RM model | Only Epley is implemented — `TrainingStats.kt`. Restore it once a second formula exists and is selectable. |
| "Progress tracked against median trends" | The 7-day median on the home screen is a hardcoded placeholder, not a computation. Restore it once the trend is computed from the weigh-in history. |
| "Segmental tracking calibrated against DEXA reference models" | ForgeFit MIT is built **from** published DXA/4C-validated equations. It is not calibrated against a DEXA scan of its user. For a health app the difference is not cosmetic — §3.2 is worded accordingly. |

## 6. Before submitting

- A privacy policy activity handling `ACTION_SHOW_PERMISSIONS_RATIONALE` is required the day
  Health Connect permissions are declared.
- The app still displays hardcoded demo figures — a fixed date, a fixed athlete name, a fixed
  session and a fixed weight delta. A reviewer opening the app sees them.
- `fallbackToDestructiveMigration()` must be gone before the first release, or the first schema
  change wipes every user's history.
