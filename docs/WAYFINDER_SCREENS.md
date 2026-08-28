# 📱 BodyForger — Wayfinder des Écrans (UI Navigation & Interaction Map)

Ce document cartographie l'arbre de navigation complet, les machines à états d'interface et les contrats d'interaction pour l'application **Mobile** (Smartphone) et **Wear OS** (Montre).

---

## 🗺️ 1. Arbre Topologique de Navigation Mobile

```mermaid
flowchart TD
    AppStart([Ouverture Application]) --> MainScaffold[Scaffold 4 Onglets]
    
    subgraph BottomNav [Barre de Navigation Inférieure]
        TabHome[🏠 Accueil]
        TabWorkout[🏋️ Séance & Routines]
        TabBIA[🧬 Biométrie & BIA]
        TabCatalog[📚 Catalogue Exos]
    end
    
    MainScaffold --> BottomNav
    
    %% --- ONGLET 1: ACCUEIL ---
    TabHome --> HomeView[Dashboard Résumé]
    HomeView -->|Clic 'Démarrer Séance'| ActiveWorkoutView[Écran Séance Active]
    HomeView -->|Clic Tuile BIA| TabBIA
    HomeView -->|Clic 'Voir Historique'| WorkoutHistoryView[Historique des Séances]
    
    %% --- ONGLET 2: WORKOUT ---
    TabWorkout --> WorkoutHub[Hub d'Entraînement]
    WorkoutHub -->|Sélectionner Modèle| ActiveWorkoutView
    WorkoutHub -->|Créer Nouvelle Routine| RoutineEditorView[Éditeur de Programme]
    WorkoutHub -->|Voir Historique| WorkoutHistoryView
    
    ActiveWorkoutView -->|Ajouter Exercice| ExercisePickerModal[Modale Sélection Exercice]
    ActiveWorkoutView -->|Série Terminée| RestTimerModal[Barre/Overlay Rest Timer]
    ActiveWorkoutView -->|Terminer Séance| WorkoutSummaryView[Résumé de Fin de Séance]
    WorkoutSummaryView -->|Enregistrer & Exporter| HomeView
    
    %% --- ONGLET 3: BIOMÉTRIE ---
    TabBIA --> BIAHub[Dashboard Composition Corporelle]
    BIAHub -->|Bouton 'Peser'| BLEScalePairing[Overlay Scan & Connexion Scale 3]
    BLEScalePairing -->|Pesée Terminée| BIAReportDetail[Rapport DEXA 5 Zones & Eau]
    BIAHub -->|Gestion Objectif| BodyGoalEditor[Éditeur de Paliers & Tendance 7j]
    
    %% --- ONGLET 4: CATALOGUE ---
    TabCatalog --> CatalogListView[Liste 1 300+ Exercices]
    CatalogListView -->|Filtrer / Rechercher| CatalogListView
    CatalogListView -->|Clic Exercice| ExerciseDetailView[Fiche Détail Exercice]
    ExerciseDetailView -->|Calculer| OneRMCalculator[Calculateur 1RM Epley/Brzycki]
```

---

## ⌚ 2. Arbre Topologique de Navigation Wear OS (Wrist-First)

```mermaid
flowchart TD
    WearLaunch([Lancement Montre]) --> WearHome[Écran d'Accueil / Sélection]
    
    WearHome -->|Démarrer Séance Libre| WearLiveWorkout[Séance Active - Live Workout]
    WearHome -->|Sélectionner Routine Sync| WearLiveWorkout
    
    subgraph WorkoutLifecycle [Cycle de Vie Séance au Poignet]
        WearLiveWorkout -->|Validation Série| WearRestTimer[Chronomètre de Repos Circulaire]
        WearRestTimer -->|Fin du temps ou 'Passer'| WearLiveWorkout
        
        WearLiveWorkout -->|Molette / Boutons +/-| WearSetAdjust[Ajustement Charge & Répétitions]
        WearLiveWorkout -->|Swipe Gauche| WearExerciseList[Liste Ordonnée des Exercices]
        WearLiveWorkout -->|Baisse du Poignet| WearAmbientMode[Mode Ambient AOD 1Hz]
        WearAmbientMode -->|Levée du Poignet| WearLiveWorkout
    end
    
    WearLiveWorkout -->|Appui Long 'Terminer'| WearSummary[Résumé & Cardio de Séance]
    WearSummary -->|Envoi Téléphone via Data Layer| WearHome
```

