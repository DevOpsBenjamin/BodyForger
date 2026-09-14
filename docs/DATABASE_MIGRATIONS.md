# Database migrations

## 1. Where we stand

`fallbackToDestructiveMigration()` has been removed. All migrations from schema 5 to current
are registered in `BodyForgerDatabases.ALL_MIGRATIONS` and verified with `MigrationTestHelper`.

Every schema change must supply an explicit `Migration` step; an unmigrated schema bump will
loudly refuse to open rather than wipe the athlete's training logs or scale weigh-ins.

## 2. Why the schemas are exported now, and not later

`core-database/schemas/` holds one JSON file per schema version, written at build time by Room
and versioned in the repository.

The timing is the whole point. To write a migration *from* version 7, you need to know exactly
what version 7 looked like. Ship version 7 without exporting its schema and there is no
reference left: it has to be reconstructed by hand, and Room compares an identity hash that
does not forgive an approximation.

The export costs nothing today — one JSON file per version bump — and it is what makes
`MigrationTestHelper` possible, which opens a real database at the old version from that file,
applies the migration and checks the data survived.

## 3. Changing the schema once released

1. Change the entities.
2. Raise `version` in `BodyForgerDatabase`.
3. Write a `Migration(previous, new)` and register it with `addMigrations`.
4. Build: Room writes the new JSON under `schemas/`. **Commit it.**
5. Write a migration test against the previous JSON.

Room chains the steps by itself: a user stuck on version 4 installing version 7 goes through
4→5, 5→6, 6→7. Every intermediate migration must therefore be kept for as long as a user might
still be running that version.

### Adding a column

The common case, and the easy one:

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE athlete_identity ADD COLUMN name TEXT")
    }
}
```

The declared type has to match what Room expects — `TEXT` for a `String?`, `INTEGER` for an
`Int`, `REAL` for a `Double`. A non-null column also needs a `DEFAULT`, since existing rows
have no value for it.

### Removing or renaming a column

SQLite cannot do it directly on the versions we support. The move is: create the table in its
new shape under a temporary name, copy the rows across, drop the old table, rename the new one.

The append-only model (ADR 001 §A) keeps this rare: finished sessions are immutable, so most
changes are additions.

## 4. What Room checks

At open time, Room compares the schema found on the device with the identity hash of the
version compiled into the app. A migration that forgets a column makes the app refuse to open
the database, rather than working against a schema it misreads. The failure is loud, which is
the point.
