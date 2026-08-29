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
Un enregistrement quotidien rattaché à l'athlète. Contient obligatoirement la masse corporelle en kilogrammes, la date civile (`YYYY-MM-DD`), le taux de masse grasse et la **Capacité de Mesure** dont il est issu. Il porte en outre, lorsque le matériel les fournit, les **Impédances Brutes**, le rythme cardiaque et l'horodatage précis émis par la balance elle-même.

Le taux de masse grasse est **toujours renseigné** : il provient soit de la balance, soit d'une saisie manuelle de l'athlète. Il ne se confond pas avec le **Rapport de Composition Corporelle**, qui n'existe que si des impédances brutes ont été mesurées.

### Impédances Brutes (Raw Impedances)
La seule grandeur réellement **mesurée** par une balance à bio-impédance : les résistances électriques du corps, exprimées en ohms, relevées le long de **trajets anatomiques** distincts et à une ou plusieurs **fréquences**.

Une balance 8 électrodes bi-fréquence en produit douze : six trajets (pied gauche ↔ pied droit, main gauche ↔ main droite, main gauche ↔ pied gauche, main gauche ↔ pied droit, main droite ↔ pied gauche, main droite ↔ pied droit) mesurés successivement à basse puis à haute fréquence. Un trajet n'est pas une zone du corps : les six trajets traversent tous à la fois les membres et le tronc.

Les impédances brutes sont **conservées telles quelles et à perpétuité**. C'est ce qui permet de recalculer rétroactivement tout l'historique lorsque les équations de composition corporelle évoluent, et d'agréger une période en analysant la médiane des résistances plutôt qu'une moyenne de résultats.

### Segmental Composition (Analyse par Segment)
La décomposition de la masse musculaire et grasse par région anatomique — tronc, bras droit, bras gauche, jambe droite, jambe gauche.

Cette analyse est **entièrement dérivée** : les impédances de chaque membre se déduisent des **Impédances Brutes** par résolution des lois de Kirchhoff. Elle n'est donc jamais stockée, mais recalculée à la demande. Confondre cette couche dérivée avec la mesure elle-même est l'erreur à ne pas commettre.

### Capacité de Mesure (Measurement Capability)
Le niveau de finesse dont provient un **Body Log**, enregistré avec lui car il détermine quelles grandeurs sont légitimement calculables :

* **Bi-fréquence 8 électrodes** — balance à poignée rétractable : composition corporelle complète et segmentaire.
* **Mono-fréquence** — impédance corps entier à une seule fréquence : composition globale uniquement.
* **Poids seul** — balance sans électrodes : aucune composition corporelle.
* **Manuelle** — masse et taux de gras déclarés par l'athlète.

Une grandeur qu'une capacité ne permet pas de mesurer est **absente**, jamais remplacée par une valeur par défaut : un chiffre inventé serait indiscernable d'une mesure réelle dans l'historique.

### BIA Profile (Profil Bio-impédance)
L'ensemble des constantes physiologiques de l'utilisateur (sexe biologique, date de naissance/âge, taille en cm) indispensables aux équations de bio-impédance électrique multi-fréquences.

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
Le composant d'adaptation matériel dédié à une famille de balances (ex: `HuaweiScale3Driver`, `StandardGattScaleDriver`), encapsulant le chiffrement, les étapes d'appairage et le décodage des trames télémétriques. Il expose la **Weigh-In Session** sous forme d'états successifs observables, afin que l'interface reste générique et ignorante du matériel.

### Weigh-In Session (Session de Pesée)
Le déroulé complet d'une pesée, de l'intention de l'athlète jusqu'à l'enregistrement du **Body Log**. Ce n'est pas une opération instantanée mais une séquence observable de plusieurs dizaines de secondes, que l'athlète suit et peut interrompre.

Elle se déroule en trois temps, dont le premier est physique et doit être énoncé à l'athlète :

1. **Réveil** — l'athlète tapote la balance ou monte brièvement dessus pour qu'elle se signale ; sans cela elle reste invisible.
2. **Négociation** — connexion, authentification et configuration du profil, **sans que l'athlète soit sur le plateau**.
3. **Mesure** — l'athlète est invité à monter pieds nus ; suivent la stabilisation du poids puis le relevé des impédances.

Une session appartient à l'appareil qui l'a lancée et s'y achève. La montre et le téléphone en sont également capables ; la balance n'en acceptant qu'une à la fois, la seconde échoue simplement.

### Association (Balance Associée)
Le lien durable entre l'athlète et une balance donnée : adresse physique de l'appareil, **HUID**, tare de calibration et modèle matériel.

L'Association est créée **une seule fois**, par l'appareil qu'importe lequel, puis partagée entre la montre et le téléphone. Tant qu'elle existe, toute pesée ultérieure l'utilise directement ; l'appairage ne se rejoue jamais.

### HUID (Huawei User ID)
Identifiant virtuel attribué à la balance lors de l'appairage initial pour segmenter et autoriser le profil utilisateur sur la mémoire flash interne du pèse-personne. **Un seul HUID par athlète**, quel que soit l'appareil qui a réalisé l'appairage : deux identifiants distincts occuperaient deux emplacements mémoire de la balance et scinderaient l'historique en deux personnes.

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
