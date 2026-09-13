package app.bodyforger.core.ble

import android.bluetooth.BluetoothGatt
import android.util.Log
import app.bodyforger.core.ble.huawei.HuaweiCharacteristic
import app.bodyforger.core.ble.huawei.HuaweiGattProfile

/**
 * Logs the GATT profile a device actually announces, against the map we expect.
 *
 * Our map was only ever read from one model, so this is what shows a profile hypothesis
 * failing on another — rather than leaving it to be inferred from a silent failure.
 */
internal fun BluetoothGatt.logAnnouncedProfile(expected: HuaweiGattProfile, tag: String) {
    Log.d(tag, "--- GATT profile announced by the device ---")
    for (service in services) {
        for (characteristic in service.characteristics) {
            val known = expected.characteristicOf(characteristic.uuid)
            val hasDescriptor =
                characteristic.getDescriptor(HuaweiGattProfile.CLIENT_CONFIG_DESCRIPTOR) != null
            Log.d(
                tag,
                "  %s props=0x%02x cccd=%s %s".format(
                    characteristic.uuid,
                    characteristic.properties,
                    if (hasDescriptor) "yes" else "NO",
                    known?.name ?: "(not in our map)"
                )
            )
        }
    }

    val announced = services.flatMap { it.characteristics }.map { it.uuid }.toSet()
    val missing = HuaweiCharacteristic.entries.filter { expected[it] !in announced }
    if (missing.isNotEmpty()) Log.w(tag, "absent from the device: ${missing.joinToString()}")
}

/** First bytes of a frame, for diagnosis. */
internal fun ByteArray.toDiagnosticHex(limit: Int = 40): String =
    take(limit).joinToString("") { "%02x".format(it) } + if (size > limit) "…" else ""
