package com.studiodraug.refproj.feature.home

import com.studiodraug.refproj.core.ui.ScreenPreview
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup
import com.studiodraug.refproj.domain.tasks.TaskSummary

/** Sample states for the debug menu's screen catalog. */
internal val homePreviews =
    ScreenPreviewGroup(
        title = "Home",
        previews =
            listOf(
                ScreenPreview("Summary") { HomeScreen(state = summary) },
                ScreenPreview("With build info") { HomeScreen(state = summary.copy(version = "1.0.0 (42)")) },
                ScreenPreview("No tasks") { HomeScreen(state = summary.copy(summary = TaskSummary(open = 0, done = 0))) },
                ScreenPreview("User not loaded yet") { HomeScreen(state = summary.copy(displayName = null)) },
            ),
    )

private val summary = HomeUiState(displayName = "Ada", summary = TaskSummary(open = 3, done = 5), environmentName = "DEV")
