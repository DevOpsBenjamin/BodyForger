package app.bodyforger.mobile.mcp

data class McpPairedDevice(
    val id: String,
    val name: String,
    val token: String,
    val createdAtEpochMs: Long
)
