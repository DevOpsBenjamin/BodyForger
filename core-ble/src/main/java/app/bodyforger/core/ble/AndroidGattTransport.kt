package app.bodyforger.core.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import app.bodyforger.core.ble.huawei.HuaweiCharacteristic
import app.bodyforger.core.ble.huawei.HuaweiFrameMagic
import app.bodyforger.core.ble.huawei.HuaweiFrameReassembler
import app.bodyforger.core.ble.huawei.HuaweiFraming
import app.bodyforger.core.ble.huawei.HuaweiGattProfile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

/**
 * Le transport Bluetooth réel, au-dessus de la pile GATT d'Android.
 *
 * Deux contraintes de la plateforme dictent cette conception, et les ignorer donne un code
 * qui marche en démonstration puis échoue en vrai :
 *
 * * **Android n'accepte qu'une opération GATT à la fois.** Écrire pendant qu'une écriture est
 *   en vol fait échouer silencieusement la seconde. Tout passe donc par un verrou, et chaque
 *   opération attend son rappel avant que la suivante ne parte.
 * * **Les réponses n'arrivent presque jamais en retour d'écriture** mais par notification, sur
 *   la même caractéristique. Il faut s'y abonner avant d'écrire, sinon la balance répond dans
 *   le vide.
 *
 * Les trames sont recollées ici : le pilote reçoit des charges complètes, jamais des
 * fragments. Un recolleur par caractéristique, puisque plusieurs peuvent émettre en parallèle.
 */
