# Firebase Security

## 1. Why `google-services.json` is public, and why that is not the point

`app-mobile/google-services.json` is committed to a public repository. That is deliberate.

This file is **client configuration**: `project_id`, project number, storage bucket, application
id, and an Android API key. It holds no private key and no service account — those must never be
committed, and never have been.

It is **designed to be embedded in the APK**. Anyone who downloads the app can unzip it and read
it. Removing it from the repository would therefore protect nothing: a Firebase API key
**identifies** the project, it **authorises** nothing on its own.

GitHub's secret scanner flags the key on the `AIza…` pattern, without distinguishing a client key
from a server key. The alert is expected.

**What actually protects the project is the rules below and the console configuration.** The key
gives the backend's address; only the rules decide what can be done there.

## 2. Committed rules

| File | Scope | Deployed |
| --- | --- | --- |
| `firestore.rules` | Database | no — no database exists yet |
| `storage.rules` | File storage | no — the service is not enabled |
| `firebase.json` | Maps each file to its service | — |

Neither service is enabled on the project. `firebase firestore:databases:list` returns nothing:
there is no `(default)` database, so there is nothing for a rule to guard and nothing a rule
could be deployed against. Firebase Storage is likewise off, which is why `firebase.json` does
not reference it — leaving it in would fail every deployment.

Both rule files are committed **ahead of the services they protect**, in total deny, so that the
day either is enabled the deny is already written and deployed in the same move. `storage.rules`
additionally needs its `storage` section added back to `firebase.json` that day.

Nothing is exposed today, and nothing has been: no database, no bucket, and no application code
calling Firebase. The SDKs are declared in `app-mobile/build.gradle.kts` in anticipation of cloud
backup (ADR 001 §E), but nothing imports them.

⚠️ **Committing these rules does not deploy them.** Until the following command has been run, the
console keeps the rules it had:

```sh
firebase deploy --only firestore:rules
```

## 3. Opening a path when synchronisation arrives

ADR 001 §E plans one document per session. The collection path is not settled, and this document
does not invent it.

When it is, the rule must stay **scoped to the owner**: an athlete reads and writes only their own
documents, the identity coming from `request.auth.uid` and not from a field the client could
choose. The HUID (ADR 001 §D) identifies the athlete to the scales, not to the cloud: it is
guessable in seventeen digits and proves nothing.

Opening a path is a deliberate act, done path by path, never through a wildcard.

## 4. To be checked in the console, outside the repository

These settings do not live in Git and must be checked by hand on
`console.firebase.google.com`:

1. **Firestore and Storage rules actually deployed** — compare against this repository.
2. **Enabled authentication providers.** An open anonymous or email/password provider allows
   accounts to be created at will and the quota to be burned through.
3. **API key restrictions**, on `console.cloud.google.com`: limit it to the `app.bodyforger.mobile`
   package and to the signing SHA-1 fingerprint.
4. **App Check**, the day a path opens: it attests that the call really comes from the
   application, which the key alone does not.
