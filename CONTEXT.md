# 📖 Glossaire du Domaine (Domain Model) — BodyForger

Ce document constitue la référence canonique des termes et concepts métier utilisés dans l'ensemble de l'écosystème BodyForger (Mobile, Wear OS, Moteur BIA, openGym Core, Health Connect).

---

## 1. Contexte : Musculation & Entraînement (Gym & Progression)

### Exercise (Exercice)
Une unité d'entraînement physique référencée dans le catalogue (1 300+ entrées). Défini par un identifiant unique, un nom canonique, une partie du corps (`bodyPart`), un équipement requis (`equipment`), un muscle cible principal (`target`), et des muscles secondaires (`secondaryMuscles`).

### Routine (Programme / Modèle de Séance)
Un modèle d'entraînement réutilisable ou planifié pour un jour donné (ex: Push, Pull, Legs, Upper, Lower). Il contient une liste ordonnée d'exercices avec des prescriptions cibles (nombre de séries, fourchette de répétitions, temps de repos par défaut, intensificateurs prévus).

### Workout Session (Séance Active / Historique de Séance)
L'instance réelle d'un entraînement exécuté par l'utilisateur à un horodatage précis. Elle enregistre l'heure de début, l'heure de fin, la durée effective, les calories actives brûlées, la série temporelle du rythme cardiaque mesurée en continu, et la liste ordonnée des exercices avec leurs séries réellement complétées.

### Set (Série)
L'unité atomique d'effort au sein d'un exercice. Une série possède une double classification orthogonale :
1. **Phase** :
   * `Warmup` (Série d'échauffement ou d'activation, non comptabilisée dans le volume de travail effectif).
   * `Work` (Série de travail effective).
2. **Type (Intensificateur)** :
   * `Straight` (Série classique : charge et répétitions fixes).
   * `DropSet` (Série principale suivie immédiatement d'une ou plusieurs décharges de poids sans temps de repos).
   * `RestPause` / `MyoReps` (Une série d'activation suivie de courtes salves/clusters de répétitions entrecoupées de micro-repos de 10 à 20 secondes).

### Drop (Décharge)
Sous-ensemble de travail au sein d'un `DropSet`. Représente une charge réduite (ex: -20%) exécutée immédiatement après la série précédente sans repos.

### Cluster (Salve de répétitions)
Sous-ensemble d'un `RestPause`. Décomposition du total de répétitions en petites salves (ex: 12 reps décomposées en [6, 3, 2, 1]) séparées par un micro-repos.

### 1RM (One Repetition Maximum)
La charge maximale théorique qu'un athlète peut soulever sur une seule répétition, calculée à partir d'une série effective via les formules validées (Epley ou Brzycki).

### Muscle Heatmap & Volume
L'agrégation hebdomadaire du nombre de séries de travail effectives (`Work`) appliquées à chaque groupe musculaire anatomique, visualisée sous forme de carte thermique corporelle.

---

## 2. Contexte : Biométrie & Composition Corporelle (BIA & Scales)

### Body Log (Pesée / Journal Corporel)
Un enregistrement quotidien rattaché à l'utilisateur. Contient obligatoirement la masse corporelle en kilogrammes (`mass`), la date civile (`YYYY-MM-DD`), l'estimation du taux de masse grasse (`body_fat`), et optionnellement les données physiologiques complètes (rythme cardiaque au repos, impédances segmentaires brutes, horodatage précis).

### BIA Profile (Profil Bio-impédance)
L'ensemble des constantes physiologiques de l'utilisateur (sexe biologique, date de naissance/âge, taille en cm) indispensables aux équations de bio-impédance électrique multi-fréquences.

### Segmental Composition (Analyse 5 Zones)
La décomposition de la masse musculaire et grasse en 5 compartiments anatomiques distincts :
* Tronc (Torse/Abdomen)
* Bras Droit & Bras Gauche
* Jambe Droite & Jambe Gauche

### Hydration Compartments (Compartiments Hydriques)
* **TBW** (Total Body Water) : Quantité totale d'eau corporelle en litres.
* **ICW** (Intracellular Water) : Eau contenue à l'intérieur des cellules musculaires.
* **ECW** (Extracellular Water) : Eau interstitielle et plasmatique (rétention d'eau).
* **Ratio ECW/TBW** : Indicateur clinique de balance hydrique (norme optimale ~0.38 - 0.40).

### Palier (Objectif par Étape)
Un seuil cible intermédiaire (masse et pourcentage de masse grasse). Il est validé automatiquement lorsque la tendance hebdomadaire stable (médiane glissante sur 7 jours) franchit le seuil requis.

### Measurement (Mensuration Ruban)
Une entrée de circonférence anatomique en centimètres (tour de poitrine, taille, bras, cuisses, mollets, cou).

---

## 3. Contexte : Matériel & Bluetooth BLE (Hardware & GATT)

### ScaleManager
L'orchestrateur central responsable du scan Bluetooth Low Energy, de la sélection du pilote matériel adapté, du cycle de vie de la connexion et de la transmission des trames à l'application (sur Smartphone ou Wear OS).

### ScaleDriver
Le composant d'adaptation matériel dédié à une famille de balances (ex: `HuaweiScale3Driver`, `StandardGattScaleDriver`), encapsulant le chiffrement, les étapes d'appairage et le décodage des trames télémétriques.

### HUID (Huawei User ID)
Identifiant virtuel attribué à la balance lors de l'appairage initial pour segmenter et autoriser le profil utilisateur sur la mémoire flash interne du pèse-personne.

---

## 4. Contexte : Montre & Exécution Wrist-First (Wear OS)

### Autonomous Workout Runner
Le moteur d'exécution autonome sur la montre Wear OS. Il fonctionne en premier plan (`Foreground Service`) sans dépendre d'une connexion active avec le smartphone.

### Health Services Session (`ExerciseClient`)
La session matérielle Android officielle déléguant la lecture continue du capteur cardiaque (BPM) et le calcul des calories au coprocesseur basse consommation de la montre.

### Ambient State (AOD Mode)
L'état d'affichage économe Always-On Display (rafraîchissement 1 Hz, fond noir absolu, contraste élevé) activé quand le poignet est baissé, maintenant la séance et les chronomètres actifs sans interruption par l'OS.

### Rest Timer & Haptic Sequence
Le chronomètre de récupération entre deux séries. Il déclenche une séquence haptique programmée (vibrations d'alerte à -3s, vibration longue de fin à 0s) perceptible à l'aveugle par l'athlète.

---

## 5. Contexte : Écosystème & Intelligence Artificielle (Health & AI)

### Health Connect Telemetry
L'exportation vers Google Health Connect :
* `ExerciseSessionRecord` (Séances de musculation avec segments d'exercices).
* `HeartRateRecord` (Séries temporelles continues de BPM).
* `PlannedExerciseSessionRecord` (Plans et séances programmées).
* `WeightRecord` & `BodyFatRecord` (Mesures de pesée et de graisse).

### MCP Server (Model Context Protocol)
Le serveur d'interface exposant les données de BodyForger aux assistants IA (Gemini, Claude) pour générer des programmes d'entraînement sur-mesure basés sur l'évolution réelle de la composition corporelle de l'utilisateur.
