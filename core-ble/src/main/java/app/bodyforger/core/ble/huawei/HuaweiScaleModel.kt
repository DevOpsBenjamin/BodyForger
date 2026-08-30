package app.bodyforger.core.ble.huawei

import app.bodyforger.core.ble.RecognisedScale
import app.bodyforger.core.model.ElectrodeCount
import app.bodyforger.core.model.ImpedanceReading
import app.bodyforger.core.model.ScaleCapability

/**
 * A recognised model of the Haige family.
 *
 * Recognition, key material and GATT map: `docs/BLE_PROTOCOL.md`.
 */
enum class HuaweiScaleModel(
    private val nameFragments: List<String>,
    val displayName: String,
    val capability: ScaleCapability?,
    val impedanceOhmDivisor: Double = HAIGE_OHM_DIVISOR,
    /**
     * Key material used for this model.
     *
     * ⚠️ Only ever read from a Scale 3 Pro. Other models inherit it as a deliberately
     * falsifiable hypothesis: a rejected handshake on such a model *is* the refutation.
     */
    val keyMaterial: HuaweiKeyMaterial = HuaweiKeyMaterial.SCALE_3_PRO,
    /** GATT map used for this model — `docs/BLE_PROTOCOL.md` §4. */
    val gattProfile: HuaweiGattProfile = HuaweiGattProfile.SCALE_3_PRO
) {
    /** `M00F` — retractable handle, eight electrodes, dual frequency. */
    HUAWEI_SCALE_3_PRO(
        nameFragments = listOf("scale 3 pro"),
        displayName = "HUAWEI Scale 3 Pro",
        capability = ScaleCapability(
            electrodeCount = ElectrodeCount.EIGHT,
            frequenciesKHz = listOf(
                ImpedanceReading.LOW_FREQUENCY_KHZ,
                ImpedanceReading.HIGH_FREQUENCY_KHZ
            )
        )
    ),

    /** `M00D` — four plate electrodes, low frequency only. */
    HUAWEI_SCALE_3(
        nameFragments = listOf("scale 3"),
        displayName = "HUAWEI Scale 3",
        capability = ScaleCapability(
            electrodeCount = ElectrodeCount.FOUR,
            frequenciesKHz = listOf(ImpedanceReading.LOW_FREQUENCY_KHZ)
        )
    ),

    /**
     * A recognised Haige device whose axes are undocumented.
     *
     * Deliberately capped at nothing: an invented ceiling would be worth less than none, and
     * the capability reveals itself on the first reading.
     */
    HAIGE_FAMILY(
        nameFragments = listOf("haigeble", "hagrid", "huawei scale", "honor scale"),
        displayName = "Balance Haige",
        capability = null
    );

    /** Ce que le pilote expose à la couche générique, sans rien laisser filtrer du modèle. */
    fun toRecognisedScale(): RecognisedScale = RecognisedScale(displayName, capability)

    private fun matches(advertisedName: String): Boolean =
        nameFragments.any { advertisedName.contains(it, ignoreCase = true) }

    companion object {
        /**
         * Haige ohm scale factor: wire counters are **tenths of an ohm**.
         *
         * `TECH.md` §6.2 warns this is not universal across the Huawei range, hence a factor
         * carried by the model rather than a decoder constant. openScale disambiguates by
         * magnitude instead, which misreads any genuine value below 400 Ω.
         */
        const val HAIGE_OHM_DIVISOR: Double = 10.0

        /**
         * Identifies a model from the advertised name.
         *
         * Declaration order decides: `Scale 3 Pro` is tested before `Scale 3`, whose label it
         * contains. Matching is by **substring** — the suffix of an advertised name is unit
         * specific and the athlete can rename the device.
         */
        fun identify(advertisedName: String?): HuaweiScaleModel? {
            if (advertisedName.isNullOrBlank()) return null
            return entries.firstOrNull { it.matches(advertisedName) }
        }

        /** Driver entry point: recognises a scale without exposing the enumeration. */
        fun recognise(advertisedName: String?): RecognisedScale? =
            identify(advertisedName)?.toRecognisedScale()
    }
}
