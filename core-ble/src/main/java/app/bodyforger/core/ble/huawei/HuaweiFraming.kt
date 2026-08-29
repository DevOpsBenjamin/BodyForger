package app.bodyforger.core.ble.huawei

/**
 * D'où vient une trame, et si son contenu est chiffré. L'octet magique porte les deux
 * informations à la fois.
 */
enum class HuaweiFrameMagic(val byte: Int, val fromScale: Boolean, val encrypted: Boolean) {
    HOST_CLEAR(0xDB, fromScale = false, encrypted = false),
    HOST_ENCRYPTED(0xDC, fromScale = false, encrypted = true),
    SCALE_CLEAR(0xBD, fromScale = true, encrypted = false),
    SCALE_ENCRYPTED(0xCD, fromScale = true, encrypted = true);

    companion object {
        fun of(byte: Int): HuaweiFrameMagic? = entries.firstOrNull { it.byte == (byte and 0xFF) }
    }
}

/**
 * La couche de trame propriétaire posée par-dessus le MTU du BLE.
 *
 * ```
 * +---------+------------+--------------+------------------+---------------+
 * | magique | longueur   | séquence     | charge (0..15 o) | CRC-16 (LE)   |
 * +---------+------------+--------------+------------------+---------------+
 * ```
 *
 * La longueur annoncée compte la charge **plus trois** ; l'octet de séquence loge le nombre
 * total de trames dans son quartet haut et l'index courant dans le quartet bas. Quatre bits
 * pour chacun : une charge ne peut donc pas dépasser seize trames de quinze octets.
 */
object HuaweiFraming {

    const val PAYLOAD_BYTES_PER_FRAME = 15
    const val MAX_FRAMES = 16
    const val MAX_PAYLOAD_BYTES = PAYLOAD_BYTES_PER_FRAME * MAX_FRAMES
    private const val HEADER_BYTES = 3
    private const val CRC_BYTES = 2
    private const val MIN_FRAME_BYTES = HEADER_BYTES + CRC_BYTES

    /** Le polynôme du CRC-16 CCITT, dont la table ci-dessous découle entièrement. */
    const val POLYNOMIAL = 0x1021

    /**
     * La table du CRC-16 CCITT, figée ici pour n'être calculée qu'une fois — au moment
     * d'écrire ce fichier, jamais à l'exécution.
     *
     * Elle n'a pas été recopiée à la main : elle a été **générée** depuis [POLYNOMIAL], et un
     * test la régénère pour vérifier qu'elle n'a pas dérivé. Une table transcrite est une
     * source d'erreur silencieuse — openScale en porte une entachée d'une coquille — et ce
     * test rend la coquille impossible.
     */
    val CRC_TABLE: IntArray = intArrayOf(
        0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50A5, 0x60C6, 0x70E7,
        0x8108, 0x9129, 0xA14A, 0xB16B, 0xC18C, 0xD1AD, 0xE1CE, 0xF1EF,
        0x1231, 0x0210, 0x3273, 0x2252, 0x52B5, 0x4294, 0x72F7, 0x62D6,
        0x9339, 0x8318, 0xB37B, 0xA35A, 0xD3BD, 0xC39C, 0xF3FF, 0xE3DE,
        0x2462, 0x3443, 0x0420, 0x1401, 0x64E6, 0x74C7, 0x44A4, 0x5485,
        0xA56A, 0xB54B, 0x8528, 0x9509, 0xE5EE, 0xF5CF, 0xC5AC, 0xD58D,
        0x3653, 0x2672, 0x1611, 0x0630, 0x76D7, 0x66F6, 0x5695, 0x46B4,
        0xB75B, 0xA77A, 0x9719, 0x8738, 0xF7DF, 0xE7FE, 0xD79D, 0xC7BC,
        0x48C4, 0x58E5, 0x6886, 0x78A7, 0x0840, 0x1861, 0x2802, 0x3823,
        0xC9CC, 0xD9ED, 0xE98E, 0xF9AF, 0x8948, 0x9969, 0xA90A, 0xB92B,
        0x5AF5, 0x4AD4, 0x7AB7, 0x6A96, 0x1A71, 0x0A50, 0x3A33, 0x2A12,
        0xDBFD, 0xCBDC, 0xFBBF, 0xEB9E, 0x9B79, 0x8B58, 0xBB3B, 0xAB1A,
        0x6CA6, 0x7C87, 0x4CE4, 0x5CC5, 0x2C22, 0x3C03, 0x0C60, 0x1C41,
        0xEDAE, 0xFD8F, 0xCDEC, 0xDDCD, 0xAD2A, 0xBD0B, 0x8D68, 0x9D49,
        0x7E97, 0x6EB6, 0x5ED5, 0x4EF4, 0x3E13, 0x2E32, 0x1E51, 0x0E70,
        0xFF9F, 0xEFBE, 0xDFDD, 0xCFFC, 0xBF1B, 0xAF3A, 0x9F59, 0x8F78,
        0x9188, 0x81A9, 0xB1CA, 0xA1EB, 0xD10C, 0xC12D, 0xF14E, 0xE16F,
        0x1080, 0x00A1, 0x30C2, 0x20E3, 0x5004, 0x4025, 0x7046, 0x6067,
        0x83B9, 0x9398, 0xA3FB, 0xB3DA, 0xC33D, 0xD31C, 0xE37F, 0xF35E,
        0x02B1, 0x1290, 0x22F3, 0x32D2, 0x4235, 0x5214, 0x6277, 0x7256,
        0xB5EA, 0xA5CB, 0x95A8, 0x8589, 0xF56E, 0xE54F, 0xD52C, 0xC50D,
        0x34E2, 0x24C3, 0x14A0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
        0xA7DB, 0xB7FA, 0x8799, 0x97B8, 0xE75F, 0xF77E, 0xC71D, 0xD73C,
        0x26D3, 0x36F2, 0x0691, 0x16B0, 0x6657, 0x7676, 0x4615, 0x5634,
        0xD94C, 0xC96D, 0xF90E, 0xE92F, 0x99C8, 0x89E9, 0xB98A, 0xA9AB,
        0x5844, 0x4865, 0x7806, 0x6827, 0x18C0, 0x08E1, 0x3882, 0x28A3,
        0xCB7D, 0xDB5C, 0xEB3F, 0xFB1E, 0x8BF9, 0x9BD8, 0xABBB, 0xBB9A,
        0x4A75, 0x5A54, 0x6A37, 0x7A16, 0x0AF1, 0x1AD0, 0x2AB3, 0x3A92,
        0xFD2E, 0xED0F, 0xDD6C, 0xCD4D, 0xBDAA, 0xAD8B, 0x9DE8, 0x8DC9,
        0x7C26, 0x6C07, 0x5C64, 0x4C45, 0x3CA2, 0x2C83, 0x1CE0, 0x0CC1,
        0xEF1F, 0xFF3E, 0xCF5D, 0xDF7C, 0xAF9B, 0xBFBA, 0x8FD9, 0x9FF8,
        0x6E17, 0x7E36, 0x4E55, 0x5E74, 0x2E93, 0x3EB2, 0x0ED1, 0x1EF0
    )

