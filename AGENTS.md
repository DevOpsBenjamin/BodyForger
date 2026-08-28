# Directives & Conventions de Développement — BodyForger

Ce document régit les règles de développement, les protocoles d'authentification et les directives de sécurité pour tout agent intervenant sur **BodyForger**.

---

## 🛑 1. Règles Fondamentales de Comportement (Agent Reliability)

1. **Arrêt immédiat au premier imprévu** : Si une commande, un build ou une action échoue de manière inattendue, **NE PAS** enchaîner des contournements aveugles en rafale. S'arrêter, analyser la cause racine et expliquer clairement la situation.
2. **Transparence et explications concises** : Toujours formuler les décisions d'architecture et les modifications techniques de façon claire et argumentée.
3. **Respect absolu du Master Plan (`PLAN.md`)** : Ne pas introduire de bibliothèques tierces superflues ou modifier le modèle de domaine sans justification technique documentée.
4. **Interdiction d'exécuter `adb screencap` ou d'installer/lancer l'application** : L'utilisateur lance lui-même depuis Android Studio (`Run ▶️`) et dépose manuellement ses captures d'écran dans `appscreen/`. L'agent compile et exécute les tests unitaires via Gradle uniquement.
5. **Pas d'ouverture de PR avant validation utilisateur** : Ne **JAMAIS** ouvrir de Pull Request (`gh pr create`) avant que l'utilisateur n'ait testé dans Android Studio, fait sa revue et donné explicitement son feu vert. L'agent crée sa branche, pousse les commits (`git push origin <branch>`), et ajuste le code selon les retours de l'utilisateur.

---

## 🛡️ 2. Workflow Git & Qualité (CI / CD)

* **Politique de branches** :
  * La branche `main` est protégée : aucun push direct, aucune suppression, aucun force-push.
  * Tout développement passe par une branche de fonctionnalité (`feat/...`, `fix/...`, `docs/...`).
  * Les Pull Requests requièrent le passage au vert obligatoire de la suite CI (`./gradlew test` et `./gradlew compileDebugKotlin`).
  * **Squash & Merge uniquement** avec suppression automatique de la branche après fusion.

---

## 🧩 3. Standards d'Architecture UI, Modularité & Internationalisation (i18n)

1. **Modularité & Découpage au fil de l'eau** :
   * Tout composant réutilisable ou dépassant ~40-50 lignes doit être extrait dans `ui/components/` (ex: `CompactNumberInput`, `RoutineSetRow`, `RestTimePickerDialog`).
   * Les fichiers d'écrans (`ui/screens/`) doivent rester concis et légers (< 250 lignes).
2. **Hygiène des Saisies Numériques (Zéro Texte Tronqué)** :
   * **NE JAMAIS** forcer une hauteur contrainte (`height(44.dp)`) sur un `OutlinedTextField` Material 3 pour de petits chiffres (ce qui tronque et coupe le texte verticalement).
   * Toujours utiliser des composants dédiés basés sur `BasicTextField` avec centrage vertical absolu et padding zéro.
3. **Internationalisation (i18n)** :
   * Centraliser les libellés dans `res/values/strings.xml` (Anglais par défaut) et `res/values-fr/strings.xml` (Français).
   * Utiliser `stringResource(R.string.xxx)` dans les composants Compose.

---

## 🧪 4. Stratégie de Test & Données

* **Architecture Local-First** : Tous les modèles de données (Kotlin Room DB, IndexedDB) doivent être testables hors-ligne avec des jeux de données d'entraînement et de pesée BIA reproductibles.
* **Intégrité Mathématique BIA** : Les tests du module `core-bia` doivent valider les algorithmes DEXA face aux équations de référence (cf. `PLAN.md`).
