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

**BodyForger** is a personal, open-source fitness and body tracking project currently under active development (**WIP**). It is designed to solve specific daily training frustrations by merging the best of two open-source projects into a native dual-app ecosystem (**Android APK + Wear OS Native**):

1. **[openGym](https://gitlab.com/DuarteSantos8/opengym)** — Comprehensive workout routines, 1,300+ exercises, and advanced set mechanics (drop-sets, rest-pause, 1RM).
2. **[SimpleBodyGraph](https://github.com/DevOpsBenjamin/SimpleBodyGraph)** — DEXA-calibrated BIA body composition modeling, connected BLE smart scales (Huawei Scale 3 / standard GATT), and body tape measurements.

### Why this project?
I'm a long-time **Hevy** user and still recommend it — it's a genuinely good app. But my own setup asks for two things it isn't built around: a watch that runs a full session **on its own**, away from the phone, and a **Google Health Connect** sync that carries everything I care about rather than a subset. Those are design priorities, not defects.

BodyForger is built to be **100% wrist-first**, fully autonomous on Wear OS (logging workouts with screen dimmed/off via Android Health Services), directly connected to smart scales via BLE, and deeply integrated with the Google Health ecosystem.

---

## 🎯 Who this is for — and who it isn't

BodyForger is a **personal, non-commercial project**, built first and foremost for its author's own use. It is open source because there is no reason to keep it closed — not because it is looking for users.

**This app is built to live inside Google Health.** That is the point of it, not a side integration — and it's the single need that started the project.

### You'll get the most out of it if

- You wear a **Wear OS watch** and train with it on your wrist, away from your phone.
- You use **Google Health Connect** as the hub of your health data.
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