    /**
     * Le CRC des trames que **nous émettons** : CCITT, registre initialisé à zéro.
     *
     * C'est celui de la table ci-dessus, employé par l'implémentation de référence éprouvée
     * en production. Les trames construites ainsi sont acceptées par le matériel.
     */
    fun crc16(data: ByteArray, length: Int = data.size): Int {
        var register = 0
        for (i in 0 until length) {
            val byte = data[i].toInt() and 0xFF
            register = ((register shl 8) and 0xFFFF) xor CRC_TABLE[(byte xor (register shr 8)) and 0xFF]
        }
        return register and 0xFFFF
    }

    /**
     * Le CRC des trames que **la balance émet** : MODBUS — polynôme `0x8005`, registre
     * initialisé à `0xFFFF`, réfléchi en entrée comme en sortie.
     *
     * ⚠️ **Les deux sens n'emploient pas le même CRC**, et c'est un constat de terrain, pas
     * une théorie : deux trames d'authentification réellement capturées le donnent sans
     * ambiguïté, là où le CCITT sortant se trompe de plusieurs milliers. `TECH.md` §3.2 ne
     * documente que le premier, et l'implémentation de référence ne vérifie jamais les trames
     * reçues — elle n'avait donc aucune occasion de s'en apercevoir.
     */
    fun receivedCrc16(data: ByteArray, length: Int = data.size): Int {
        var register = 0xFFFF
        for (i in 0 until length) {
            register = register xor (data[i].toInt() and 0xFF)
            repeat(8) {
                register = if (register and 1 != 0) {
                    (register shr 1) xor MODBUS_REFLECTED_POLYNOMIAL
                } else {
                    register shr 1
                }
            }
        }
        return register and 0xFFFF
    }

    /** Polynôme MODBUS `0x8005`, sous sa forme réfléchie. */
    private const val MODBUS_REFLECTED_POLYNOMIAL = 0xA001

