package app.bodyforger.core.ble.huawei

/**
 * Fixed protocol commands that are **already complete frames**.
 *
 * Written with `writeRaw`, never `write` — `docs/BLE_PROTOCOL.md` §3.
 */
object HuaweiCommands {

    /** Generic command: it queries, arms or disarms, depending on the characteristic. */
    val QUERY = byteArrayOf(0xDB.toByte(), 0x03, 0x00, 0xC1.toByte(), 0x40)

    /** Host capabilities, written without response. */
    val HOST_CAPABILITIES = byteArrayOf(
        0x5A, 0x00, 0x05, 0x00, 0x01, 0x37, 0x01, 0x00, 0x1C, 0xA9.toByte()
    )
}