@SuppressLint("MissingPermission")
class AndroidGattTransport(
    private val context: Context,
    private val device: BluetoothDevice,
    private val profile: HuaweiGattProfile,
    private val operationTimeoutMs: Long = DEFAULT_OPERATION_TIMEOUT_MS
) : ScaleTransport {

    private val notifications = MutableSharedFlow<ScaleNotification>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    override val incoming: Flow<ScaleNotification> = notifications.asSharedFlow()

    /** Android n'admet qu'une opération GATT en vol : ce verrou les met en file. */
    private val gattLock = Mutex()

    private var gatt: BluetoothGatt? = null
    private var connection: CompletableDeferred<Boolean>? = null
    private var services: CompletableDeferred<Boolean>? = null
    private var pendingWrite: CompletableDeferred<Boolean>? = null
    private var pendingDescriptor: CompletableDeferred<Boolean>? = null

    private val reassemblers = mutableMapOf<HuaweiCharacteristic, HuaweiFrameReassembler>()

    /**
     * Établit la connexion et découvre les services, avec reprise.
     *
     * ⚠️ Une première tentative échoue couramment sur Android sans que rien ne soit anormal —
     * c'est l'échec 133, bien connu, qui frappe surtout un appareil jamais appairé ou dont le
     * scan vient à peine de s'arrêter. Réessayer suffit presque toujours. Sans cette reprise,
     * un appairage parfaitement légitime paraît refusé.
     */
    override suspend fun connect(): Boolean {
        repeat(CONNECTION_ATTEMPTS) { attempt ->
            if (attemptConnect()) return true
            Log.w(TAG, "tentative de connexion ${attempt + 1}/$CONNECTION_ATTEMPTS échouée")
            closeInternal()
            // Laisser la pile Bluetooth se libérer avant de réessayer : enchaîner
            // immédiatement reproduit le même échec.
            delay(RETRY_DELAY_MS)
        }
        return false
    }

    private suspend fun attemptConnect(): Boolean = gattLock.withLock {
        if (gatt != null) return@withLock true

        val connected = CompletableDeferred<Boolean>().also { connection = it }
        val discovered = CompletableDeferred<Boolean>().also { services = it }

        Log.d(TAG, "connexion à ${device.address}")
        gatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        if (withTimeoutOrNull(CONNECTION_TIMEOUT_MS) { connected.await() } != true) {
            Log.w(TAG, "connexion refusée ou expirée")
            return@withLock false
        }
        if (gatt?.discoverServices() != true) {
            Log.w(TAG, "découverte des services non démarrée")
            return@withLock false
        }
        val ready = withTimeoutOrNull(CONNECTION_TIMEOUT_MS) { discovered.await() } == true
        Log.d(TAG, if (ready) "services découverts" else "découverte des services échouée")
        if (ready) dumpProfile()
        ready
    }

    override suspend fun subscribe(characteristic: HuaweiCharacteristic): Boolean =
        setNotification(characteristic, enabled = true)

    override suspend fun unsubscribe(characteristic: HuaweiCharacteristic): Boolean =
        setNotification(characteristic, enabled = false)

    private suspend fun setNotification(
        characteristic: HuaweiCharacteristic,
        enabled: Boolean
    ): Boolean = gattLock.withLock {
        val target = resolve(characteristic)
        if (target == null) {
            // Caractéristique absente : l'hypothèse de profil GATT ne tient pas sur ce modèle.
            Log.w(TAG, "caractéristique introuvable : $characteristic")
            return@withLock false
        }
        val connected = gatt ?: return@withLock false
        if (!connected.setCharacteristicNotification(target, enabled)) {
            Log.w(TAG, "abonnement refusé : $characteristic")
            return@withLock false
        }

        // Activer les notifications côté Android ne suffit pas : il faut aussi le dire à la
        // balance, en écrivant dans le descripteur de configuration client.
        val descriptor = target.getDescriptor(HuaweiGattProfile.CLIENT_CONFIG_DESCRIPTOR)
        if (descriptor == null) {
            // Sans descripteur de configuration, la balance ne peut pas être avertie qu'on
            // écoute. Certaines caractéristiques n'en ont pas et ne notifient jamais.
            Log.w(TAG, "pas de descripteur de notification sur $characteristic")
            return@withLock false
        }
        // ⚠️ Notification et indication ne s'activent pas avec la même valeur, et écrire
        // l'une pour l'autre fait rejeter le descripteur. La famille Haige emploie
        // massivement l'**indication** — l'acquittement que la notification n'a pas — et une
        // seule de ses caractéristiques notifie vraiment. Le choix se lit donc dans les
        // propriétés de chaque caractéristique, jamais supposé.
        val indicates = target.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0
        val notifies = target.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
        if (!indicates && !notifies) {
            Log.w(TAG, "$characteristic ne sait ni notifier ni indiquer (props=0x%02x)".format(target.properties))
            return@withLock false
        }
        val value = when {
            !enabled -> BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            notifies -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        }
        Log.d(TAG, "abonnement $characteristic en ${if (notifies) "notification" else "indication"}")

        val acknowledged = CompletableDeferred<Boolean>().also { pendingDescriptor = it }
        val started = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            connected.writeDescriptor(descriptor, value) == BluetoothGatt.GATT_SUCCESS
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = value
            @Suppress("DEPRECATION")
            connected.writeDescriptor(descriptor)
        }
        if (!started) {
            Log.w(TAG, "écriture du descripteur non démarrée : $characteristic")
            return@withLock false
        }

        if (enabled) reassemblers[characteristic] = HuaweiFrameReassembler()
        else reassemblers.remove(characteristic)

        val ok = withTimeoutOrNull(operationTimeoutMs) { acknowledged.await() } == true
        Log.d(TAG, "abonnement $characteristic : ${if (ok) "ok" else "ÉCHEC (descripteur non acquitté)"}")
        ok
    }

    override suspend fun write(
        characteristic: HuaweiCharacteristic,
        payload: ByteArray,
        withResponse: Boolean
    ): Boolean {
        val magic = if (characteristic.protection == HuaweiCharacteristic.Protection.CLEAR) {
            HuaweiFrameMagic.HOST_CLEAR
        } else {
            HuaweiFrameMagic.HOST_ENCRYPTED
        }
        // Chaque trame part séparément et attend son acquittement : la pile Android ne
        // tolère pas deux écritures simultanées.
        for (frame in HuaweiFraming.split(payload, magic)) {
            if (!writeFrame(characteristic, frame, withResponse)) return false
        }
        return true
    }

    override suspend fun writeRaw(
        characteristic: HuaweiCharacteristic,
        frame: ByteArray,
        withResponse: Boolean
    ): Boolean = writeFrame(characteristic, frame, withResponse)

    private suspend fun writeFrame(
        characteristic: HuaweiCharacteristic,
        frame: ByteArray,
        withResponse: Boolean
    ): Boolean = gattLock.withLock {
        val target = resolve(characteristic)
        if (target == null) {
            Log.w(TAG, "caractéristique introuvable à l'écriture : $characteristic")
            return@withLock false
        }
        val connected = gatt ?: return@withLock false
        val writeType = if (withResponse) {
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        } else {
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        }

        val acknowledged = CompletableDeferred<Boolean>().also { pendingWrite = it }
        val started = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            connected.writeCharacteristic(target, frame, writeType) == BluetoothGatt.GATT_SUCCESS
        } else {
            @Suppress("DEPRECATION")
            target.value = frame
            @Suppress("DEPRECATION")
            target.writeType = writeType
            @Suppress("DEPRECATION")
            connected.writeCharacteristic(target)
        }
        if (!started) {
            Log.w(TAG, "écriture non démarrée : $characteristic")
            return@withLock false
        }

        val ok = withTimeoutOrNull(operationTimeoutMs) { acknowledged.await() } == true
        Log.d(TAG, "écriture $characteristic (${frame.size} o) : ${if (ok) "ok" else "ÉCHEC"}")
        ok
    }

    override fun close() = closeInternal()

    private fun closeInternal() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        reassemblers.clear()
        connection = null
        services = null
        pendingWrite = null
        pendingDescriptor = null
    }

    /**
     * Journalise ce que la balance expose réellement.
     *
     * La carte GATT n'a été relevée que sur un modèle : c'est ici que l'on voit si elle tient
     * sur un autre matériel, plutôt que de le déduire d'un échec muet.
     */
    private fun dumpProfile() {
        val services = gatt?.services ?: return
        Log.d(TAG, "--- profil GATT annoncé par l'appareil ---")
        for (service in services) {
            for (characteristic in service.characteristics) {
                val known = profile.characteristicOf(characteristic.uuid)
                val cccd = characteristic.getDescriptor(HuaweiGattProfile.CLIENT_CONFIG_DESCRIPTOR)
                Log.d(
                    TAG,
                    "  ${characteristic.uuid} props=0x%02x cccd=%s %s".format(
                        characteristic.properties,
                        if (cccd != null) "oui" else "NON",
                        known?.name ?: "(inconnue de notre carte)"
                    )
                )
            }
        }
        val missing = HuaweiCharacteristic.entries.filter { resolve(it) == null }
        if (missing.isNotEmpty()) Log.w(TAG, "absentes de l'appareil : ${missing.joinToString()}")
    }

    private fun resolve(characteristic: HuaweiCharacteristic): BluetoothGattCharacteristic? {
        val uuid = profile[characteristic] ?: return null
        return gatt?.services?.firstNotNullOfOrNull { it.getCharacteristic(uuid) }
    }

    private val callback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // Le statut 133 est l'échec générique d'Android : il ne dit rien de plus.
                    Log.d(TAG, "connecté (statut $status)")
                    connection?.complete(status == BluetoothGatt.GATT_SUCCESS)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "déconnecté (statut $status)")
                    connection?.complete(false)
                    services?.complete(false)
                    // Une déconnexion en vol laisserait une opération suspendue jusqu'au délai.
                    pendingWrite?.complete(false)
                    pendingDescriptor?.complete(false)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            services?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            pendingWrite?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            pendingDescriptor?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) = deliver(characteristic.uuid, value)

        @Deprecated("Requis avant Android 13, qui livre la valeur en argument.")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) = deliver(characteristic.uuid, characteristic.value ?: ByteArray(0))
    }

    /**
     * Recolle la trame reçue et n'émet que lorsqu'une charge est complète.
     *
     * `tryEmit` plutôt qu'une émission suspendue : on est dans un rappel de la pile
     * Bluetooth, qu'il ne faut jamais bloquer.
     */
    private fun deliver(uuid: UUID, frame: ByteArray) {
        val characteristic = profile.characteristicOf(uuid) ?: return
        val reassembler = reassemblers.getOrPut(characteristic) { HuaweiFrameReassembler() }
        val payload = reassembler.feed(frame)
        if (payload == null) {
            Log.v(TAG, "trame partielle ou écartée sur $characteristic (${frame.size} o)")
            return
        }
        Log.d(TAG, "reçu $characteristic : ${payload.size} o")
        notifications.tryEmit(
            ScaleNotification(
                characteristic = characteristic,
                payload = payload,
                encrypted = characteristic.protection != HuaweiCharacteristic.Protection.CLEAR
            )
        )
    }

    companion object {
        /** Filtre de journal pour suivre une session : `adb logcat -s BodyForgerBle`. */
        const val TAG = "BodyForgerBle"

        const val DEFAULT_OPERATION_TIMEOUT_MS = 5_000L
        const val CONNECTION_TIMEOUT_MS = 15_000L

        /** Trois essais : l'échec 133 frappe la première tentative, rarement la troisième. */
        const val CONNECTION_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 600L
    }
}