    /**
     * Découpe une charge utile en trames prêtes à écrire sur la caractéristique.
     *
     * Une charge vide produit **une** trame et non zéro : certaines commandes n'ont pas de
     * corps et doivent quand même être émises.
     */
    fun split(
        payload: ByteArray,
        magic: HuaweiFrameMagic,
        /**
         * La signature à apposer. Les deux sens n'emploient pas le même CRC : ce paramètre
         * existe pour pouvoir reconstituer une trame **telle que la balance l'émet**, ce dont
         * les tests ont besoin et que le code de production n'a jamais à faire.
         */
        crc: (ByteArray, Int) -> Int = ::crc16
    ): List<ByteArray> {
        require(payload.size <= MAX_PAYLOAD_BYTES) {
            "Charge de ${payload.size} octets : le séquencement sur quatre bits en admet $MAX_PAYLOAD_BYTES au plus"
        }
        val total = maxOf(1, (payload.size + PAYLOAD_BYTES_PER_FRAME - 1) / PAYLOAD_BYTES_PER_FRAME)
        return List(total) { index ->
            val from = index * PAYLOAD_BYTES_PER_FRAME
            val chunk = payload.copyOfRange(from, minOf(from + PAYLOAD_BYTES_PER_FRAME, payload.size))
            val frame = ByteArray(HEADER_BYTES + chunk.size + CRC_BYTES)
            frame[0] = magic.byte.toByte()
            frame[1] = (chunk.size + HEADER_BYTES).toByte()
            frame[2] = ((((total - 1) and 0x0F) shl 4) or (index and 0x0F)).toByte()
            chunk.copyInto(frame, HEADER_BYTES)
            val signature = crc(frame, HEADER_BYTES + chunk.size)
            frame[frame.size - 2] = (signature and 0xFF).toByte()
            frame[frame.size - 1] = ((signature shr 8) and 0xFF).toByte()
            frame
        }
    }
}

/**
 * Recolle les trames reçues jusqu'à reconstituer la charge utile.
 *
 * ⚠️ **Le CRC est vérifié ici, contrairement à l'implémentation de référence** qui lit la
 * longueur et la séquence sans jamais contrôler l'intégrité. Une trame corrompue y était
 * recollée telle quelle, puis déchiffrée en bruit — d'où une charge absurde sans que rien
 * n'indique la corruption.
 *
 * L'instance n'est pas sûre vis-à-vis des accès concurrents : une par connexion.
 */
class HuaweiFrameReassembler {

    private val chunks = mutableListOf<ByteArray>()
    private var expectedFrames = 0

    /**
     * Pourquoi la dernière trame a été écartée, ou `null` si elle a été acceptée.
     *
     * Une trame incomplète et une trame rejetée rendent toutes deux `null` : sans cette
     * distinction, un défaut de recollage est indiscernable d'un message qui arrive en
     * plusieurs morceaux.
     */
    var lastRejection: String? = null
        private set

    /** L'origine des trames en cours de recollage, tant qu'une charge est incomplète. */
    var magic: HuaweiFrameMagic? = null
        private set

    /**
     * Injecte une trame reçue et rend la charge complète, ou `null` tant qu'il en manque.
     *
     * Une trame malformée, au CRC faux ou hors séquence est **écartée** et remet le
     * recollage à zéro : mieux vaut perdre une charge et la voir retransmise que d'en
     * assembler une fausse.
     */
    fun feed(raw: ByteArray): ByteArray? {
        lastRejection = null
        if (raw.size < 5) return discard("trame de ${raw.size} octets, trop courte")
        val frameMagic = HuaweiFrameMagic.of(raw[0].toInt())
            ?: return discard("octet magique inconnu : 0x%02x".format(raw[0].toInt() and 0xFF))

        val declared = (raw[1].toInt() and 0xFF) - 3
        if (declared < 0 || 3 + declared + 2 > raw.size) {
            return discard("longueur annoncée ${raw[1].toInt() and 0xFF} incompatible avec ${raw.size} octets")
        }

        val expectedCrc = HuaweiFraming.receivedCrc16(raw, 3 + declared)
        val actualCrc = (raw[3 + declared].toInt() and 0xFF) or
            ((raw[4 + declared].toInt() and 0xFF) shl 8)
        if (expectedCrc != actualCrc) {
            return discard("CRC attendu 0x%04x, reçu 0x%04x".format(expectedCrc, actualCrc))
        }

        val sequence = raw[2].toInt() and 0xFF
        val index = sequence and 0x0F
        val total = ((sequence shr 4) and 0x0F) + 1

        if (index == 0) {
            chunks.clear()
            expectedFrames = total
            magic = frameMagic
        } else if (index != chunks.size || total != expectedFrames || frameMagic != magic) {
            // Trame orpheline, ou d'un autre message : on ne devine pas ce qui manque.
            return discard("trame $index/$total hors séquence (${chunks.size} déjà reçues)")
        }

        chunks += raw.copyOfRange(3, 3 + declared)

        if (index + 1 < total) return null

        val payload = ByteArray(chunks.sumOf { it.size })
        var offset = 0
        for (chunk in chunks) {
            chunk.copyInto(payload, offset)
            offset += chunk.size
        }
        reset()
        return payload
    }

    /** Abandonne le recollage en cours — à appeler sur déconnexion. */
    fun reset() {
        chunks.clear()
        expectedFrames = 0
        magic = null
    }

    private fun discard(reason: String): ByteArray? {
        reset()
        lastRejection = reason
        return null
    }
}
