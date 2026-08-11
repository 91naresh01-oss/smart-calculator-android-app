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
    fun theme() = mutate { it.copy(theme = when (it.theme) { ThemeMode.SYSTEM -> ThemeMode.LIGHT; ThemeMode.LIGHT -> ThemeMode.DARK; ThemeMode.DARK -> ThemeMode.SYSTEM }) }
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
    fun resetFourValueMode() = mutate { current ->
        val mode = current.fourValueMode
        val prefix = "four-${mode.key}-"
        current.copy(toolInputs = current.toolInputs.filterKeys { !it.startsWith(prefix) } + mode.inputDefaults())
    }
    fun selectMoreTool(tool: String?) = mutate { it.copy(selectedMoreTool = tool) }
    fun saveHistory(label: String, value: String) = mutate { it.copy(history = (listOf(HistoryEntry(UUID.randomUUID().toString(), label, value, System.currentTimeMillis())) + it.history).take(100)) }
    fun originalHistory(entries: List<HistoryEntry>) = mutate { it.copy(originalHistory = entries.take(100)) }
    fun clearHistory() = mutate { it.copy(history = emptyList(), originalHistory = emptyList()) }
}
