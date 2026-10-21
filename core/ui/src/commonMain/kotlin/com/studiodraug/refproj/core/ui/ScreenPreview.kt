package com.studiodraug.refproj.core.ui

import androidx.compose.runtime.Composable

/**
 * A screen in a fixed sample state, e.g. "Login – wrong password". Rendered by the debug menu's
 * screen catalog, so testers and designers can check every state on a real device without
 * reaching it through the app. [content] must be stateless: sample data in, no-op actions.
 */
class ScreenPreview(
    val name: String,
    val content: @Composable () -> Unit,
)

/**
 * The sample screens one feature (or the design system) contributes to the catalog. Each feature
 * binds one in its Koin module under its own name, like its `NavEntryInstaller`, so the debug menu
 * never depends on a feature.
 */
class ScreenPreviewGroup(
    val title: String,
    val previews: List<ScreenPreview>,
)
