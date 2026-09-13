package app.bodyforger.core.ble.huawei

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HuaweiFramingTest {

    @Test
    fun `the frozen table is exactly the one the polynomial generates`() {
        // The frozen table is regenerated here from the polynomial alone: a transcription
        // error — the kind openScale's own copy carries — could not survive this.
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
    fun `the first entries match the published table`() {
        val published = intArrayOf(
            0, 4129, 8258, 12387, 16516, 20645, 24774, 28903,
            33032, 37161, 41290, 45419, 49548, 53677, 57806, 61935,
            4657, 528, 12915, 8786
        )
        assertArrayEquals(published, HuaweiFraming.CRC_TABLE.copyOf(published.size))
    }

    @Test
    fun `a different byte changes the CRC`() {
        assertEquals(0x2621, HuaweiFraming.crc16(bytes("db0300c1")))
        assertTrue(HuaweiFraming.crc16(bytes("db0300c1")) != HuaweiFraming.crc16(bytes("db0300c0")))
    }

    @Test
    fun `frames received from the scale follow a different CRC than the ones we send`() {
        // The scale signs with a MODBUS CRC where we sign with CCITT. A locally built frame
        // round-trips through our own CRC, so nothing here would ever reveal the difference —
        // it only shows against a captured frame, which is what this one is.
        val first = bytes("bd1210a9ccb66a6d7386d9d992f0bd9cacb4")
        assertEquals(0x9ed3, HuaweiFraming.receivedCrc16(first))
        assertTrue(HuaweiFraming.crc16(first) != 0x9ed3)

        val second = bytes("bd0511ec01")
        assertEquals(0xd295, HuaweiFraming.receivedCrc16(second))
    }

    @Test
    fun `a real frame from the scale reassembles`() {
        val reassembler = HuaweiFrameReassembler()
        assertNull(reassembler.feed(bytes("bd1210a9ccb66a6d7386d9d992f0bd9cacb4d39e")))
        val payload = reassembler.feed(bytes("bd0511ec0195d2"))
        assertEquals(17, payload!!.size)
        assertNull(reassembler.lastRejection)
    }

    @Test
    fun `an empty payload produces one frame, not zero`() {
        val frames = HuaweiFraming.split(ByteArray(0), HuaweiFrameMagic.HOST_CLEAR)
        assertEquals(1, frames.size)
        assertEquals("db030036c0", frames[0].toHex())
    }

    @Test
    fun `a twenty-byte payload fits in two sequenced frames`() {
        val frames = HuaweiFraming.split(ByteArray(20) { it.toByte() }, HuaweiFrameMagic.HOST_ENCRYPTED)
        assertEquals(2, frames.size)
        assertEquals("dc1210000102030405060708090a0b0c0d0eea68", frames[0].toHex())
        assertEquals("dc08110f1011121395a6", frames[1].toHex())
    }

    @Test
    fun `the sequence byte carries the total in the high nibble and the index in the low one`() {
        val frames = HuaweiFraming.split(ByteArray(69) { ((it * 7) % 256).toByte() }, HuaweiFrameMagic.HOST_ENCRYPTED)
        assertEquals(5, frames.size)
        frames.forEachIndexed { index, frame ->
            val sequence = frame[2].toInt() and 0xFF
            assertEquals("index de la trame $index", index, sequence and 0x0F)
            assertEquals("announced total", 4, (sequence shr 4) and 0x0F)
        }
    }

    @Test
    fun `the announced length is the payload plus three`() {
        for (size in listOf(0, 1, 14, 15, 16, 69)) {
            HuaweiFraming.split(ByteArray(size), HuaweiFrameMagic.HOST_CLEAR).forEach { frame ->
                assertEquals((frame[1].toInt() and 0xFF) - 3, frame.size - 5)
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a payload the sequencing cannot carry is refused`() {
        HuaweiFraming.split(ByteArray(HuaweiFraming.MAX_PAYLOAD_BYTES + 1), HuaweiFrameMagic.HOST_CLEAR)
    }

    // --- Recollage ---

    @Test
    fun `what is split reassembles identically`() {
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
    fun `the payload only appears on the last frame`() {
        val frames = asScale(ByteArray(40), HuaweiFrameMagic.SCALE_ENCRYPTED)
        val reassembler = HuaweiFrameReassembler()
        assertNull(reassembler.feed(frames[0]))
        assertNull(reassembler.feed(frames[1]))
        assertNotNull(reassembler.feed(frames[2]))
    }

    @Test
    fun `a frame with a wrong CRC is discarded`() {
        val frame = asScale(ByteArray(4), HuaweiFrameMagic.SCALE_CLEAR)[0]
        val corrupted = frame.copyOf().also { it[it.size - 1] = (it[it.size - 1] + 1).toByte() }
        assertNull(HuaweiFrameReassembler().feed(corrupted))
    }

    @Test
    fun `a payload corrupted along the way is never returned`() {
        val payload = ByteArray(40) { it.toByte() }
        val frames = asScale(payload, HuaweiFrameMagic.SCALE_ENCRYPTED)
        val reassembler = HuaweiFrameReassembler()
        reassembler.feed(frames[0])
        reassembler.feed(frames[1].copyOf().also { it[5] = (it[5] + 1).toByte() })
        assertNull("mieux vaut perdre la charge que d'en assembler une fausse", reassembler.feed(frames[2]))
    }

    @Test
    fun `an unknown magic byte is discarded`() {
        val frame = asScale(ByteArray(4), HuaweiFrameMagic.SCALE_CLEAR)[0]
        assertNull(HuaweiFrameReassembler().feed(frame.copyOf().also { it[0] = 0x42 }))
    }

    @Test
    fun `a frame that is too short is discarded`() {
        val reassembler = HuaweiFrameReassembler()
        assertNull(reassembler.feed(ByteArray(0)))
        assertNull(reassembler.feed(ByteArray(4)))
    }

    @Test
    fun `an orphan frame does not start a reassembly`() {
        val frames = asScale(ByteArray(40), HuaweiFrameMagic.SCALE_ENCRYPTED)
        assertNull(HuaweiFrameReassembler().feed(frames[1]))
        assertNull(HuaweiFrameReassembler().feed(frames[2]))
    }

    @Test
    fun `a new first frame abandons the reassembly in progress`() {
        val payload = ByteArray(20) { it.toByte() }
        val frames = asScale(payload, HuaweiFrameMagic.SCALE_ENCRYPTED)
        val reassembler = HuaweiFrameReassembler()
        reassembler.feed(frames[0])
        reassembler.feed(frames[0])
        assertArrayEquals(payload, reassembler.feed(frames[1]))
    }

    @Test
    fun `the frame's origin is read from the magic byte`() {
        assertEquals(HuaweiFrameMagic.SCALE_ENCRYPTED, HuaweiFrameMagic.of(0xCD))
        assertTrue(HuaweiFrameMagic.SCALE_ENCRYPTED.fromScale)
        assertTrue(HuaweiFrameMagic.SCALE_ENCRYPTED.encrypted)
        assertTrue(!HuaweiFrameMagic.HOST_CLEAR.fromScale && !HuaweiFrameMagic.HOST_CLEAR.encrypted)
        assertNull(HuaweiFrameMagic.of(0x00))
    }

    /** Rebuilds a frame **the way the scale emits it**: same structure, other signature. */
    private fun asScale(payload: ByteArray, magic: HuaweiFrameMagic) =
        HuaweiFraming.split(payload, magic, HuaweiFraming::receivedCrc16)

    private fun bytes(hex: String) = ByteArray(hex.length / 2) {
        hex.substring(it * 2, it * 2 + 2).toInt(16).toByte()
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}
