package app.bodyforger.core.ble.huawei

import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.core.model.ScaleUserProfile
import java.time.LocalDateTime

/**
 * Les charges utiles que la balance attend, construites octet par octet.
 *
 * Ces structures sont **positionnelles et de taille fixe** : un champ décalé d'un octet ne
 * produit pas une erreur mais une valeur absurde acceptée sans broncher. Rien ici n'est
 * inféré ni arrondi au hasard.
 */
object HuaweiPayloads {

    const val PROFILE_BYTES = 69
    private const val HUID_BYTES = 30
    private const val UID_BYTES = 32
    private const val UID_OFFSET = 30

    /** Le type de profil : mesure courante, ou acquittement d'une mesure obtenue. */
    enum class ProfileKind(val code: Int) {
        /** Configuration avant pesée. */
        ROUTINE(0),

        /** Validation envoyée une fois la trame reçue : la balance clôt alors la session. */
        MEASUREMENT_COMMIT(2)
    }

    /**
     * Le profil utilisateur, 69 octets en clair avant chiffrement.
     *
     * ```
     * 0..29   HUID, ASCII complété de zéros
     * 30..61  UID secondaire, facultatif — laissé à zéro
     * 62      sexe : 1 homme, 0 femme
     * 63      âge en années
     * 64..65  taille en cm, petit-boutiste
     * 66..67  poids × 100, petit-boutiste
     * 68      type de profil
     * ```
     *
     * Le poids transmis est le **dernier connu**, qui aide la balance à cadrer sa mesure. Il
     * est laissé à zéro lorsqu'aucun n'est disponible : mieux vaut ne rien dire qu'annoncer
     * un poids inventé, que la balance graverait dans sa calibration.
     */
    fun userProfile(
        huid: String,
        profile: ScaleUserProfile,
        kind: ProfileKind = ProfileKind.ROUTINE,
        weightKg: Double? = profile.lastWeightKg
    ): ByteArray {
        val huidBytes = huid.toByteArray(Charsets.US_ASCII)
        require(huidBytes.size <= HUID_BYTES) { "HUID de ${huidBytes.size} octets, $HUID_BYTES au plus" }

        val physiology = profile.physiology
        require(physiology.ageYears in 0..255) { "Âge hors bornes : ${physiology.ageYears}" }
        require(physiology.heightCm > 0) { "Taille invalide : ${physiology.heightCm}" }

        val payload = ByteArray(PROFILE_BYTES)
        huidBytes.copyInto(payload)
        // Les octets 30..61 restent nuls : l'UID secondaire est facultatif.
        payload[UID_OFFSET + UID_BYTES] = if (physiology.sex == BiologicalSex.MALE) 1 else 0
        payload[63] = physiology.ageYears.toByte()
        payload.putUInt16LittleEndian(64, physiology.heightCm.toInt())
        payload.putUInt16LittleEndian(66, ((weightKg ?: 0.0) * 100).toInt().coerceIn(0, 0xFFFF))
        payload[68] = kind.code.toByte()
        return payload
    }

    /**
     * L'heure courante au format standard Bluetooth *Current Time*.
     *
     * Caractéristique du SIG, donc identique sur tout matériel : dix octets, année en tête.
     * Le jour de la semaine suit la convention ISO, lundi valant 1.
     */
    fun currentTime(now: LocalDateTime): ByteArray {
        val payload = ByteArray(10)
        payload.putUInt16LittleEndian(0, now.year)
        payload[2] = now.monthValue.toByte()
        payload[3] = now.dayOfMonth.toByte()
        payload[4] = now.hour.toByte()
        payload[5] = now.minute.toByte()
        payload[6] = now.second.toByte()
        payload[7] = now.dayOfWeek.value.toByte()
        payload[8] = 0 // fraction de seconde en 256e, sans objet ici
        payload[9] = 0 // raison de l'ajustement : aucune
        return payload
    }

    /** Armement (`0x01`) ou désarmement (`0x00`) du mode association. */
    fun bindingControl(armed: Boolean): ByteArray = byteArrayOf(if (armed) 1 else 0)

    private fun ByteArray.putUInt16LittleEndian(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}
