package com.naresh.smartcalculatornote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class CalculatorViewModel(private val repository: CalculatorRepository) : ViewModel() {
    private val edits = MutableStateFlow<AppState?>(null)
    private val saveMutex = Mutex()
    val state: StateFlow<AppState> = repository.state
        .combine(edits) { saved, edited -> edited ?: saved }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState())
    private fun mutate(block: (AppState) -> AppState) {
        val next = block(edits.value ?: state.value)
        edits.value = next
        viewModelScope.launch { saveMutex.withLock { repository.save(next) } }
    }
    fun select(tab: MainTab) = mutate {
        it.copy(
            activeTab = tab,
            selectedMoreTool = if (tab == MainTab.MORE && it.activeTab != MainTab.MORE) null else it.selectedMoreTool
        )
    }
    fun theme(mode: ThemeMode) = mutate { it.copy(theme = mode) }
    fun fontScale(scale: Float) = mutate { it.copy(fontScale = scale.coerceIn(0.85f, 1.3f)) }
    fun calSection(section: CalSection) = mutate { it.copy(calSection = section) }
    fun rows(rows: List<CalcRow>) = mutate { it.copy(rows = rows.ifEmpty { listOf(CalcRow(UUID.randomUUID().toString())) }) }
    fun cash(cash: Map<Int, String>) = mutate { it.copy(cash = cash) }
    fun input(key: String, value: String) = mutate { it.copy(toolInputs = it.toolInputs + (key to value)) }
    fun selectFourValueMode(mode: FourValueMode) = mutate { current ->
        val prefix = "four-${mode.key}-"
        current.copy(
            fourValueMode = mode,
            toolInputs = current.toolInputs.filterKeys { !it.startsWith(prefix) } + mode.inputDefaults()
        )
    }
    fun switchFourValueMode(mode: FourValueMode) = mutate { current ->
        current.copy(
            fourValueMode = mode,
            toolInputs = mode.inputDefaults() + current.toolInputs
        )
    }
    fun resetFourValueMode() = mutate { current ->
        val mode = current.fourValueMode
        val prefix = "four-${mode.key}-"
        current.copy(toolInputs = current.toolInputs.filterKeys { !it.startsWith(prefix) } + mode.inputDefaults())
    }
    fun selectMoreTool(tool: String?) = mutate { it.copy(selectedMoreTool = tool) }
    fun saveHistory(label: String, value: String) = mutate { it.copy(history = (listOf(HistoryEntry(UUID.randomUUID().toString(), label, value, System.currentTimeMillis())) + it.history).take(100)) }
    fun originalHistory(entries: List<HistoryEntry>) = mutate { it.copy(originalHistory = entries.take(100)) }
    fun clearHistory() = mutate { it.copy(history = emptyList(), originalHistory = emptyList()) }
    fun upsertNote(note: SmartNote) = mutate { current ->
        val existing = current.notes.indexOfFirst { it.id == note.id }
        val next = if (existing >= 0) current.notes.toMutableList().apply { set(existing, note) } else (listOf(note) + current.notes)
        current.copy(notes = next.sortedByDescending { it.updatedAt }.take(500))
    }
    fun deleteNote(id: String) = mutate { it.copy(notes = it.notes.filterNot { note -> note.id == id }) }
    fun completeNote(id: String) = mutate { current ->
        current.copy(notes = current.notes.map { note ->
            if (note.id == id) note.copy(completed = true, reminderAt = null, updatedAt = System.currentTimeMillis()) else note
        })
    }
    fun openNotes() = mutate { it.copy(activeTab = MainTab.CAL, calSection = CalSection.NOTES) }
}
