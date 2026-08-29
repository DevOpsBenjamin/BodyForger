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
 * Le scan BLE réel.
 *
 * Aucun filtre matériel n'est posé : les filtres d'Android portent sur le nom GAP ou sur un
 * UUID de service, or la famille Haige ne publie ni l'un ni l'autre de façon exploitable —
 * son modèle vit dans le nom annoncé. Le tri se fait donc côté logiciel, en confiant chaque
 * nom au pilote.
 */
@SuppressLint("MissingPermission")
class AndroidScaleScanner(private val context: Context) : ScaleScanner {

    companion object {
        const val TAG = "BodyForgerBle"
        const val BLUETOOTH_UNAVAILABLE = -1

        /**
         * Traduit un code d'échec d'Android.
         *
         * Le bridage est le plus déroutant : il survient après quelques scans rapprochés,
         * dure une trentaine de secondes, et ne se distingue d'une balance éteinte que par ce
         * code.
         */
        fun describe(errorCode: Int): String = when (errorCode) {
            ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "un scan est déjà en cours."
            ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "enregistrement refusé par le système."
            ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "erreur interne de la pile Bluetooth."
            ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "scan non pris en charge par cet appareil."
            SCAN_TOO_FREQUENTLY -> "Android a bridé la recherche : trop de scans en peu de temps. Réessayez dans une trentaine de secondes."
            BLUETOOTH_UNAVAILABLE -> "Bluetooth indisponible ou éteint."
            else -> "échec inattendu (code $errorCode)."
        }

        /** `SCAN_FAILED_SCANNING_TOO_FREQUENTLY`, absent des constantes publiques anciennes. */
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
                // `scanRecord.deviceName` est le nom **annoncé**, celui qui porte le modèle.
                // `result.device.name` est le nom GAP, générique sur cette famille.
                // Un appareil sans nom annoncé n'apprend rien à personne : ni reconnaissable,
                // ni identifiable à l'œil.
                val advertised = result.scanRecord?.deviceName ?: return
                trySend(
                    DiscoveredScale(
                        deviceAddress = result.device.address,
                        advertisedName = advertised,
                        // Un appareil inconnu est émis quand même : c'est ce qui permet de
                        // voir qu'une balance est là sous un nom que le pilote ne sait pas
                        // encore reconnaître.
                        recognised = identifier.identify(advertised),
                        signalStrengthDbm = result.rssi
                    )
                )
            }

            override fun onScanFailed(errorCode: Int) {
                // ⚠️ Android bride les applications qui démarrent plus de cinq scans en trente
                // secondes, et le refuse alors **en silence** pendant une demi-minute. Fermer
                // sans rien dire laissait l'écran sur une recherche éternelle.
                Log.w(TAG, "scan refusé : ${describe(errorCode)}")
                close(ScanRejected(errorCode, describe(errorCode)))
            }
        }

        val settings = ScanSettings.Builder()
            // La balance ne s'annonce que quelques secondes après un tapotement : un scan
            // économe la manquerait.
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
