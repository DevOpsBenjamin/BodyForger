# ⚡ BodyForger

<div align="center">

### **Wrist-First Gym & Body Composition Suite (WIP)**
*Native Android & Wear OS Application*

[![Status: WIP](https://img.shields.io/badge/Status-Work%20In%20Progress-yellow.svg)](#)
[![Android](https://img.shields.io/badge/Platform-Native%20Android%20%7C%20Wear%20OS-3DDC84.svg?logo=android&logoColor=white)](#)
[![Health Connect](https://img.shields.io/badge/Google-Health%20Connect-34A853.svg?logo=google&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 💡 About BodyForger

**BodyForger** is a personal, open-source fitness and body tracking project under active development (**WIP**). It's a native dual-app ecosystem (**Android APK + Wear OS Native**) that brings together two things I previously tracked in separate places:

1. **Workouts** — routines, a 1,300+ exercise library and advanced set mechanics (drop-sets, rest-pause, 1RM), building on **[openGym](https://gitlab.com/DuarteSantos8/opengym)**.
2. **Body composition** — DEXA-calibrated BIA modelling, BLE smart scales and tape measurements, carried over from **[SimpleBodyGraph](https://github.com/DevOpsBenjamin/SimpleBodyGraph)**, a small earlier app of mine that only did scale tracking. For a mature, broadly supported take on that half, see **[openScale](https://github.com/oliexdev/openScale)**.

### Why this project?
I'm a long-time **Hevy** user and still recommend it — it's a good app. Two things about my own setup pushed me to build something else.

The first is that at the end of a session I have to take my phone out for the sync to happen. I'd rather **the watch write to Health Connect itself**, and walk away.

The second is that the Health Connect sync doesn't feel quite dialled in for what I want out of it. I'm hoping to do better — though that's a hope, not a claim. This is a work in progress and it hasn't proven anything yet.

### How it relates to Google Health
BodyForger **doesn't live inside Google Health**. It's a standalone, local-first app with its own database, its own history and its own statistics — it keeps working with Health Connect switched off entirely.

What it does is write *outwards*, deliberately: exercise types are mapped onto Google's own SDK types so sessions land as proper, well-formed records rather than opaque blobs — which is also what makes them worth anything to an assistant like Gemini reading them back. Data flows out; nothing depends on it flowing in.

The rest is **wrist-first**: full session autonomy on Wear OS with the screen dimmed or off (Android Health Services), and smart scales over BLE.

---

## 🎯 Who this is for — and who it isn't

BodyForger is a **personal, non-commercial project**, built first and foremost for its author's own use. It is open source because there is no reason to keep it closed — not because it is looking for users.

It's tuned for a watch-first routine with **Health Connect as the destination** for what it records — not as a dependency, and not as a place it lives.

### You'll get the most out of it if

- You wear a **Wear OS watch** and train with it on your wrist, away from your phone.
- You want your health data to land in **Google Health Connect**, cleanly typed.
- You own a **BLE body composition scale** and care about raw impedance, not just a body fat number.

### It's probably not the right fit if

- You train with your **phone in hand** and don't own a Wear OS watch.
- You don't use Health Connect, or you're on iOS.
- You're **starting out** and want a polished, supported app with a large exercise community.

Nothing is blocked — the app won't stop you. But it's tuned for one specific need, and **BodyForger doesn't aim to replace the apps below**. They are more polished, better supported and built for far more people than this one is. If your setup looks different from mine, start there:

| | |
| :--- | :--- |
| **[Hevy](https://www.hevyapp.com/)** | Polished workout tracking, large community, excellent onboarding. |
| **[openGym](https://gitlab.com/DuarteSantos8/opengym)** | Open-source routine builder with a 1,300+ exercise library. |
| **[openScale](https://github.com/oliexdev/openScale)** | Open-source body metrics tracking with ~60 supported BLE scales. |

That's a sincere recommendation. This project simply scratches a very specific itch, and it seems more useful to say so plainly than to pretend it suits everyone.

---

## 🗺️ Roadmap & Architecture

Detailed technical architecture, data models, Wear OS lifecycles, and implementation phases are documented in:

👉 **[Read the Master Plan (PLAN.md)](PLAN.md)**

---

## 🌐 Web & Domain

Landing page & documentation: **[bodyforger.app](https://bodyforger.app)**
