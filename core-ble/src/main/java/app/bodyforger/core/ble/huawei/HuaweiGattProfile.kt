package app.bodyforger.core.ble.huawei

import java.util.UUID

/**
 * A protocol characteristic and the encryption it requires.
 *
 * Three regimes coexist on one device — `docs/BLE_PROTOCOL.md` §4.
 */
enum class HuaweiCharacteristic(val protection: Protection) {
    /** Authentication request; the scale answers with its nonce. */
    AUTH_REQUEST(Protection.CLEAR),

    /** Token exchange: client nonce and token against the scale's token. */
    AUTH_TOKENS(Protection.CLEAR),

    /** Session key transport, protected by the root key. */
    SESSION_KEY(Protection.ROOT_KEY),

    /** Arms and disarms binding mode. */
    BINDING_CONTROL(Protection.CLEAR),

    /** HUID engraving in flash memory, and the tare in return. */
    HUID_REGISTRATION(Protection.SESSION_KEY),

    /** User profile and measurement acknowledgement. */
    USER_PROFILE(Protection.SESSION_KEY),

    /** Clock synchronisation; a standard Bluetooth SIG characteristic. */
    TIME_SYNC(Protection.CLEAR),

    /** Serial number. */
    SERIAL_NUMBER(Protection.CLEAR),

    /** Device configuration state. */
    DEVICE_CONFIGURATION(Protection.CLEAR),

    /** Hardware model. */
    HARDWARE_MODEL(Protection.CLEAR),

    /** Readings the scale kept offline. */
    OFFLINE_HISTORY(Protection.CLEAR),

    /** Real-time BIA telemetry stream. */
    BIA_STREAM(Protection.SESSION_KEY),

    /** Host capability announcement; written without response. */
    CAPABILITIES_REQUEST(Protection.CLEAR),

    /** The scale's capability answer. */
    CAPABILITIES_RESPONSE(Protection.CLEAR),

    /** Status sentinel: the scale pushes its events here. */
    STATUS_SENTINEL(Protection.CLEAR);

    enum class Protection { CLEAR, ROOT_KEY, SESSION_KEY }
}

/**
 * A model's GATT characteristic map.
 *
 * Read from a Scale 3 Pro; twelve of its fifteen UUIDs are proprietary, three are Bluetooth
 * SIG allocations — `docs/BLE_PROTOCOL.md` §4.
 */
data class HuaweiGattProfile(val characteristics: Map<HuaweiCharacteristic, UUID>) {

    operator fun get(characteristic: HuaweiCharacteristic): UUID? = characteristics[characteristic]

    /** Finds the characteristic matching a received UUID, or `null` when unknown. */
    fun characteristicOf(uuid: UUID): HuaweiCharacteristic? =
        characteristics.entries.firstOrNull { it.value == uuid }?.key

    companion object {
        /** Read from a Scale 3 Pro, and verified on it alone. */
        val SCALE_3_PRO = HuaweiGattProfile(
            mapOf(
                HuaweiCharacteristic.AUTH_REQUEST to uuid("02b2a08e-f8b0-4047-b1fd-f4e0efeee679"),
                HuaweiCharacteristic.AUTH_TOKENS to uuid("32330a04-15d9-421a-91c5-2a2d5c7525c9"),
                HuaweiCharacteristic.SESSION_KEY to uuid("a3d330f8-b84f-4f48-a78c-f8d1e33b597a"),
                HuaweiCharacteristic.BINDING_CONTROL to uuid("4338c65e-ed8e-4085-bbea-a25e33ca6b54"),
                HuaweiCharacteristic.HUID_REGISTRATION to uuid("42596cbe-d291-4da3-8ca6-d1ae5d1c9174"),
                HuaweiCharacteristic.USER_PROFILE to uuid("8cc61d7d-66c0-4802-89c3-38c5a163592e"),
                HuaweiCharacteristic.TIME_SYNC to uuid("00002a2b-0000-1000-8000-00805f9b34fb"),
                HuaweiCharacteristic.SERIAL_NUMBER to uuid("11872f15-a91d-49da-ac89-5107284f3425"),
                HuaweiCharacteristic.DEVICE_CONFIGURATION to uuid("bfc36f6e-4150-4a4b-9052-3d359e52962e"),
                HuaweiCharacteristic.HARDWARE_MODEL to uuid("426f058d-8211-413e-8320-397a890a08bf"),
                HuaweiCharacteristic.OFFLINE_HISTORY to uuid("0212f42a-5f19-4bc1-ba52-d7ec7ccb71a4"),
                HuaweiCharacteristic.BIA_STREAM to uuid("46797c17-d639-488d-9476-4789e8472878"),
                HuaweiCharacteristic.CAPABILITIES_REQUEST to uuid("0000fe01-0000-1000-8000-00805f9b34fb"),
                HuaweiCharacteristic.CAPABILITIES_RESPONSE to uuid("0000fe02-0000-1000-8000-00805f9b34fb"),
                HuaweiCharacteristic.STATUS_SENTINEL to uuid("ba216311-1787-472b-bef6-3eb29e62293e")
            )
        )

        /** Standard client configuration descriptor, common to all BLE. */
        val CLIENT_CONFIG_DESCRIPTOR: UUID = uuid("00002902-0000-1000-8000-00805f9b34fb")

        private fun uuid(value: String): UUID = UUID.fromString(value)
    }
}
