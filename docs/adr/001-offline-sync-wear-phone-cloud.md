# ADR 001 : Architecture de Synchronisation Hors-Ligne, Indépendance Wear OS & Stratégie Cloud Backup

## 📋 Statut
**Accepté & Validé** (2026-08-28)

---

## 🎯 1. Contexte & Problématique

BodyForger est une application d'entraînement de force et de suivi biométrique (DEXA / BIA 8 électrodes) conçue selon le paradigme **Local-First**. L'écosystème comprend :
1. **Une application Mobile Android** (Source de vérité principale, calculs de surcharge progressive, graphiques BIA).
2. **Une application Wear OS Standalone** (Exécution de séance autonome à la salle sans smartphone, capture cardio, chrono de repos vibrant, mode Ambient AOD).
3. **Une passerelle Google Health Connect** (Inter-opérabilité avec Samsung Health, Google Fit, Withings).
4. **Une couche optionnelle de Backup Cloud** (Restauration en cas de changement d'appareil).

Les contraintes critiques sont :
* **Déconnexion Bluetooth fréquente** : À la salle de sport, la montre doit fonctionner à 100% sans smartphone à proximité.
* **Autonomie de batterie Wear OS** : Éviter le polling capteur continu et les réveils processeurs inutiles.
* **Résilience aux crashs / extinction de batterie** : Persistance atomique par série.
* **Intégrité des données** : Éviter les doublons ou conflits de synchronisation.

---

## 🏛️ 2. Décisions d'Architecture

### A. Rôles des Appareils & Modèle "Append-Only"
* **Immutabilité des Séances (`WorkoutSession` & `WorkoutSet`)** :
  * Chaque séance et série reçoit un `UUID` unique dès sa création (qu'elle soit initiée sur la montre ou le smartphone).
  * Les séances terminées sont traitées en **Append-Only** (non mutables).
  * Les écritures en base Room utilisent `OnConflictStrategy.REPLACE` / `IGNORE` basé sur l'UUID, garantissant l'**idempotence absolue** des synchronisations.
* **Horodatage Monotone** : Pour les données éditables (ex: profil utilisateur, modification d'un nom de routine), la résolution de conflit applique la règle **Last-Write-Wins (LWW)** basée sur `updatedAtEpochMs`.

### B. Protocole de Synchronisation Wear OS ↔ Mobile (Wearable Data Layer)
1. **Réconciliation par Inventaire (Handshake UUID)** :
   * Ne jamais se reposer uniquement sur les événements ponctuels `onDataChanged` (qui peuvent être manqués si l'application est en sommeil ou tuée par l'OEM).
   * À chaque reconnexion Bluetooth/Wi-Fi :
     1. La montre et le smartphone échangent l'inventaire de leurs UUID de séances (`session_inventory_request` / `session_inventory_response`).
     2. Tout delta manquant d'un côté ou de l'autre est retransféré automatiquement.
2. **Format des Payloads & Limite DataItem (100 Ko)** :
   * Une séance de musculation typique (15 à 25 séries avec BPM agrégé) pèse entre **5 et 15 Ko** en JSON.
   * Le transfert passe par `DataClient` (DataItem compact).
   * Si des données continues haute-fréquence sont activées, le payload est compressé (GZIP) ou transféré via `ChannelClient`.
3. **Versioning du Protocole (`schemaVersion`)** :
   * Tous les messages Data Layer incluent un champ `schemaVersion: 1` pour permettre l'évolution du modèle sans rupture si la montre et le téléphone sont sur des versions d'app différentes.

### C. Gestion Batterie & Capteurs sur Wear OS
* **Health Services API (`ExerciseClient`)** :
  * Utilisation exclusive de `HealthServices` de Wear OS au lieu de lire les capteurs bruts via `SensorManager`.
  * Délégation du batching matériel de la fréquence cardiaque et des calories à l'OS.
* **Foreground Service & Ongoing Activity** :
  * Maintien d'un service de premier plan avec notification permanente (`OngoingActivity`) pour garantir que l'OS ne tue pas le runner en cours d'exercice.

### D. Flags de Synchronisation dans Room DB
Chaque entité stockée dans Room DB porte un statut de synchronisation :
* `LOCAL_ONLY` : Donnée créée localement, non encore propagée.
* `SYNCED_PEER` : Synchronisée entre Montre et Smartphone.
* `SYNCED_CLOUD` : Archivée dans le Cloud.

### E. Stratégie Cloud Backup (Firestore / Storage)
* **Agrégation en Document Unique** :
  * Pour respecter les quotas gratuits (Spark 20 000 écritures/jour) et la limite de 1 Mio par document : **1 séance = 1 document Firestore**.
  * Les métadonnées de haut niveau (date, durée, volume total, catégories Health Connect) sont des champs indexables.
  * Le détail des séries est stocké dans un tableau/blob imbriqué.
* **Procédure de Restauration Testable** :
  * Option de restauration intégrale ("Restaurer les données Cloud") sur installation propre.
  * Option d'export/import de fichier local (`bodyforger_backup.json`) pour garantir la souveraineté des données de l'utilisateur.

---

## ⚖️ 3. Conséquences & Bénéfices

* **Fiabilité 100% Hors-Ligne** : L'utilisateur peut laisser son téléphone au vestiaire et s'entraîner uniquement avec sa montre.
* **Zéro Doublon** : L'idempotence par UUID élimine les séances dupliquées.
* **Économie de Batterie** : Batching Health Services + DataLayer local au lieu de requêtes réseau directes en séance.
* **Zéro Risque de Dépassement Quota Cloud** : 1 écriture Firestore par séance terminée.
