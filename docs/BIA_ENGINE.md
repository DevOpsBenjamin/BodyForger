# The Body Composition Engine

How BodyForger turns the resistances a scale measures into a body composition, and why each
constant has the value it has. Code refers here rather than repeating it; constant names in
`core-bia` match the names used below so the two can be read side by side.

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
construction — a cheap admissibility test against an external source.

---

## 3. Fat-free mass

A dual-frequency regression over nine terms, one row per biological sex:

```
FFM = c1·(H²/Z50) + c2·(H²/Z250) + c3·Z50 + c4·Z250
    + c5·W + c6·H + c7·A² + c8·A + c9
```

with `H` height in cm, `W` mass in kg, `A` age in years, and `Z50` / `Z250` the whole-body
impedances of §2.

| Coefficient | Term | Male | Female |
| :--- | :--- | ---: | ---: |
| `c1` | `H²/Z50` | +0.12631 | +0.07182 |
| `c2` | `H²/Z250` | +0.16098 | +0.07944 |
| `c3` | `Z50` | −0.01195 | −0.01169 |
| `c4` | `Z250` | −0.02027 | −0.01661 |
| `c5` | mass | +0.14923 | +0.11944 |
| `c6` | height | +0.25154 | +0.23935 |
| `c7` | age² | −0.000070 | +0.000430 |
| `c8` | age | −0.03560 | −0.08840 |
| `c9` | bias | −20.79390 | −14.71130 |

**Why two frequencies.** At 50 kHz the current does not cross cell membranes and reads
extracellular water alone; at 250 kHz it enters the cytoplasm and reads total water. The gap
between the two is what separates fluid retention from active muscle.

**The single-frequency case needs no second table.** When the high-frequency block is absent,
the low frequency stands in for both and the regression collapses to its one-impedance form,
the paired coefficients simply adding: `0.12631 + 0.16098 = 0.28729`. A test asserts this
rather than a second table assuming it.

**Four electrodes.** When only the foot-to-foot path exists, the whole model above does not
apply and a single-frequency equation is used instead:

```
FFM_male   = 0.406·(H²/R_feet) + 0.360·W + 0.100·H − 0.080·A − 9.10
FFM_female = 0.370·(H²/R_feet) + 0.300·W + 0.110·H − 0.070·A − 8.20
```

⚠️ These five constants are the only ones in the engine with no independent corroboration.
They produce plausible figures; they have not been checked against a reference.

---

## 4. Splitting fat-free mass (Brozek)

Fat-free mass divides into water, protein and bone mineral using the **Western Brozek 4C**
constants, not the scale manufacturer's:

```
water = 0.732 · FFM      protein = 0.211 · FFM      bone mineral = 0.057 · FFM
```

The three sum to 1.000 exactly. The manufacturer uses 0.740 / 0.205 / 0.055, which also sums
to 1.000 — closure does not decide between them. The choice is a positioning one: the scale is
an instrument, not the reference, and BodyForger measures with constants suited to the athlete
it serves. See *Lean Mass Compartments* in `CONTEXT.md`.

**Extracellular ratio** is read from the gap between frequencies, `0.380 + 0.05·(Z250/Z50 −
0.88)`, clamped to a plausible band. ⚠️ This affine calibration is inherited from the
reference implementation and has not been independently validated; the underlying physics is
standard. Without a second frequency it falls back to the clinical norm of `0.380`.

---

## 5. Skeletal muscle, and its distribution

Total skeletal muscle derives from fat-free mass: `SMM = 0.605·FFM − 1.833`.

⚠️ Reproducing the reference library's own output would need `0.562` rather than `0.605` —
a second-decimal discrepancy, not a rounding artefact. Left as is for now: the absolute value
matters far less than its consistency over time (§6).

Muscle then spreads over the five segments by **relative conductance**. Muscle conducts better
than fat, so the less resistant segment carries the larger share. Weighting a limb by the
*other* limb's resistance is exactly that conductance — `Z_LH / (Z_RH + Z_LH)` and
`(1/Z_RH) / (1/Z_RH + 1/Z_LH)` are the same expression.

The same logic applies one level up, between the upper and lower body, corrected by a
geometric factor: at equal muscle mass an arm is shorter than a leg and does not oppose the
same resistance. That factor is calibrated so a reference morphology recovers the usual
population fractions of ~17 % arms and ~48 % legs.

**One constant remains a population figure**: the share of total muscle held in the four
limbs, ~65 %, the rest being the trunk. Bioimpedance does not isolate the trunk reliably
(§2), so `trunkKg` is a remainder, never a measurement.

---

## 6. What "accurate" means here

Bioimpedance is not a DEXA scan and is never exact. In absolute terms it is off by several
points of body fat; what it does well is **repeat itself**.

Two rules follow, and they govern every choice above:

* A formula that is slightly wrong but applied consistently to the whole history is worth more
  than a truer one applied to part of it. This is why derived values are never stored.
* Comparing against the manufacturer's own figures is a **gross-error detector, not an
  accuracy target**. Its tolerance is the kilogram. Reducing a sub-kilogram gap is never in
  itself a reason to change the model.

---

## 7. Sources, and what to distrust

The reverse-engineering corpus in `../BLE/` is uneven, and two of its documents actively
mislead:

* **`HUAWEI_SCALE_3_BIA_DUAL_FREQ_PROOF.md`** explains a lean mass figure as a sum of two
  terms when that figure is simply `mass × (1 − manufacturer fat rate)`. Its segmental term
  rests on a `Z_limb` defined nowhere in any source. Only its nine global coefficients survive.
* **`HUAWEI_SCALE_3_BIA_GROUND_TRUTH.md`** §2–§3 lists its six resistances in an order that is
  not the wire order, which makes every derived `Z` value it publishes unusable.

The decoder's own test fixtures are the reliable reference: they are synthetic frames built to
the real structure, and the engine's test vectors are a theoretical body whose segments are
known in advance — so the solver is checked against a truth set beforehand, which no captured
vector can offer.
