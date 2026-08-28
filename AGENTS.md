# Directives & Conventions de Développement — BodyForger

Ce document régit les règles de développement, les protocoles d'authentification et les directives de sécurité pour tout agent intervenant sur **BodyForger**.

---

## 🛑 1. Règles Fondamentales de Comportement (Agent Reliability)

1. **Arrêt immédiat au premier imprévu** : Si une commande, un build ou une action échoue de manière inattendue, **NE PAS** enchaîner des contournements aveugles en rafale. S'arrêter, analyser la cause racine et expliquer clairement la situation.
2. **Transparence et explications concises** : Toujours formuler les décisions d'architecture et les modifications techniques de façon claire et argumentée.
3. **Respect absolu du Master Plan (`PLAN.md`)** : Ne pas introduire de bibliothèques tierces superflues ou modifier le modèle de domaine sans justification technique documentée.

---

## 🔑 2. Gestion Multi-Comptes GitHub CLI (`gh`)

* **Préfixe obligatoire pour les commandes GH** :
  Toute commande `gh` interagissant avec l'API GitHub sur ce dépôt doit impérativement être préfixée avec le token du compte propriétaire `DevOpsBenjamin` :
  ```bash
  GH_TOKEN=$(gh auth token --user DevOpsBenjamin) gh pr list
  GH_TOKEN=$(gh auth token --user DevOpsBenjamin) gh pr create --title "..." --body "..."
  ```

---

## 🛡️ 3. Workflow Git & Qualité (CI / CD)

* **Politique de branches** :
  * La branche `main` est protégée : aucun push direct, aucune suppression, aucun force-push.
  * Tout développement passe par une branche de fonctionnalité (`feat/...`, `fix/...`, `docs/...`).
  * Les Pull Requests requièrent le passage au vert obligatoire de la suite CI (`Build Web Landing` et futurs checks Android).
  * **Squash & Merge uniquement** avec suppression automatique de la branche après fusion.

---

## 🧪 4. Stratégie de Test & Données

* **Architecture Local-First** : Tous les modèles de données (Kotlin Room DB, IndexedDB) doivent être testables hors-ligne avec des jeux de données d'entraînement et de pesée BIA reproductibles.
* **Intégrité Mathématique BIA** : Les tests du module `core-bia` doivent valider les algorithmes DEXA face aux équations de référence (cf. `PLAN.md`).
