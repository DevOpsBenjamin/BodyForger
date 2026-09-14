package app.bodyforger.mobile.mcp

import app.bodyforger.core.database.BodyForgerDatabase
import app.bodyforger.core.database.entity.impedanceRows
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.database.entity.toEntity
import app.bodyforger.mobile.mcp.McpToolHelpers.arrayProp
import app.bodyforger.mobile.mcp.McpToolHelpers.errorJson
import app.bodyforger.mobile.mcp.McpToolHelpers.intProp
import app.bodyforger.mobile.mcp.McpToolHelpers.numProp
import app.bodyforger.mobile.mcp.McpToolHelpers.stringProp
import app.bodyforger.mobile.mcp.McpToolHelpers.toolDescriptor
import org.json.JSONArray
import org.json.JSONObject

/**
 * Weigh-ins over MCP: what the scale wrote elsewhere, brought into the app.
 *
 * Only the resistances and the mass are carried. The body fat percentage travels as what the
 * scale itself announced — our own is recomputed from the resistances and never stored.
 */
class BodyForgerBodyLogToolHandler(private val database: BodyForgerDatabase?) {

    fun listToolDescriptors(): List<JSONObject> = listOf(
        toolDescriptor(
            TOOL_INSERT_BODY_LOG,
            "Insert or replace a body reading: mass, the percentage the scale announced, and the " +
                "raw resistances in ohms. Sending the same id again replaces the reading and its " +
                "resistances rather than duplicating it.",
            JSONObject().apply {
                put("id", stringProp("Stable id. Reuse it to replace a reading; omitted, one is generated"))
                put("measuredAtEpochMs", numProp("When the reading was taken (epoch ms). Identity is the instant; the calendar day is derived from it here, in the phone's zone"))
                put("massKg", numProp("Mass in kilograms"))
                put("bodyFatPercentage", numProp("The percentage the scale announced, if any. Left out when it weighed without measuring"))
                put("restingHeartRateBpm", intProp("Heart rate the scale read, if any"))
                put("sourceDeviceAddress", stringProp("BLE address of the scale, or omitted for a manual entry"))
                put("impedances", arrayProp(
                    "Raw resistances: [{path, frequencyKHz, ohms}]. path is named — " +
                        "LEFT_FOOT_TO_RIGHT_FOOT, LEFT_HAND_TO_RIGHT_HAND, LEFT_HAND_TO_LEFT_FOOT, " +
                        "LEFT_HAND_TO_RIGHT_FOOT, RIGHT_HAND_TO_LEFT_FOOT, RIGHT_HAND_TO_RIGHT_FOOT — " +
                        "never an index. A path that was not measured is left out, never sent as zero"
                ))
            },
            required = listOf("massKg", "measuredAtEpochMs")
        ),
        toolDescriptor(
            TOOL_LIST_BODY_LOGS,
            "List stored body readings, most recent first, with their raw resistances.",
            JSONObject().apply {
                put("limit", intProp("Max readings to return (default 30)"))
                put("offset", intProp("Pagination offset (default 0)"))
            }
        )
    )

    fun canHandle(name: String): Boolean = name in SUPPORTED_TOOLS

    suspend fun execute(name: String, arguments: JSONObject): JSONObject {
        val db = database ?: return errorJson("BodyForger database is not initialized.")
        return when (name) {
            TOOL_INSERT_BODY_LOG -> handleInsert(db, arguments)
            TOOL_LIST_BODY_LOGS -> handleList(db, arguments)
            else -> errorJson("Unsupported tool: $name")
        }
    }

    private suspend fun handleInsert(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val log = try {
            BodyLogMcpMapper.parseBodyLog(args)
        } catch (e: IllegalArgumentException) {
            return errorJson(e.message ?: "Invalid body reading.")
        }

        val deviceAddress = args.optString("sourceDeviceAddress").takeIf { it.isNotEmpty() }
        db.bodyLogDao().save(log.toEntity(deviceAddress), log.impedanceRows())

        return JSONObject().apply {
            put("success", true)
            put("id", log.id)
            put("dateIso", log.dateIso)
            put("massKg", log.massKg)
            // What was stored, not what was sent: a resistance the model refuses never
            // silently becomes a reading that looks complete.
            put("impedanceCount", log.rawImpedances.ohmsByReading.size)
            put("fidelity", log.fidelity.exercisedElectrodeCount.name)
        }
    }

    private suspend fun handleList(db: BodyForgerDatabase, args: JSONObject): JSONObject {
        val limit = args.optInt("limit", 30).coerceIn(1, 100)
        val offset = args.optInt("offset", 0).coerceAtLeast(0)
        val stored = db.bodyLogDao().getLogsPaged(limit, offset)

        return JSONObject().apply {
            put("count", stored.size)
            put("bodyLogs", JSONArray().apply {
                stored.forEach { put(BodyLogMcpMapper.toBodyLogJson(it.toDomain(), it.log.sourceDeviceAddress)) }
            })
        }
    }

    companion object {
        const val TOOL_INSERT_BODY_LOG = "bodyforger_insert_body_log"
        const val TOOL_LIST_BODY_LOGS = "bodyforger_list_body_logs"

        private val SUPPORTED_TOOLS = setOf(TOOL_INSERT_BODY_LOG, TOOL_LIST_BODY_LOGS)
    }
}
