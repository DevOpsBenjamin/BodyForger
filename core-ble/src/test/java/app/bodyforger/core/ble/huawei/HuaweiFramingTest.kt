package app.bodyforger.core.ble.huawei

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HuaweiFramingTest {

    // --- La table du CRC : figée dans le code, mais vérifiée ici ---

    @Test
    fun `la table figee est exactement celle que le polynome engendre`() {
        // C'est ce test qui autorise à figer la table : une coquille de transcription — comme
        // celle que porte openScale — ne pourrait pas y survivre.
        val regenerated = IntArray(256) { index ->
            var value = index shl 8
            repeat(8) {
                value = if (value and 0x8000 != 0) {
                    ((value shl 1) xor HuaweiFraming.POLYNOMIAL) and 0xFFFF
                } else {
                    (value shl 1) and 0xFFFF
                }
            }
            value
        }
        assertArrayEquals(regenerated, HuaweiFraming.CRC_TABLE)
    }

    @Test
    fun `les premieres entrees correspondent a la table publiee`() {
        // Recopiées depuis `TECH.md` §3.2, en décimal comme le document les donne.
        val published = intArrayOf(
            0, 4129, 8258, 12387, 16516, 20645, 24774, 28903,
            33032, 37161, 41290, 45419, 49548, 53677, 57806, 61935,
            4657, 528, 12915, 8786
        )
        assertArrayEquals(published, HuaweiFraming.CRC_TABLE.copyOf(published.size))
    }

    @Test
    fun `un octet different change le CRC`() {
        assertEquals(0x2621, HuaweiFraming.crc16(bytes("db0300c1")))
        assertTrue(HuaweiFraming.crc16(bytes("db0300c1")) != HuaweiFraming.crc16(bytes("db0300c0")))
    }

    @Test
    fun `les trames recues de la balance suivent un autre CRC que celles qu'on emet`() {
        // Constat de terrain sur deux trames d'authentification réellement capturées : la
        // balance signe en MODBUS, quand nous signons en CCITT. `TECH.md` ne documente que
        // le second, et la référence ne vérifie jamais ce qu'elle reçoit — elle n'avait donc
        // aucune occasion de s'en apercevoir.
        val first = bytes("bd1210a9ccb66a6d7386d9d992f0bd9cacb4")
        assertEquals(0x9ed3, HuaweiFraming.receivedCrc16(first))
        assertTrue(HuaweiFraming.crc16(first) != 0x9ed3)

        val second = bytes("bd0511ec01")
        assertEquals(0xd295, HuaweiFraming.receivedCrc16(second))
    }

    @Test
    fun `une trame reelle de la balance se recolle`() {
        val reassembler = HuaweiFrameReassembler()
        assertNull(reassembler.feed(bytes("bd1210a9ccb66a6d7386d9d992f0bd9cacb4d39e")))
        val payload = reassembler.feed(bytes("bd0511ec0195d2"))
        // Quinze octets puis deux : l'aléa de seize octets, précédé de son octet d'entête.
        assertEquals(17, payload!!.size)
        assertNull(reassembler.lastRejection)
    }

    // --- Découpage ---

    @Test
    fun `une charge vide produit une trame, pas zero`() {
        // Certaines commandes n'ont pas de corps et doivent quand même partir.
        val frames = HuaweiFraming.split(ByteArray(0), HuaweiFrameMagic.HOST_CLEAR)
        assertEquals(1, frames.size)
        assertEquals("db030036c0", frames[0].toHex())
    }

    @Test
    fun `une charge de vingt octets tient en deux trames sequencees`() {
        val frames = HuaweiFraming.split(ByteArray(20) { it.toByte() }, HuaweiFrameMagic.HOST_ENCRYPTED)
        assertEquals(2, frames.size)
        assertEquals("dc1210000102030405060708090a0b0c0d0eea68", frames[0].toHex())
        assertEquals("dc08110f1011121395a6", frames[1].toHex())
    }

    @Test
    fun `l'octet de sequence porte le total en haut et l'index en bas`() {
        val frames = HuaweiFraming.split(ByteArray(69) { ((it * 7) % 256).toByte() }, HuaweiFrameMagic.HOST_ENCRYPTED)
        assertEquals(5, frames.size)
        frames.forEachIndexed { index, frame ->
            val sequence = frame[2].toInt() and 0xFF
            assertEquals("index de la trame $index", index, sequence and 0x0F)
            assertEquals("total annoncé", 4, (sequence shr 4) and 0x0F)
        }
    }

    @Test
    fun `la longueur annoncee vaut la charge plus trois`() {
        for (size in listOf(0, 1, 14, 15, 16, 69)) {
            HuaweiFraming.split(ByteArray(size), HuaweiFrameMagic.HOST_CLEAR).forEach { frame ->
                assertEquals((frame[1].toInt() and 0xFF) - 3, frame.size - 5)
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `une charge que le sequencement ne peut pas porter est refusee`() {
        // Quatre bits d'index : au-delà de seize trames, l'index reboucle silencieusement.
        HuaweiFraming.split(ByteArray(HuaweiFraming.MAX_PAYLOAD_BYTES + 1), HuaweiFrameMagic.HOST_CLEAR)
    }

    // --- Recollage ---

    @Test
    fun `ce qui est decoupe se recolle a l'identique`() {
        for (size in listOf(0, 1, 15, 16, 69, HuaweiFraming.MAX_PAYLOAD_BYTES)) {
            val payload = ByteArray(size) { ((it * 31) % 256).toByte() }
            val reassembler = HuaweiFrameReassembler()
            val frames = asScale(payload, HuaweiFrameMagic.SCALE_ENCRYPTED)
            var result: ByteArray? = null
            frames.forEach { result = reassembler.feed(it) }
            assertArrayEquals("charge de $size octets", payload, result)
        }
    }

    @Test
    fun `la charge n'apparait qu'a la derniere trame`() {
        val frames = asScale(ByteArray(40), HuaweiFrameMagic.SCALE_ENCRYPTED)
        val reassembler = HuaweiFrameReassembler()
        assertNull(reassembler.feed(frames[0]))
        assertNull(reassembler.feed(frames[1]))
        assertNotNull(reassembler.feed(frames[2]))
    }

    @Test
    fun `une trame au CRC faux est ecartee`() {
        // La référence ne vérifie pas le CRC : une trame corrompue y était recollée telle
        // quelle, puis déchiffrée en bruit sans que rien n'indique la corruption.
        val frame = asScale(ByteArray(4), HuaweiFrameMagic.SCALE_CLEAR)[0]
        val corrupted = frame.copyOf().also { it[it.size - 1] = (it[it.size - 1] + 1).toByte() }
        assertNull(HuaweiFrameReassembler().feed(corrupted))
    }

    @Test
    fun `une charge corrompue en cours de route n'est jamais rendue`() {
        val payload = ByteArray(40) { it.toByte() }
        val frames = asScale(payload, HuaweiFrameMagic.SCALE_ENCRYPTED)
        val reassembler = HuaweiFrameReassembler()
        reassembler.feed(frames[0])
        reassembler.feed(frames[1].copyOf().also { it[5] = (it[5] + 1).toByte() })
        assertNull("mieux vaut perdre la charge que d'en assembler une fausse", reassembler.feed(frames[2]))
    }

    @Test
    fun `un octet magique inconnu est ecarte`() {
        val frame = asScale(ByteArray(4), HuaweiFrameMagic.SCALE_CLEAR)[0]
        assertNull(HuaweiFrameReassembler().feed(frame.copyOf().also { it[0] = 0x42 }))
    }

    @Test
    fun `une trame trop courte est ecartee`() {
        val reassembler = HuaweiFrameReassembler()
        assertNull(reassembler.feed(ByteArray(0)))
        assertNull(reassembler.feed(ByteArray(4)))
    }

    @Test
    fun `une trame orpheline ne demarre pas un recollage`() {
        // Reprendre au milieu d'un message reviendrait à inventer les trames manquantes.
        val frames = asScale(ByteArray(40), HuaweiFrameMagic.SCALE_ENCRYPTED)
        assertNull(HuaweiFrameReassembler().feed(frames[1]))
        assertNull(HuaweiFrameReassembler().feed(frames[2]))
    }

    @Test
    fun `une nouvelle premiere trame abandonne le recollage en cours`() {
        val payload = ByteArray(20) { it.toByte() }
        val frames = asScale(payload, HuaweiFrameMagic.SCALE_ENCRYPTED)
        val reassembler = HuaweiFrameReassembler()
        reassembler.feed(frames[0])
        // La balance recommence son envoi : on repart de sa nouvelle première trame.
        reassembler.feed(frames[0])
        assertArrayEquals(payload, reassembler.feed(frames[1]))
    }

    @Test
    fun `l'origine de la trame se lit dans l'octet magique`() {
        assertEquals(HuaweiFrameMagic.SCALE_ENCRYPTED, HuaweiFrameMagic.of(0xCD))
        assertTrue(HuaweiFrameMagic.SCALE_ENCRYPTED.fromScale)
        assertTrue(HuaweiFrameMagic.SCALE_ENCRYPTED.encrypted)
        assertTrue(!HuaweiFrameMagic.HOST_CLEAR.fromScale && !HuaweiFrameMagic.HOST_CLEAR.encrypted)
        assertNull(HuaweiFrameMagic.of(0x00))
    }

    /** Reconstitue une trame **comme la balance l'émet** : même structure, autre signature. */
    private fun asScale(payload: ByteArray, magic: HuaweiFrameMagic) =
        HuaweiFraming.split(payload, magic, HuaweiFraming::receivedCrc16)

    private fun bytes(hex: String) = ByteArray(hex.length / 2) {
        hex.substring(it * 2, it * 2 + 2).toInt(16).toByte()
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}
