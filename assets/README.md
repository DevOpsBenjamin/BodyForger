# BodyForger — Brand assets and vector sources

This folder holds the official vector sources for **BodyForger**.

---

## 🔨 The forge hammer

The app icon is a **forge hammer** drawn as a single filled shape, carrying a radial
green-to-yellow gradient on an obsidian background. The green forms a core over the
hammer eye and the neon yellow radiates out towards the peen and the knob.

Two reasons for this mark:

* **"Forger" as in blacksmith, not as in forgery** — the hammer resolves the ambiguity
  of the name by anchoring the identity on the smith, the effort and the transformation.
* **It survives small sizes** — the previous logo (watch bezel plus olympic plate) is
  rich and holds up beautifully at 512 px, but collapses at 72 px: 95% of its pixels sit
  below a luminance of 64, so on a Wear OS launcher it turns into a dark smudge. A solid,
  high-contrast silhouette stays readable all the way down to mdpi.

---

## 📁 Files

| File | Role | Notes |
| :--- | :--- | :--- |
| **`ic_launcher_master.svg`** | Source of the app icon | Rounded square (radius 112/512). This is **the** reference; every Android icon derives from it. |
| **`playstore_icon_512.svg`** | Source of the Play Store icon | Same artwork, but **without rounding** — Google applies its own mask. |
| **`playstore_icon_512.png`** | Play Store deliverable | 512×512, 32-bit, fully opaque, ~39 KB. Generated, never hand-edited. |
| `logo.svg` | Previous HD illustration | Pixel Watch bezel and olympic plate. **No longer the source of the icons.** Kept for the splash screen, banners and promotional material. |

> [!IMPORTANT]
> The former rule — one compact SVG below 192 px, the rich one above — no longer applies
> to icons. It only ever existed to work around the old logo being unreadable when small.
> The hammer holds at every density from a single source, so there is no low-resolution
> variant left to maintain.

---

## ⚙️ Regenerating

```bash
python3 tools/build_icons.py
```

Writes 40 PNGs (4 variants × 5 densities × 2 modules) plus the Play Store icon, and
re-checks the geometry on the way. Never edit the PNGs by hand: they are entirely
derived.

The script is **macOS-only** — it rasterises with `qlmanage`, the only SVG engine
available without an external dependency. Since `qlmanage` is a thumbnailer and flattens
everything onto opaque white, each image is rendered twice, over white and over black,
and the alpha channel is rebuilt with `α = 1 − (white − black)`. The inversion is exact,
antialiased edges included.

### What the script produces

| Resource | Canvas | Role |
| :--- | :--- | :--- |
| `ic_launcher.png` | 48 dp | Legacy icon (pre-Android 8), rounded square |
| `ic_launcher_round.png` | 48 dp | Round launchers and Wear OS |
| `ic_launcher_foreground.png` | 108 dp | Foreground layer of the adaptive icon |
| `ic_launcher_monochrome.png` | 108 dp | Android 13+ themed icons |

The adaptive icon's background is not an image but a colour
(`values/ic_launcher_background.xml`, `#050608`).

### Framing the adaptive icon

Android draws on 108 dp but shows only 72, and guarantees only the central 66. So that
launchers display exactly the artwork approved at 512, the 512 canvas is centred inside a
768-unit box — since 512/768 = 72/108. The hammer therefore keeps its original
proportions, and its 57 dp diagonal stays inside the 66 dp safe circle. The script
verifies both margins on every run and stops rather than shipping a clipped icon.
