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
import app.bodyforger.core.ble.huawei.HuaweiFraming
import app.bodyforger.core.ble.huawei.HuaweiGattProfile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

/**
 * The real Bluetooth transport, above Android's GATT stack.
 *
 * Platform constraints that shape it — one operation at a time, answers as notifications,
 * error 133 — are described in `docs/BLE_PROTOCOL.md` §7.
 *
 * Frames are reassembled here: a driver receives whole payloads, never fragments.
 */
@SuppressLint("MissingPermission")
class AndroidGattTransport(
    private val context: Context,
    private val device: BluetoothDevice,
    private val profile: HuaweiGattProfile,
    private val operationTimeoutMs: Long = DEFAULT_OPERATION_TIMEOUT_MS
) : ScaleTransport {

    private val inbox = GattFrameInbox(profile, TAG)
    override val incoming: Flow<ScaleNotification> = inbox.incoming

    /** Android allows one GATT operation in flight; this queues them. */
    private val gattLock = Mutex()

    private var gatt: BluetoothGatt? = null
    private var connection: CompletableDeferred<Boolean>? = null
    private var services: CompletableDeferred<Boolean>? = null
    private var pendingWrite: CompletableDeferred<Boolean>? = null
    private var pendingDescriptor: CompletableDeferred<Boolean>? = null

    /** Connects and discovers services, retrying: error 133 commonly hits the first try. */
    override suspend fun connect(): Boolean {
        repeat(CONNECTION_ATTEMPTS) { attempt ->
            if (attemptConnect()) return true
            Log.w(TAG, "tentative de connexion ${attempt + 1}/$CONNECTION_ATTEMPTS échouée")
            closeInternal()
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
        if (ready) gatt?.logAnnouncedProfile(profile, TAG)
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
            Log.w(TAG, "caractéristique introuvable : $characteristic")
            return@withLock false
        }
        val connected = gatt ?: return@withLock false
        if (!connected.setCharacteristicNotification(target, enabled)) {
            Log.w(TAG, "abonnement refusé : $characteristic")
            return@withLock false
        }

        val descriptor = target.getDescriptor(HuaweiGattProfile.CLIENT_CONFIG_DESCRIPTOR)
        if (descriptor == null) {
            Log.w(TAG, "pas de descripteur de notification sur $characteristic")
            return@withLock false
        }
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

        if (enabled) inbox.open(characteristic) else inbox.close(characteristic)

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
        inbox.clear()
        connection = null
        services = null
        pendingWrite = null
        pendingDescriptor = null
    }

    private fun resolve(characteristic: HuaweiCharacteristic): BluetoothGattCharacteristic? {
        val uuid = profile[characteristic] ?: return null
        return gatt?.services?.firstNotNullOfOrNull { it.getCharacteristic(uuid) }
    }

    private val callback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "connecté (statut $status)")
                    connection?.complete(status == BluetoothGatt.GATT_SUCCESS)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "déconnecté (statut $status)")
                    connection?.complete(false)
                    services?.complete(false)
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
        ) = inbox.deliver(characteristic.uuid, value)

        @Deprecated("Requis avant Android 13, qui livre la valeur en argument.")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) = inbox.deliver(characteristic.uuid, characteristic.value ?: ByteArray(0))
    }

    /** Reassembles, emitting only complete payloads. Never blocks the Bluetooth callback. */
    companion object {
        /** Log tag: `adb logcat -s BodyForgerBle`. */
        const val TAG = "BodyForgerBle"

        const val DEFAULT_OPERATION_TIMEOUT_MS = 5_000L
        const val CONNECTION_TIMEOUT_MS = 15_000L

        /** Error 133 hits the first attempt, rarely the third. */
        const val CONNECTION_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 600L
    }
}
