# ForgeFit MIT — the open composition engine

ForgeFit MIT is BodyForger's own body-composition engine, the one this repository ships. Every number it produces comes from a
**published, peer-reviewed equation** or a **published anthropometric constant**, plus one
physics-based frequency conversion. No coefficient is taken from any manufacturer's firmware,
and nothing is tuned to reproduce a manufacturer's output. This document is the map from each
reported figure to its source.

The engine lives in `core-bia/ForgeFitModel.kt`; the segmental stage in
`core-bia/SegmentalDistribution.kt`. The Kirchhoff decomposition of the six measured paths
into five segments is shared with the rest of the app — see `docs/BIA_ENGINE.md` §2.

---

## 1. Fat-free mass

Two published equations, merged by their published precision (inverse-variance weighting on
each equation's standard error of estimate):

| Equation | Path | Population | SEE |
| :--- | :--- | :--- | ---: |
| **Sun / NHANES** (Chumlea et al.) | hand-to-foot, whole body | US adults, sex-specific | 3.90 / 2.90 kg |
| **Wu et al. 2011** | foot-to-foot, the legs | adults, sex as a term | 3.17 kg |

When only the four-electrode (foot-to-foot) path exists, Wu alone is used; when only
hand-to-foot exists, Sun alone.

### Dual frequency, used honestly

Both equations are calibrated at **50 kHz**. The 250 kHz reading is therefore first converted
to its 50 kHz-equivalent — impedance is systematically lower at 250 kHz because the current
crosses cell membranes — before the validated equation is applied:

```
Z50-equivalent = Z250 / TYPICAL_FREQUENCY_RATIO      (≈ 0.887)
FFM = 0.25 · f(Z50) + 0.75 · f(Z50-equivalent)
```

The 3/4 weight on the (converted) 250 reading is a design choice, verified non-critical. The
converted 250 still moves faster over time, which is where a genuine recomposition shows up.

Fat mass and body-fat % follow directly: `fat = mass − FFM`.

---

## 2. Skeletal muscle mass

**Janssen et al. 2000**, the resistance-only form, from the whole-body (hand-to-foot) reading.
Published, DXA-validated.

---

## 3. The four compartments

Fat-free mass splits into water, protein and bone mineral by the **Western Brozek 4C**
fractions (`docs/BIA_ENGINE.md` §4). The extracellular/total-water ratio is nudged from the
250/50 impedance ratio around the clinical norm (~0.38).

---

## 4. Basal metabolism and metabolic age

* **BMR** — Katch-McArdle: `370 + 21.6 · FFM` kcal/day. Published, driven by fat-free mass
  rather than total mass, so it tracks a recomposition.
* **Metabolic body age** — `age + clamp(BMI − 25, −5, +20)`, bounded to 18–80.

---

## 5. Segmental muscle, segmental fat, visceral index

The whole-body figures above are distributed over the five segments using **only published
anthropometric constants and each limb's own impedance** — no manufacturer coefficient. The
per-limb impedances come from the Kirchhoff decomposition (`docs/BIA_ENGINE.md` §2).

### Muscle

A limb's **segmental resistance index** `L²/Z` is the standard segmental-BIA predictor of its
lean tissue. But limb lengths alone do not carry the true leg-to-arm muscle ratio, so:

* appendicular muscle = `SMM × 0.75` — **Kim et al. 2002**;
* of that, lower limbs hold **70 %**, upper limbs 30 % — population distribution;
* within each pair (left vs right), the split follows `L²/Z` — this is where the impedance
  earns its keep: it reads the **left/right asymmetry** a lifter actually wants;
* the trunk is the remainder, `SMM − appendicular`.

By construction the five segments sum to SMM.

### Fat

There is **no clean published equation for a single limb's fat** from BIA (manufacturers keep
theirs proprietary). ForgeFit therefore constructs it, transparently:

* a limb's total mass = body mass × its published mass fraction — **de Leva 1996 / Winter**
  (per arm ≈ 5 %, per leg ≈ 20 %);
* its **non-muscular** mass = limb mass − limb muscle (floored at zero, so no limb ever shows
  negative fat);
* total fat is spread over the segments in proportion to that non-muscular mass;
* the trunk takes the remainder — the only honest place for trunk fat, whose impedance BIA
  measures poorly.

Each brick is published; the assembly is ForgeFit's own engineering, not a cited equation. Its
accuracy is bounded by the muscle estimate it rests on.

### Visceral level

Every consumer scale defines its **own arbitrary visceral scale** — there is no universal
medical equation from impedance alone. ForgeFit's index is deliberately its own, physiologically
motivated, and transparent:

```
visceral = round( trunkFat · 0.70 + max(0, age − 30) · 0.10 ),  bounded 1–59
```

It rises with trunk fat and, past middle age, with age — the two established drivers of
visceral adiposity. It is **not** a medical visceral-adipose-tissue measurement, and is not
calibrated against any manufacturer's number.

### Body score

A single **0-100** summary of composition — BodyForger's own scale, deliberately not
calibrated to any manufacturer's score. No public definition of such a score exists, so it is
built from **logical, transparent penalties** rather than a fitted equation:

```
score = 100
        − max(0, fat% − healthy)       · 1.6     (healthy: men 18, women 27)
        − max(0, essential − fat%)     · 1.5     (essential ≈ 8 %: too lean also costs)
        − max(0, visceral − 9)         · 1.2     (central fat is the real risk)
        + min(6, max(0, SMI − 8) · 2.5)          (muscle lifts, but capped)
        bounded to 40–100
```

It moves the right way: a lean, muscular athlete lands at 100; someone obese but muscular in the
mid-70s (the muscle is credited, the obesity still marked); someone obese and untrained in the
50s. It is a motivational summary, not a clinical measurement.

---

## Every field is filled

ForgeFit MIT populates every field of `BodyCompositionReport`. The segmental figures
(muscle, fat, visceral) require the eight-electrode paths and are `null` on a foot-to-foot-only
reading — honestly absent rather than faked. ForgeFit Private differs only in *how* it computes
each figure, not in *which* it reports.
