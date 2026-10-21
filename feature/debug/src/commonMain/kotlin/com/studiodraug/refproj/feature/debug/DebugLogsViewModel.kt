package com.studiodraug.refproj.feature.debug

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Severity
import com.studiodraug.refproj.core.observability.LogBuffer
import com.studiodraug.refproj.core.observability.LogEntry
import com.studiodraug.refproj.core.ui.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

/** One log line, ready to display. */
data class LogLine(
    val time: String,
    val severity: Severity,
    val tag: String,
    val message: String,
    val stackTrace: String?,
)

data class DebugLogsUiState(
    val minSeverity: Severity = Severity.Debug,
    /** Newest first. */
    val lines: List<LogLine> = emptyList(),
)

sealed interface DebugLogsAction {
    data class MinSeveritySelected(
        val severity: Severity,
    ) : DebugLogsAction

    data object Clear : DebugLogsAction
}

/** The log viewer: the in-memory [LogBuffer], newest first, filtered by level. */
class DebugLogsViewModel(
    private val buffer: LogBuffer,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : MviViewModel<DebugLogsUiState, DebugLogsAction, Nothing>(DebugLogsUiState()) {
    private val minSeverity = MutableStateFlow(currentState.minSeverity)

    init {
        viewModelScope.launch {
            combine(buffer.entries, minSeverity) { entries, min ->
                min to entries.asReversed().filter { it.severity >= min }.map { it.toLine() }
            }.collect { (min, lines) -> updateState { copy(minSeverity = min, lines = lines) } }
        }
    }

    override fun onAction(action: DebugLogsAction) {
        when (action) {
            is DebugLogsAction.MinSeveritySelected -> minSeverity.value = action.severity
            DebugLogsAction.Clear -> buffer.clear()
        }
    }

    private fun LogEntry.toLine() =
        LogLine(TIME_FORMAT.format(timestamp.toLocalDateTime(timeZone).time), severity, tag, message, throwable)

    companion object {
        /** The levels the viewer filters by, lowest first. */
        val FILTER_LEVELS = listOf(Severity.Debug, Severity.Info, Severity.Warn, Severity.Error)

        /** `09:41:02.120` */
        private val TIME_FORMAT =
            LocalTime.Format {
                hour()
                char(':')
                minute()
                char(':')
                second()
                char('.')
                secondFraction(fixedLength = 3)
            }
    }
}
