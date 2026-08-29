package app.bodyforger.core.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Le scan BLE réel.
 *
 * Aucun filtre matériel n'est posé : les filtres d'Android portent sur le nom GAP ou sur un
 * UUID de service, or la famille Haige ne publie ni l'un ni l'autre de façon exploitable —
 * son modèle vit dans le nom annoncé. Le tri se fait donc côté logiciel, en confiant chaque
 * nom au pilote.
 */
@SuppressLint("MissingPermission")
class AndroidScaleScanner(private val context: Context) : ScaleScanner {

    override fun scan(identifier: ScaleIdentifier): Flow<DiscoveredScale> = callbackFlow {
        val manager = context.getSystemService(BluetoothManager::class.java)
        val scanner = manager?.adapter?.bluetoothLeScanner
        if (scanner == null) {
            // Bluetooth absent ou éteint : un flux vide plutôt qu'une exception, l'appelant
            // n'a rien à réparer.
            close()
            return@callbackFlow
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // `scanRecord.deviceName` est le nom **annoncé**, celui qui porte le modèle.
                // `result.device.name` est le nom GAP, générique sur cette famille.
                val advertised = result.scanRecord?.deviceName ?: return
                val recognised = identifier.identify(advertised) ?: return
                trySend(
                    DiscoveredScale(
                        deviceAddress = result.device.address,
                        advertisedName = advertised,
                        recognised = recognised,
                        signalStrengthDbm = result.rssi
                    )
                )
            }

            override fun onScanFailed(errorCode: Int) {
                close()
            }
        }

        val settings = ScanSettings.Builder()
            // La balance ne s'annonce que quelques secondes après un tapotement : un scan
            // économe la manquerait.
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(emptyList(), settings, callback)
        awaitClose { scanner.stopScan(callback) }
    }
}