---

## 📋 3. Spécification Détaillée des Écrans & Contrats d'Interaction

### 📱 Écrans Mobile (Android Jetpack Compose)

| Écran | Rôle Métier | Données en Entrée | Actions / Événements Déclenchés |
| :--- | :--- | :--- | :--- |
| **`HomeScreen`** | Vue d'ensemble quotidienne, motivation et accès rapide. | `UserStats`, `NextScheduledWorkout`, `LastBodyLog`. | `onStartWorkout()`, `onOpenBIA()`, `onViewHistory()`. |
| **`WorkoutScreen` (Active)** | Pilotage de la séance en direct avec chronomètre et cardio live. | `ActiveWorkoutSession`, `LiveHeartRate`. | `onLogSet(weight, reps, type)`, `onAddExercise()`, `onFinishWorkout()`. |
| **`RoutineEditorScreen`** | Création et édition de modèles de séances (Split, PPL, Upper/Lower). | `Routine?` (si édition). | `onAddExercise()`, `onReorderExercises()`, `onSaveRoutine()`. |
| **`ExerciseDetailScreen`** | Guide d'exécution, muscles cibles primaires/secondaires, historique 1RM. | `exerciseId: String`. | `onAddToCurrentWorkout()`, `onCalculate1RM()`. |
| **`BiometricsScreen`** | Analyse clinique de composition corporelle et pesée BLE. | `BiaProfile`, `List<BodyLog>`. | `onTriggerBLEScan()`, `onUpdateGoal()`, `onExportHealthConnect()`. |
| **`CatalogScreen`** | Explorateur des 1 300+ exercices openGym avec recherche instantanée. | `SearchFilter(muscle, equipment)`. | `onSelectExercise()`, `onApplyFilter()`. |

---

### ⌚ Écrans Wear OS (Compose for Wear OS)

| Écran | Rôle Métier | Interactions Spécifiques | Cas Limites / Edge-Cases |
| :--- | :--- | :--- | :--- |
| **`WearLiveWorkoutScreen`** | Affichage grand format de la série en cours avec cardio en direct. | • Bouton géant `VALIDER SÉRIE`<br>• Couronne rotative pour ajuster le poids. | Écran verrouillable contre la transpiration (`Water Lock`). |
| **`WearRestTimerScreen`** | Compte à rebours de récupération visuel et haptique. | • Anneau circulaire dégressif<br>• Bouton `+30s` et `PASSER`. | Vibration continue même en veille profonde via `AlarmManager.setAlarmClock()`. |
| **`WearAmbientScreen`** | Affichage Always-On 1 Hz basse consommation. | • Fond noir absolu 100% OLED<br>• Affichage minimaliste chrono + BPM. | Désactivation des animations Compose pour éviter la surconsommation CPU. |
| **`WearSummaryScreen`** | Bilan de fin d'entraînement au poignet. | • Affichage durée, BPM moyen/max, volume total (kg). | Persistance locale immédiate dans Room avant tentative de synchro Bluetooth. |

---

## 🛡️ 4. Matrice de Résolution des Cas Limites (Edge-Cases)

1. **Perte de connexion Bluetooth entre Montre et Téléphone** :
   * *Comportement* : La montre et le téléphone continuent leur exécution de manière 100% autonome en écrivant dans leur base Room locale respective.
   * *Récupération* : La synchronisation s'effectue automatiquement dès la reconnexion via `WearableDataLayerManager` avec réconciliation par horodatage UTC.
2. **Fermeture inopinée ou extinction de batterie pendant la séance** :
   * *Comportement* : Chaque validation de série est immédiatement persistée de façon transactionnelle en base Room (`atomic insert`).
   * *Récupération* : Au redémarrage, l'application propose un bouton *« Reprendre la séance en cours »*.
3. **Interruption de la pesée BLE (descente prématurée de la balance)** :
   * *Comportement* : Timeout de sécurité après 25 secondes si les 4 fragments de télémétrie ne sont pas reçus.
   * *Retour utilisateur* : Message clair : *"Pesée incomplète — Veuillez rester immobile sur la balance jusqu'au signal sonore"*.
