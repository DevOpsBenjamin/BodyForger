# Sécurité Firebase

## 1. Pourquoi `google-services.json` est public, et pourquoi ce n'est pas le sujet

`app-mobile/google-services.json` est versionné dans un dépôt public. C'est délibéré.

Ce fichier est la **configuration client** : `project_id`, numéro de projet, bucket de stockage,
identifiant d'application, et une clé API Android. Il ne contient aucune clé privée ni compte de
service — ceux-là ne doivent jamais être versionnés, et ne l'ont jamais été.

Il est **conçu pour être embarqué dans l'APK**. Quiconque télécharge l'application peut le
dézipper et le lire. Le retirer du dépôt ne protégerait donc rien : la clé API Firebase
**identifie** le projet, elle n'**autorise** rien par elle-même.

Le scanner de secrets de GitHub signale la clé sur le motif `AIza…`, sans distinguer une clé
client d'une clé serveur. L'alerte est attendue.

**Ce qui protège réellement, ce sont les règles ci-dessous et la configuration de la console.**
La clé donne l'adresse du backend ; seules les règles décident de ce qu'on peut y faire.

## 2. Règles versionnées

| Fichier | Portée |
| --- | --- |
| `firestore.rules` | Base de données |
| `storage.rules` | Stockage de fichiers |
| `firebase.json` | Associe chaque fichier à son service |

Les deux sont en **refus total**. Aucun code de l'application n'appelle Firebase à ce jour : les
SDK sont déclarés dans `app-mobile/build.gradle.kts` en prévision de la sauvegarde cloud
(ADR 001 §E), mais rien ne les importe. Le refus ne casse donc rien, et ferme tout ce qui
resterait ouvert d'une configuration en mode test.

⚠️ **Versionner ces règles ne les déploie pas.** Tant que la commande suivante n'a pas été
lancée, la console garde les règles qu'elle avait :

```sh
firebase deploy --only firestore:rules,storage
```

## 3. Ouvrir un chemin quand la synchronisation arrivera

L'ADR 001 §E prévoit une séance par document. Le chemin de collection n'est pas arrêté, et ce
document ne l'invente pas.

Quand il le sera, la règle doit rester **cadrée sur le propriétaire** : un athlète ne lit et
n'écrit que ses propres documents, l'identité venant de `request.auth.uid` et non d'un champ que
le client pourrait choisir. Le HUID (ADR 001 §D) identifie l'athlète face aux balances, pas face
au cloud : il est deviné en dix-sept chiffres et ne prouve rien.

Ouvrir un chemin est un acte délibéré, à faire chemin par chemin, jamais par un joker.

## 4. À vérifier dans la console, hors du dépôt

Ces réglages ne vivent pas dans Git et doivent être contrôlés à la main sur
`console.firebase.google.com` :

1. **Règles Firestore et Storage effectivement déployées** — comparer à ce dépôt.
2. **Fournisseurs d'authentification activés.** Un fournisseur anonyme ou email/mot de passe
   ouvert permet de créer des comptes à volonté et de consommer le quota.
3. **Restrictions de la clé API**, sur `console.cloud.google.com` : la limiter au package
   `app.bodyforger.mobile` et à l'empreinte SHA-1 de signature.
4. **App Check**, le jour où un chemin s'ouvre : il atteste que l'appel vient bien de
   l'application, ce que la clé seule ne fait pas.
