# ADR 001: Offline Synchronisation Architecture, Wear OS Independence & Cloud Backup Strategy

## 📋 Status
**Accepted & Validated** (2026-08-28)

---

## 🎯 1. Context & Problem

BodyForger is a strength-training and biometric tracking application (DEXA / 8-electrode BIA) built on a **local-first** paradigm. The ecosystem comprises:
1. **An Android mobile application** (primary source of truth, progressive-overload calculations, BIA charts).
2. **A standalone Wear OS application** (autonomous session execution at the gym without a phone, heart-rate capture, haptic rest timer, Ambient AOD mode).
3. **A Google Health Connect gateway** (interoperability with Samsung Health, Google Fit, Withings).
4. **An optional cloud backup layer** (restore when changing device).

The critical constraints are:
* **Frequent Bluetooth disconnection**: at the gym, the watch must work fully without a phone nearby.
* **Wear OS battery life**: avoid continuous sensor polling and needless processor wake-ups.
* **Resilience to crashes and flat batteries**: atomic persistence, set by set.
* **Data integrity**: avoid duplicates and synchronisation conflicts.

---

## 🏛️ 2. Architecture Decisions

### A. Device roles & the append-only model
* **Immutability of sessions (`WorkoutSession` & `WorkoutSet`)**:
  * Every session and set receives a unique `UUID` the moment it is created, whether it started on the watch or on the phone.
  * Finished sessions are treated as **append-only** (not mutable).
  * Room writes use `OnConflictStrategy.REPLACE` / `IGNORE` keyed on the UUID, which makes synchronisation **absolutely idempotent**.
* **Monotonic timestamps**: for editable data (user profile, a renamed routine), conflict resolution applies **last-write-wins (LWW)** on `updatedAtEpochMs`.

### B. Wear OS ↔ mobile synchronisation protocol (Wearable Data Layer)
1. **Reconciliation by inventory (UUID handshake)**:
   * Never rely on one-off `onDataChanged` events alone — they can be missed when the app is asleep or killed by the OEM.
   * On every Bluetooth/Wi-Fi reconnection:
     1. Watch and phone exchange the inventory of their session UUIDs (`session_inventory_request` / `session_inventory_response`).
     2. Any delta missing on either side is transferred again automatically.
2. **Payload format & the DataItem limit (100 KB)**:
   * A typical strength session (15 to 25 sets with aggregated BPM) weighs **5 to 15 KB** as JSON.
   * Transfers go through `DataClient` (a compact DataItem).
   * If continuous high-frequency data is enabled, the payload is compressed (GZIP) or transferred over `ChannelClient`.
3. **Protocol versioning (`schemaVersion`)**:
   * Every Data Layer message carries `schemaVersion: 1`, so the model can evolve without breaking when watch and phone run different app versions.

### C. Battery and sensor management on Wear OS
* **Health Services API (`ExerciseClient`)**:
  * Wear OS `HealthServices` is used exclusively, rather than reading raw sensors through `SensorManager`.
  * Hardware batching of heart rate and calories is delegated to the OS.
* **Foreground service & ongoing activity**:
  * A foreground service with a permanent notification (`OngoingActivity`) is held, so the OS does not kill the runner mid-exercise.

### D. Synchronisation flags in the Room DB
Every entity stored in the Room DB carries a synchronisation status:
* `LOCAL_ONLY`: created locally, not yet propagated.
* `SYNCED_PEER`: synchronised between watch and phone.
* `SYNCED_CLOUD`: archived in the cloud.

### E. Cloud backup strategy (Firestore / Storage)
* **Aggregation into a single document**:
  * To respect the free quotas (Spark: 20,000 writes/day) and the 1 MiB per-document limit: **one session = one Firestore document**.
  * High-level metadata (date, duration, total volume, Health Connect categories) are indexable fields.
  * The set-by-set detail is stored in a nested array/blob.
* **A restore procedure that can be tested**:
  * A full restore option ("Restore cloud data") on a clean install.
  * A local file export/import option (`bodyforger_backup.json`) to guarantee the user's data sovereignty.

---

## ⚖️ 3. Consequences & Benefits

* **Fully reliable offline**: the user can leave their phone in the locker room and train with the watch alone.
* **Zero duplicates**: UUID idempotence eliminates duplicated sessions.
* **Battery savings**: Health Services batching plus a local Data Layer, rather than direct network requests during a session.
* **No risk of blowing the cloud quota**: one Firestore write per finished session.
