package com.studiodraug.refproj.feature.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.component.RefContentWidth
import com.studiodraug.refproj.core.designsystem.component.RefEmptyState
import com.studiodraug.refproj.core.designsystem.component.RefListItem
import com.studiodraug.refproj.core.designsystem.component.RefTopAppBar
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.ui.ScreenPreview
import com.studiodraug.refproj.feature.debug.resources.Res
import com.studiodraug.refproj.feature.debug.resources.debug_catalog
import com.studiodraug.refproj.feature.debug.resources.debug_preview_not_found
import com.studiodraug.refproj.feature.debug.resources.debug_preview_not_found_message
import org.jetbrains.compose.resources.stringResource

/** Table of contents of the screen catalog, grouped by feature. */
@Composable
fun DebugCatalogScreen(
    state: DebugCatalogUiState,
    onOpenPreview: (group: String, preview: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { RefTopAppBar(title = stringResource(Res.string.debug_catalog), onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        RefContentWidth(modifier = Modifier.padding(padding)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                state.groups.forEach { group ->
                    item(key = group.title) {
                        Text(
                            text = group.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier
                                    .padding(
                                        start = RefTheme.spacing.screenHorizontal,
                                        end = RefTheme.spacing.screenHorizontal,
                                        top = RefTheme.spacing.lg,
                                        bottom = RefTheme.spacing.xs,
                                    ).semantics { heading() },
                        )
                    }
                    items(group.previews, key = { "${group.title}/$it" }) { preview ->
                        RefListItem(title = preview, onClick = { onOpenPreview(group.title, preview) })
                    }
                }
            }
        }
    }
}

/**
 * Shows one sample screen under a title bar with a way back. The sample's own insets are handled
 * here, so its top bar does not add the status bar a second time.
 */
@Composable
fun DebugPreviewScreen(
    title: String,
    preview: ScreenPreview?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { RefTopAppBar(title = title, onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(modifier = Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize()) {
            if (preview != null) {
                preview.content()
            } else {
                RefEmptyState(
                    title = stringResource(Res.string.debug_preview_not_found),
                    message = stringResource(Res.string.debug_preview_not_found_message),
                )
            }
        }
    }
}

@Preview
@Composable
private fun DebugCatalogScreenPreview() {
    RefTheme {
        DebugCatalogScreen(
            state =
                DebugCatalogUiState(
                    listOf(
                        CatalogGroup("Login", listOf("Empty", "Wrong password")),
                        CatalogGroup("Tasks", listOf("Loading", "Empty", "List")),
                    ),
                ),
            onOpenPreview = { _, _ -> },
            onBack = {},
        )
    }
}
