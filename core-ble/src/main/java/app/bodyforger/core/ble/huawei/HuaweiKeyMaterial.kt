package app.bodyforger.core.ble.huawei

/**
 * A model's cryptographic key material.
 *
 * Only ever read from a Scale 3 Pro; other models inherit it as a falsifiable hypothesis —
 * `docs/BLE_PROTOCOL.md` §2.
 */
data class HuaweiKeyMaterial(
    /** Authentication secret, salted to derive the tokens. */
    val authenticationSecret: ByteArray,
    /** First whitebox table. */
    val whiteboxFirst: ByteArray,
    /** Second whitebox table. */
    val whiteboxSecond: ByteArray
) {
    init {
        require(authenticationSecret.size == 16) { "Authentication secret of invalid size" }
        require(whiteboxFirst.size == 16) { "First whitebox table of invalid size" }
        require(whiteboxSecond.size == 16) { "Second whitebox table of invalid size" }
    }

    override fun equals(other: Any?): Boolean = this === other || (other is HuaweiKeyMaterial &&
        authenticationSecret.contentEquals(other.authenticationSecret) &&
        whiteboxFirst.contentEquals(other.whiteboxFirst) &&
        whiteboxSecond.contentEquals(other.whiteboxSecond))

    override fun hashCode(): Int = authenticationSecret.contentHashCode()

    companion object {
        /** Read from a Scale 3 Pro, and the default for every other model. */
        val SCALE_3_PRO = HuaweiKeyMaterial(
            authenticationSecret = "90B96ECA297EF78717E66E491084D3F8".hexToBytes(),
            whiteboxFirst = "CA4946D061C9FE534F6044F930EBB69B".hexToBytes(),
            whiteboxSecond = "FBCE6E2B4BAF80ED969BA26B4A4B9325".hexToBytes()
        )

        private fun String.hexToBytes(): ByteArray =
            ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }
}
