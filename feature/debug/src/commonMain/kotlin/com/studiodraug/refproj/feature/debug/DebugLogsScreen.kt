package com.studiodraug.refproj.feature.debug

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Severity
import com.studiodraug.refproj.core.designsystem.component.RefButton
import com.studiodraug.refproj.core.designsystem.component.RefButtonStyle
import com.studiodraug.refproj.core.designsystem.component.RefContentWidth
import com.studiodraug.refproj.core.designsystem.component.RefDivider
import com.studiodraug.refproj.core.designsystem.component.RefEmptyState
import com.studiodraug.refproj.core.designsystem.component.RefFilterChip
import com.studiodraug.refproj.core.designsystem.component.RefTopAppBar
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.feature.debug.resources.Res
import com.studiodraug.refproj.feature.debug.resources.debug_clear
import com.studiodraug.refproj.feature.debug.resources.debug_level_debug
import com.studiodraug.refproj.feature.debug.resources.debug_level_error
import com.studiodraug.refproj.feature.debug.resources.debug_level_info
import com.studiodraug.refproj.feature.debug.resources.debug_level_warn
import com.studiodraug.refproj.feature.debug.resources.debug_logs
import com.studiodraug.refproj.feature.debug.resources.debug_logs_empty_message
import com.studiodraug.refproj.feature.debug.resources.debug_logs_empty_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DebugLogsScreen(
    state: DebugLogsUiState,
    onAction: (DebugLogsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RefTopAppBar(
                title = stringResource(Res.string.debug_logs),
                onBack = onBack,
                actions = {
                    RefButton(
                        text = stringResource(Res.string.debug_clear),
                        onClick = { onAction(DebugLogsAction.Clear) },
                        style = RefButtonStyle.Tertiary,
                        enabled = state.lines.isNotEmpty(),
                    )
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        RefContentWidth(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = RefTheme.spacing.screenHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(RefTheme.spacing.sm),
                ) {
                    DebugLogsViewModel.FILTER_LEVELS.forEach { level ->
                        RefFilterChip(
                            text = stringResource(level.label()),
                            selected = level == state.minSeverity,
                            onClick = { onAction(DebugLogsAction.MinSeveritySelected(level)) },
                        )
                    }
                }
                if (state.lines.isEmpty()) {
                    RefEmptyState(
                        title = stringResource(Res.string.debug_logs_empty_title),
                        message = stringResource(Res.string.debug_logs_empty_message),
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(state.lines) { index, line ->
                            if (index > 0) RefDivider()
                            LogLineRow(line)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogLineRow(line: LogLine) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = RefTheme.spacing.screenHorizontal, vertical = RefTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(RefTheme.spacing.xxs),
    ) {
        Text(
            text = "${line.time}  ${line.severity.name.first()}/${line.tag}",
            style = MaterialTheme.typography.labelSmall,
            color = line.severity.color(),
            fontFamily = FontFamily.Monospace,
        )
        Text(text = line.message, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
        line.stackTrace?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                maxLines = STACK_TRACE_LINES,
            )
        }
    }
}

@Composable
private fun Severity.color(): Color =
    when {
        this >= Severity.Error -> MaterialTheme.colorScheme.error
        this == Severity.Warn -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

private fun Severity.label(): StringResource =
    when (this) {
        Severity.Info -> Res.string.debug_level_info
        Severity.Warn -> Res.string.debug_level_warn
        Severity.Error, Severity.Assert -> Res.string.debug_level_error
        Severity.Verbose, Severity.Debug -> Res.string.debug_level_debug
    }

private const val STACK_TRACE_LINES = 8

@Preview
@Composable
private fun DebugLogsScreenPreview() {
    RefTheme {
        DebugLogsScreen(
            state =
                DebugLogsUiState(
                    minSeverity = Severity.Info,
                    lines =
                        listOf(
                            LogLine("09:41:02.120", Severity.Warn, "Network", "Slow response (2.4 s)", stackTrace = null),
                            LogLine("09:41:00.003", Severity.Info, "Analytics", "Screen home", stackTrace = null),
                        ),
                ),
            onAction = {},
            onBack = {},
        )
    }
}
