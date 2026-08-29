# 📖 Domain Model (Glossary) — BodyForger

This document is the canonical reference for the business terms and concepts used across the entire BodyForger ecosystem (Mobile, Wear OS, BIA Engine, openGym Core, Health Connect).

---

## 0. Scope: Audience & Boundaries

### Target User
The athlete BodyForger is designed for: someone who trains with a **Wear OS watch on the wrist**, away from their phone, and who wants what they record to land in **Google Health Connect** as properly typed records. They typically own a **bioelectrical impedance scale** and care about raw impedance as much as the computed result.

Health Connect is a **destination, not a dependency**: the application holds its own database, history and statistics, and keeps working with the export switched off (see *Health Connect Telemetry*).

This is not a technical prerequisite — the app blocks no one. It is a **design frame**: every trade-off is settled in favour of this usage, and usages further from it are deliberately less well served.

The corollary is accepted: an athlete who trains phone-in-hand, without a watch, or outside the Google Health ecosystem, is **explicitly pointed towards other applications** better suited to their need.

---

## 1. Context: Strength Training & Progression (Gym)

### Exercise
A physical training unit referenced in the catalogue (1,300+ entries). Defined by a unique identifier, a canonical name, a body part (`bodyPart`), required equipment (`equipment`), a primary target muscle (`target`), and secondary muscles (`secondaryMuscles`).

### Routine (Programme / Session Template)
A reusable workout template, or one planned for a given day (e.g. Push, Pull, Legs, Upper, Lower). It holds an ordered list of exercises with target prescriptions (set count, rep range, default rest time, planned intensifiers).

### Workout Session
The actual instance of a workout performed by the user at a precise timestamp. It records the start time, end time, effective duration, active calories burned, the continuously measured heart-rate time series, and the ordered list of exercises with the sets actually completed.

### Set
The atomic unit of effort within an exercise. A set carries two orthogonal classifications:
1. **Phase**:
   * `Warmup` (warm-up or activation set, not counted towards effective working volume).
   * `Work` (effective working set).
2. **Type (Intensifier)**:
   * `Straight` (classic set: fixed load and reps).
   * `DropSet` (a main set followed immediately by one or more load reductions with no rest).
   * `RestPause` / `MyoReps` (an activation set followed by short rep clusters separated by 10–20 second micro-rests).

### Drop
A work subset within a `DropSet`. Represents a reduced load (e.g. −20%) performed immediately after the previous set with no rest.

### Cluster
A subset of a `RestPause`. The breakdown of a rep total into small bursts (e.g. 12 reps split into [6, 3, 2, 1]) separated by a micro-rest.

### 1RM (One Repetition Maximum)
The theoretical maximum load an athlete can lift for a single repetition, computed from an effective set using validated formulas (Epley or Brzycki).

### Muscle Heatmap & Volume
The weekly aggregation of effective working (`Work`) set counts applied to each anatomical muscle group, visualised as a body heatmap.

---

## 2. Context: Biometrics & Body Composition (BIA & Scales)

### Body Log
A daily record attached to the athlete. It always carries body mass in kilograms, the calendar date (`YYYY-MM-DD`), the body fat percentage, and the **Measurement Capability** it came from. Where the hardware provides them, it also carries the **Raw Impedances**, heart rate, and the precise timestamp emitted by the scale itself.

Body fat percentage is **always present**: it comes either from the scale or from a manual entry by the athlete. It is not to be confused with the **Body Composition Report**, which exists only when raw impedances were measured.

### Raw Impedances
The only quantity a bioelectrical impedance scale actually **measures**: the electrical resistances of the body, in ohms, taken along distinct **anatomical paths** and at one or more **frequencies**.

A dual-frequency 8-electrode scale produces twelve: six paths (left foot ↔ right foot, left hand ↔ right hand, left hand ↔ left foot, left hand ↔ right foot, right hand ↔ left foot, right hand ↔ right foot) measured first at low then at high frequency. A path is not a body zone: all six paths cross both the limbs and the trunk.

Raw impedances are **kept verbatim and forever**. This is what makes it possible to recompute the entire history retroactively when the body composition equations evolve, and to aggregate a period by analysing the median of the resistances rather than an average of results.

### Segmental Composition
The breakdown of muscle and fat mass by anatomical region — trunk, right arm, left arm, right leg, left leg.

This analysis is **entirely derived**: each limb's impedance is deduced from the **Raw Impedances** by solving Kirchhoff's laws. It is therefore never stored, but recomputed on demand. Confusing this derived layer with the measurement itself is the mistake to avoid.

### Measurement Trueness vs Repeatability
Bioelectrical impedance is **not** a DEXA scan, and is never exact. In absolute terms it is off by several points of body fat; what it does well is **repeat itself**. The quantity that carries meaning is therefore the **trend across measurements**, never the figure of a single day.

Two consequences follow, and they govern every choice made about body composition:

* A formula that is slightly wrong but **applied consistently to the whole history** is worth more than a truer formula applied to only part of it. Changing the calculation without recomputing the past manufactures a progression that came from the code rather than from the athlete.
* This is why derived quantities are **never stored** and the **Raw Impedances** are kept forever: the day an equation improves, the entire history moves with it and the curve stays honest.

Chasing absolute accuracy against the scale's own figure is a category error. The instruments of truth here are consistency over time and the rolling median (as already used to validate a **Palier**), not agreement to the decimal.

### Measurement Capability
The level of fidelity a **Body Log** originates from, recorded alongside it because it determines which quantities are legitimately computable:

