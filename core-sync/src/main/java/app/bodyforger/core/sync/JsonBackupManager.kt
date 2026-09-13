package app.bodyforger.core.sync

/**
 * The local backup format, per ADR 001 §E.
 *
 * Reading and writing live apart, sharing [BackupKeys] so the two cannot drift.
 */
object JsonBackupManager {

    const val CURRENT_SCHEMA_VERSION = 1

    fun serialize(payload: BodyForgerBackupPayload): String = BackupSerializer.write(payload)

    fun deserialize(jsonString: String): BodyForgerBackupPayload = BackupDeserializer.read(jsonString)
}
