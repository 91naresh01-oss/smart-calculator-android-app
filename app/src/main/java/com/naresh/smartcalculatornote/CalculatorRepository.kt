package com.naresh.smartcalculatornote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore("smart_calculator_state")

class CalculatorRepository(private val context: Context) {
    private val stateV1Key = stringPreferencesKey("state_v1")
    private val stateV2Key = stringPreferencesKey("state_v2")
    private val stateV3Key = stringPreferencesKey("state_v3")
    /** Reads V2 first and falls back to the prior V1 payload without discarding user data. */
    val state: Flow<AppState> = context.dataStore.data.map { preferences ->
        preferences[stateV3Key]?.let(::decode) ?: preferences[stateV2Key]?.let(::decode) ?: preferences[stateV1Key]?.let(::decode) ?: AppState()
    }
    suspend fun save(state: AppState) { context.dataStore.edit { it[stateV3Key] = encode(state) } }
    suspend fun loadOnce(): AppState = state.first()

    private fun encode(state: AppState) = JSONObject().apply {
        put("version", 3); put("activeTab", state.activeTab.name); put("theme", state.theme.name)
        put("fontScale", state.fontScale.toDouble()); put("calSection", state.calSection.name)
        put("fourValueMode", state.fourValueMode.key)
        put("selectedMoreTool", state.selectedMoreTool)
        put("rows", JSONArray().apply { state.rows.forEach { row -> put(JSONObject().put("id", row.id).put("label", row.label).put("amount", row.amount).put("operator", row.operator.name)) } })
        put("cash", JSONObject().apply { state.cash.forEach { (key, value) -> put(key.toString(), value) } })
        put("toolInputs", JSONObject(state.toolInputs))
        put("history", encodeHistory(state.history)); put("originalHistory", encodeHistory(state.originalHistory))
        put("notes", encodeNotes(state.notes))
    }.toString()

    private fun encodeHistory(entries: List<HistoryEntry>) = JSONArray().apply { entries.take(100).forEach { entry -> put(JSONObject().put("id", entry.id).put("label", entry.label).put("value", entry.value).put("createdAt", entry.createdAt)) } }

    private fun encodeNotes(notes: List<SmartNote>) = JSONArray().apply {
        notes.take(500).forEach { note ->
            put(JSONObject().apply {
                put("id", note.id); put("title", note.title); put("details", note.details)
                put("createdAt", note.createdAt); put("updatedAt", note.updatedAt); put("completed", note.completed)
                put("reminderAt", note.reminderAt ?: JSONObject.NULL); put("repeat", note.repeat.name)
            })
        }
    }

    private fun decode(raw: String): AppState? = try {
        val json = JSONObject(raw)
        val rows = json.optJSONArray("rows")?.let { array ->
            (0 until array.length()).mapNotNull { index ->
                runCatching {
                    array.getJSONObject(index).let {
                        CalcRow(
                            it.getString("id"),
                            it.optString("label"),
                            it.optString("amount").replace(",", ""),
                            runCatching { enumValueOf<Operator>(it.optString("operator", "ADD")) }.getOrDefault(Operator.ADD)
                        )
                    }
                }.getOrNull()
            }
        }.orEmpty()
        val cashJson = json.optJSONObject("cash") ?: JSONObject(); val cash = listOf(500, 200, 100, 50, 20, 10).associateWith { cashJson.optString(it.toString()) }
        val toolsJson = json.optJSONObject("toolInputs") ?: JSONObject(); val tools = toolsJson.keys().asSequence().associateWith {
            val value = toolsJson.optString(it)
            if (value.replace(",", "").matches(Regex("-?\\d*(\\.\\d*)?"))) value.replace(",", "") else value
        }
        AppState(
            activeTab = runCatching { enumValueOf<MainTab>(json.optString("activeTab")) }.getOrDefault(MainTab.CAL),
            rows = rows.ifEmpty { AppState().rows }, cash = cash, toolInputs = tools,
            fourValueMode = FourValueMode.fromKey(json.optString("fourValueMode")),
            selectedMoreTool = json.optString("selectedMoreTool").takeIf { it.isNotBlank() && it != "null" },
            history = decodeHistory(json.optJSONArray("history")), originalHistory = decodeHistory(json.optJSONArray("originalHistory")),
            theme = runCatching { enumValueOf<ThemeMode>(json.optString("theme")) }.getOrDefault(ThemeMode.SYSTEM),
            fontScale = json.optDouble("fontScale", 1.0).toFloat().takeIf { it in 0.85f..1.3f } ?: 1f,
            calSection = runCatching { enumValueOf<CalSection>(json.optString("calSection")) }.getOrDefault(CalSection.CALCULATOR),
            notes = decodeNotes(json.optJSONArray("notes"))
        )
    } catch (_: Exception) { null }

    private fun decodeHistory(array: JSONArray?): List<HistoryEntry> = if (array == null) {
        emptyList()
    } else {
        (0 until minOf(array.length(), 100)).mapNotNull { index ->
            runCatching {
                array.getJSONObject(index).let { HistoryEntry(it.getString("id"), it.optString("label"), it.optString("value"), it.optLong("createdAt")) }
            }.getOrNull()
        }
    }

    private fun decodeNotes(array: JSONArray?): List<SmartNote> = if (array == null) {
        emptyList()
    } else {
        (0 until minOf(array.length(), 500)).mapNotNull { index ->
            runCatching {
                array.getJSONObject(index).let {
                    SmartNote(
                        id = it.getString("id"), title = it.optString("title"), details = it.optString("details"),
                        createdAt = it.optLong("createdAt"), updatedAt = it.optLong("updatedAt"), completed = it.optBoolean("completed"),
                        reminderAt = if (it.isNull("reminderAt")) null else it.optLong("reminderAt").takeIf { value -> value > 0L },
                        repeat = runCatching { enumValueOf<ReminderRepeat>(it.optString("repeat")) }.getOrDefault(ReminderRepeat.NONE)
                    )
                }
            }.getOrNull()
        }
    }
}
