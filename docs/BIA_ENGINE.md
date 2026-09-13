# The Body Composition Engine

How BodyForger turns the resistances a scale measures into a body composition. This document
covers the **physics shared by every reading** — what is measured, and how six paths become
five segments. The exact published equations the engine applies on top of that physics live in
`FORGEFIT_MODEL.md`; constant names in `core-bia` match the names used below.

The engine this repository ships, **ForgeFit MIT**, is built entirely from published,
peer-reviewed equations and published anthropometric constants. Every coefficient it applies
has a citation in `FORGEFIT_MODEL.md`.

---

## 1. What is measured, and what is derived

A bioelectrical impedance scale measures exactly one thing: **electrical resistance in ohms**,
along anatomical paths, at one or more frequencies. Everything else — fat mass, muscle,
water — is computed from those resistances plus the athlete's physiology.

An eight-electrode dual-frequency scale yields **twelve** readings: six paths at low
frequency, the same six at high frequency. They are persisted verbatim and forever. Derived
quantities never are — that is what allows the whole history to be recomputed when an
equation improves.

A path is **not** a body zone. All six cross both the limbs and the trunk.

---

## 2. Isolating the five segments (Kirchhoff)

The body is modelled as five conductors meeting at a central node: four limbs, long and
narrow, and a trunk of wide cross-section. A scale can never measure one limb alone, since
current must enter at one point and leave at another. Six closed loops are measured for five
unknowns:

```
R_lfrf = Z_LF + Z_RF                  R_lhlf = Z_LH + Z_trunk + Z_LF
R_lhrh = Z_LH + Z_RH                  R_lhrf = Z_LH + Z_trunk + Z_RF
                                      R_rhlf = Z_RH + Z_trunk + Z_LF
                                      R_rhrf = Z_RH + Z_trunk + Z_RF
```

**Limbs** come out by cross-differencing, which cancels the trunk. Summing the paths through
the right arm and subtracting those through the left gives `2·(Z_RH − Z_LH)`; combined with
`R_lhrh = Z_RH + Z_LH`, each arm follows. Legs work the same way from `R_lfrf`.

**The trunk** comes from summing the four crossed paths, where it appears four times:

```
Z_trunk = (R_lhlf + R_lhrf + R_rhlf + R_rhrf − 2·(R_lfrf + R_lhrh)) / 4
```

**Whole-body impedance** reduces to a remarkably simple form, because the four crossed paths
cancel in the average of the limbs:

```
Z_body = (R_lfrf + R_lhrh) / 4
```

Only foot-to-foot and hand-to-hand decide it. The diagonals decide the left/right split and
nothing else — a useful invariant when checking an implementation.

### Two guard rails this yields

**Trunk resistance is a residual of large numbers.** At ±1 % contact error it swings by ±9 Ω
around a nominal ~20 Ω — close to 100 % relative uncertainty, against 2 % for `Z_body`. Treat
it as an indicator, never as a fine measurement.

**Limb spread is bounded.** Once `R_lfrf` and `R_lhrh` are fixed, cross-differencing can only
produce a difference between two matching limbs of at most
`|(R_rhlf + R_rhrf) − (R_lhlf + R_lhrf)| / 4`. Any segmental figure beyond that is wrong by
construction — a cheap admissibility test.

---

## 3. Fat-free mass

ForgeFit computes fat-free mass from **published equations only**, merged by their published
precision (inverse-variance weighting on each equation's standard error of estimate):

* **Sun / NHANES** (Chumlea et al.) — hand-to-foot, whole body, sex-specific;
* **Wu et al. 2011** — foot-to-foot, the legs, sex handled by a term.

The exact coefficients and the merge are set out in `FORGEFIT_MODEL.md` §1. When only the
foot-to-foot path exists (four electrodes), Wu alone is used; when only hand-to-foot exists,
Sun alone.

**Why two frequencies.** At 50 kHz the current does not cross cell membranes and reads
extracellular water alone; at 250 kHz it enters the cytoplasm and reads total water. The gap
between the two is what separates fluid retention from active muscle. Both equations are
calibrated at 50 kHz, so the 250 kHz reading is first converted to its 50 kHz-equivalent
(impedance is systematically lower at 250 kHz) before the equation is applied — see
`FORGEFIT_MODEL.md` §1.

Fat mass and body-fat % follow directly: `fat = mass − FFM`.

---

## 4. Splitting fat-free mass (Brozek)

Fat-free mass divides into water, protein and bone mineral using the **Western Brozek 4C**
constants:

```
water = 0.732 · FFM      protein = 0.211 · FFM      bone mineral = 0.057 · FFM
```

The three sum to 1.000 exactly. These are the published 4C fractions, chosen because the scale
is an instrument, not the reference, and BodyForger measures with constants suited to the
athlete it serves.

**Extracellular ratio** is read from the gap between frequencies, `0.380 + 0.05·(Z250/Z50 −
0.88)`, clamped to a plausible band. Without a second frequency it falls back to the clinical
norm of `0.380`. The underlying physics is standard; the affine calibration is ForgeFit's own
design choice.

---

## 5. Skeletal muscle, and its distribution

Total skeletal muscle comes from **Janssen et al. 2000** — the resistance-only, DXA-validated
form, from the whole-body (hand-to-foot) reading.

It is then spread over the five segments from **published anthropometric constants and each
limb's own resistance index** `L²/Z`. The upper/lower split is a population constant; the
per-limb impedance sets the left/right balance — the asymmetry a lifter actually wants to read.
Segmental fat is derived from each segment's non-muscular mass, and a visceral index and body
score close the report. The full method, with sources (de Leva/Winter, Kim et al. 2002), is in
`FORGEFIT_MODEL.md` §5.

Bioimpedance does not isolate the trunk reliably (§2), so `trunkKg` is always a remainder,
never a measurement.

---

## 6. What "accurate" means here

Bioimpedance is not a DEXA scan and is never exact. In absolute terms it is off by several
points of body fat; what it does well is **repeat itself**.

Two rules follow, and they govern every choice above:

* A formula that is slightly wrong but applied consistently to the whole history is worth more
  than a truer one applied to part of it. This is why derived values are never stored.
* Comparing against the scale's own displayed figures is a **gross-error detector, not an
  accuracy target**. Its tolerance is the kilogram. Reducing a sub-kilogram gap is never in
  itself a reason to change the model.

---

## 7. Sources

Every figure ForgeFit reports traces to a published source:

* **Fat-free mass** — Sun / NHANES (Chumlea et al.); Wu et al. 2011 (foot-to-foot).
* **Skeletal muscle** — Janssen et al. 2000.
* **Compartments** — Western Brozek 4C fractions.
* **Basal metabolism** — Katch-McArdle.
* **Segmental distribution** — de Leva 1996 / Winter (segment masses and lengths); Kim et al.
  2002 (appendicular muscle share).

Full equations and coefficients are in `FORGEFIT_MODEL.md`. The engine's test vectors are a
theoretical body whose segments are known in advance, so the Kirchhoff solver is checked
against a truth set that no captured reading can offer.
