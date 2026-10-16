package com.studiodraug.refproj.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.component.RefCard
import com.studiodraug.refproj.core.designsystem.component.RefContentWidth
import com.studiodraug.refproj.core.designsystem.component.RefTopAppBar
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.domain.tasks.TaskSummary
import com.studiodraug.refproj.feature.home.resources.Res
import com.studiodraug.refproj.feature.home.resources.home_environment
import com.studiodraug.refproj.feature.home.resources.home_greeting
import com.studiodraug.refproj.feature.home.resources.home_summary_done
import com.studiodraug.refproj.feature.home.resources.home_summary_open
import com.studiodraug.refproj.feature.home.resources.home_summary_title
import com.studiodraug.refproj.feature.home.resources.home_title
import com.studiodraug.refproj.feature.home.resources.home_version
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { RefTopAppBar(title = stringResource(Res.string.home_title)) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        RefContentWidth(modifier = Modifier.padding(padding)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = RefTheme.spacing.screenHorizontal, vertical = RefTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(RefTheme.spacing.lg),
            ) {
                state.displayName?.let { name ->
                    Text(text = stringResource(Res.string.home_greeting, name), style = MaterialTheme.typography.headlineSmall)
                }
                RefCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(Res.string.home_summary_title), style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(RefTheme.spacing.xxl)) {
                        Count(label = stringResource(Res.string.home_summary_open), value = state.summary.open)
                        Count(label = stringResource(Res.string.home_summary_done), value = state.summary.done)
                    }
                }
                BuildInfo(state)
            }
        }
    }
}

/** Read as one unit by screen readers ("3, Open"). */
@Composable
private fun Count(
    label: String,
    value: Int,
) {
    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(text = value.toString(), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BuildInfo(state: HomeUiState) {
    val version = state.version ?: return
    Column {
        Text(
            text = stringResource(Res.string.home_environment, state.environmentName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.home_version, version),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    RefTheme {
        HomeScreen(
            state =
                HomeUiState(
                    displayName = "Test User",
                    summary = TaskSummary(open = 3, done = 5),
                    environmentName = "DEV",
                    version = "1.0.0 (42)",
                ),
        )
    }
}
