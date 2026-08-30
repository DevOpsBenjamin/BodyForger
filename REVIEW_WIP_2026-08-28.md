# 🔍 Revue du WIP — BodyForger

> **Date :** 28 août 2026 · projet démarré le jour même
> **Branche :** `feat/firebase-mobile-integration` (`cdbc5a4`)
> **Méthode du projet :** plan d'abord (PLAN / CONTEXT / ADR), puis design system, puis routines & exercices, puis incrémental piloté par les issues Wayfinder.

---

## 🎯 Cadrage

Cette revue tient compte du stade réel : **le squelette modulaire est volontairement pré-créé, les modules vides sont des emplacements réservés pour M2 → M7.**

Ne sont donc **pas** traités comme des défauts :

- `core-ble` (8 l.), `core-healthconnect` (10 l.), `core-bia` (45 l.) → placeholders M2.
- `app-wear` (287 l.) → maquette, le moteur c'est M3.
- Firebase déclaré sans code → PR #18 en cours, plomberie avant usage.
- Milestones M2–M5, M7 vides → futures.
- Tests placeholders à 11 lignes → normaux à ce stade.
- Room défini mais pas encore instancié → l'UI se prototype d'abord, c'est un choix défendable.

Ce document se concentre sur **ce qui coûte plus cher si on ne le traite pas maintenant**.

---

## ✅ Ce qui est solide

**La doc.** `CONTEXT.md` comme glossaire canonique, `AGENTS.md` avec des règles opérationnelles concrètes, l'ADR 001 qui tranche les vrais arbitrages (idempotence UUID, quotas Spark, `schemaVersion`). Poser ça avant le code, c'est exactement ce qui rend l'incrémental tenable — chaque issue arrive dans un cadre déjà décidé.

**L'ossature.** 8 modules découplés, version catalog, JDK 21, CI (`test` + `assembleDebug` sur les deux APK), `main` protégée, squash-merge. Rien à reprendre.

**Le découpage UI.** 39 composants pour 10 écrans, règle des 250 lignes globalement tenue. Pour 4 h de travail, la discipline est déjà là.

**La granularité des PRs.** 8 PRs, une par thème, squash-mergées. L'historique est lisible.

---

## 🔧 À traiter maintenant (le coût augmente avec le temps)

### 1. `applicationId` divergents — 5 minutes aujourd'hui, pénible dans un mois

| App | `applicationId` actuel |
| :--- | :--- |
| Mobile | `app.bodyforger` |
| Wear | `app.bodyforger.wear` |

Google demande le **même `applicationId`** pour l'association automatique mobile ↔ Wear sur le Play Store. Changer ça après Firebase configuré, keystore signé et Data Layer câblé, c'est une plaie.

→ **À trancher avant M3/M4.**

### 2. Bug réel : `JsonBackupManager` perd les pesées

`bodyLogs` existe dans `BodyForgerBackupPayload`, mais n'est **ni sérialisé ni désérialisé** — `deserialize()` retourne `bodyLogs = emptyList()` en dur.

Le code est neuf et déjà couvert par un test qui ne détecte pas le trou. C'est le bon moment pour le fermer, avant que le format de backup ne parte en prod.

→ Accessoirement : 400 lignes de `org.json` manuel. `kotlinx.serialization` donnerait le round-trip gratuitement et supprimerait cette classe de bug.

### 3. Hygiène git — irréversible une fois dans l'historique

- `.idea/workspace.xml` tracké.
- ~20 PNG dans `appscreen/`, dont des fichiers `... (101).` sans extension.

Les binaires restent dans l'historique pour toujours. À nettoyer tant que le repo a 8 commits.

### 4. i18n — dette qui grossit à chaque écran

**12 fichiers sur 50 utilisent `stringResource`** (24 %). Les 52 strings FR/EN existent, la règle AGENTS.md §3.3 est posée.

Chaque écran ajouté sans `stringResource` est une repasse en plus. Autant l'appliquer au fil de l'eau plutôt qu'en session de rattrapage.

---

## 🧭 Suggestions d'ordonnancement (pas des reproches)

### Le point de bascule Room

Aujourd'hui tout l'état vit dans `MainActivity.kt:52-67` :

```kotlin
val routines = remember {
    mutableStateListOf<Routine>().apply { addAll(DebugSampleRoutines.list) }
}
```

C'est légitime pour prototyper l'UX des routines. Le seul point à garder en tête : **le coût de la bascule vers Room + ViewModel croît avec le nombre d'écrans branchés sur `remember`.**

Il y a un moment optimal pour la faire — probablement juste avant l'issue #14 (séance active), puisque la persistance atomique par série en est le cœur et qu'elle a besoin de `WorkoutSetEntity` (qui n'existe pas encore) de toute façon.

→ Suggestion : **#14 = l'occasion de câbler Room, pas un écran de plus sur `remember`.**

### Dé-risquer Wear plus tôt

Health Services + Ambient AOD + foreground service, c'est **la seule vraie inconnue technique du projet** et c'est aussi le différenciateur vs Hevy.

M3 est bien placé dans la roadmap, mais un spike court (est-ce que `ExerciseClient` tient en ambient sans se faire tuer ?) rapporterait beaucoup s'il arrivait avant d'empiler beaucoup d'UI mobile. À voir selon ton appétence au risque.

### Cohérence vitrine

Le README annonce « 1 300+ exercices », le catalogue en contient **124** (le commentaire du code dit honnêtement « ~100 »). Sans urgence — soit l'import openGym rattrape, soit une note « catalogue de base, import openGym en cours » suffit.

---

## 📌 En résumé

**Pour 4 h, le rapport structure / code est le bon.** Le plan est posé, l'ossature est propre, le premier vertical slice (design system → routines → exercices) est cohérent et livré. La méthode incrémentale pilotée par issues fonctionne visiblement.

Les seuls points qui méritent une action à court terme sont ceux dont le coût est **asymétrique dans le temps** :

| Priorité | Action | Pourquoi maintenant |
| :--- | :--- | :--- |
| 🔴 | Unifier `applicationId` | Très cher à changer après M3/M4 |
| 🔴 | Nettoyer `.idea/` + `appscreen/` du suivi git | Irréversible dans l'historique |
| 🟠 | Corriger `bodyLogs` dans `JsonBackupManager` | Avant de figer le format de backup |
| 🟠 | Appliquer `stringResource` au fil de l'eau | Dette linéaire au nombre d'écrans |
| 🟡 | Câbler Room à l'occasion de #14 | Point de bascule naturel |
| 🟡 | Spike Wear / Health Services | Seule vraie inconnue technique |

Le reste suivra les milestones.
