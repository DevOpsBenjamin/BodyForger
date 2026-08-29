package app.bodyforger.core.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * The real BLE scan.
 *
 * No hardware filter is applied: Android filters on the GAP name or a service UUID, and this
 * family publishes neither usefully — `docs/BLE_PROTOCOL.md` §1.
 */
@SuppressLint("MissingPermission")
class AndroidScaleScanner(private val context: Context) : ScaleScanner {

    companion object {
        const val TAG = "BodyForgerBle"
        const val BLUETOOTH_UNAVAILABLE = -1

        /** Translates an Android scan failure code. */
        fun describe(errorCode: Int): String = when (errorCode) {
            ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "un scan est déjà en cours."
            ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "enregistrement refusé par le système."
            ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "erreur interne de la pile Bluetooth."
            ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "scan non pris en charge par cet appareil."
            SCAN_TOO_FREQUENTLY -> "Android a bridé la recherche : trop de scans en peu de temps. Réessayez dans une trentaine de secondes."
            BLUETOOTH_UNAVAILABLE -> "Bluetooth indisponible ou éteint."
            else -> "échec inattendu (code $errorCode)."
        }

        /** `SCAN_FAILED_SCANNING_TOO_FREQUENTLY`, absent from older public constants. */
        const val SCAN_TOO_FREQUENTLY = 6
    }

    override fun scan(identifier: ScaleIdentifier): Flow<DiscoveredScale> = callbackFlow {
        val manager = context.getSystemService(BluetoothManager::class.java)
        val scanner = manager?.adapter?.bluetoothLeScanner
        if (scanner == null) {
            Log.w(TAG, "Bluetooth indisponible ou éteint")
            close(ScanRejected(BLUETOOTH_UNAVAILABLE, "Bluetooth indisponible ou éteint."))
            return@callbackFlow
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val advertised = result.scanRecord?.deviceName ?: return
                trySend(
                    DiscoveredScale(
                        deviceAddress = result.device.address,
                        advertisedName = advertised,
                        recognised = identifier.identify(advertised),
                        signalStrengthDbm = result.rssi
                    )
                )
            }

            override fun onScanFailed(errorCode: Int) {
                Log.w(TAG, "scan refusé : ${describe(errorCode)}")
                close(ScanRejected(errorCode, describe(errorCode)))
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        Log.d(TAG, "scan démarré")
        scanner.startScan(emptyList(), settings, callback)
        awaitClose {
            Log.d(TAG, "scan arrêté")
            runCatching { scanner.stopScan(callback) }
        }
    }
}