* **Dual-frequency, 8 electrodes** — scale with a retractable handle: full and segmental body composition.
* **Single-frequency** — whole-body impedance at one frequency: global composition only.
* **Weight only** — scale without electrodes: no body composition.
* **Manual** — mass and body fat declared by the athlete.

A quantity a capability cannot measure is **absent**, never substituted by a default value: a fabricated figure would be indistinguishable from a real measurement in the history.

### Lean Mass Compartments
The chemical split of **fat-free mass** into water, protein and bone mineral. BodyForger uses the Western **Brozek 4C** constants — 0.732 water, 0.211 protein, 0.057 bone mineral — not the scale manufacturer's.

The scale is an **instrument, not the reference**: the goal is to measure with BodyForger's own constants, calibrated for the athlete it serves, rather than to reproduce the manufacturer's app.

This split is a layer of its own. It leaves fat-free mass, body fat percentage and every **Segmental Composition** value untouched — those follow from the impedances, not from the chemistry.

### Manufacturer Cross-Check
Comparing BodyForger's figures against the scale's own is a **gross-error detector, not an accuracy target**. Its tolerance is the kilogram: an arm going from 2 to 6 kg is a bug worth hunting; a few hundred grams, or a point of body fat, is noise and is ignored.

Reducing a sub-kilogram gap against the manufacturer is never in itself a reason to change the model.

### BIA Profile
The set of the user's physiological constants (biological sex, date of birth / age, height in cm) required by the multi-frequency bioelectrical impedance equations.

### Hydration Compartments
* **TBW** (Total Body Water): total body water content in litres.
* **ICW** (Intracellular Water): water held inside the muscle cells.
* **ECW** (Extracellular Water): interstitial and plasma water (water retention).
* **ECW/TBW Ratio**: clinical indicator of fluid balance (optimal norm ~0.38 – 0.40).

### Palier (Milestone Goal)
An intermediate target threshold (mass and body fat percentage). It is validated automatically when the stable weekly trend (7-day rolling median) crosses the required threshold.

### Measurement (Tape Measurement)
An anatomical circumference entry in centimetres (chest, waist, arms, thighs, calves, neck).

---

## 3. Context: Hardware & Bluetooth LE (GATT)

### ScaleManager
The central orchestrator responsible for Bluetooth Low Energy scanning, selecting the appropriate hardware driver, managing the connection lifecycle, and forwarding frames to the application (on Smartphone or Wear OS).

### ScaleDriver
The hardware adaptation component dedicated to a family of scales (e.g. `HuaweiScale3Driver`, `StandardGattScaleDriver`), encapsulating encryption, pairing steps, and telemetry frame decoding. It exposes the **Weigh-In Session** as a sequence of observable states, so the interface stays generic and unaware of the hardware.

### Weigh-In Session
The complete course of a weigh-in, from the athlete's intent through to the recording of the **Body Log**. It is not an instantaneous operation but an observable sequence of several tens of seconds, which the athlete follows and can interrupt.

It unfolds in three phases, the first of which is physical and must be stated to the athlete:

1. **Wake-up** — the athlete taps the scale, or briefly steps on it, so that it announces itself; without this it stays invisible.
2. **Negotiation** — connection, authentication and profile configuration, **with the athlete off the platform**.
3. **Measurement** — the athlete is invited to step on barefoot; weight stabilisation follows, then the impedance reading.

A session belongs to the device that started it and ends there. Both watch and phone are equally capable; since the scale accepts only one at a time, the second simply fails.

### Association (Paired Scale)
The durable link between the athlete and a given scale: the device's physical address, **HUID**, calibration tare, and hardware model.

The Association is created **once only**, by either device, then shared between watch and phone. As long as it exists, every later weigh-in uses it directly; pairing is never replayed.

### HUID (Huawei User ID)
A virtual identifier assigned to the scale during initial pairing, to segment and authorise the user profile in the scale's internal flash memory. **One HUID per athlete**, regardless of which device performed the pairing: two distinct identifiers would occupy two memory slots on the scale and split the history into two people.

---

## 4. Context: Watch & Wrist-First Execution (Wear OS)

### Autonomous Workout Runner
The standalone execution engine on the Wear OS watch. It runs in the foreground (`Foreground Service`) without depending on an active connection to the smartphone.

### Health Services Session (`ExerciseClient`)
The official Android hardware session that delegates continuous heart-rate (BPM) sensor reading and calorie computation to the watch's low-power coprocessor.

### Ambient State (AOD Mode)
The power-saving Always-On Display state (1 Hz refresh, pure black background, high contrast) engaged when the wrist is lowered, keeping the session and timers running without interruption by the OS.

### Rest Timer & Haptic Sequence
The recovery timer between two sets. It fires a programmed haptic sequence (warning vibrations at −3s, long end vibration at 0s) perceivable by the athlete without looking.

---

## 5. Context: Ecosystem & Artificial Intelligence (Health & AI)

### Health Connect Telemetry
The export to Google Health Connect:
* `ExerciseSessionRecord` (strength sessions with exercise segments).
* `HeartRateRecord` (continuous BPM time series).
* `PlannedExerciseSessionRecord` (plans and scheduled sessions).
* `WeightRecord` & `BodyFatRecord` (weight and body fat measurements).

### MCP Server (Model Context Protocol)
The interface server exposing BodyForger's data to AI assistants (Gemini, Claude) in order to generate tailored training programmes based on the user's actual body composition trend.
