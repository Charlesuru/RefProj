package com.studiodraug.refproj.feature.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.testing.PlatformTest
import com.studiodraug.refproj.domain.tasks.TaskSummary
import com.studiodraug.refproj.feature.home.resources.Res
import com.studiodraug.refproj.feature.home.resources.home_greeting
import com.studiodraug.refproj.feature.home.resources.home_version
import kotlin.test.Test
import org.jetbrains.compose.resources.getString

// UI TEST

/** Home: the greeting once the user is known, the task counts, and build info only behind its flag. */
@OptIn(ExperimentalTestApi::class)
class HomeScreenTest : PlatformTest() {
    @Test
    fun showsGreetingAndCounts_withoutBuildInfoByDefault() =
        runComposeUiTest {
            setContent {
                RefTheme {
                    HomeScreen(state = HomeUiState(displayName = "Ada", summary = TaskSummary(open = 3, done = 7), environmentName = "DEV"))
                }
            }

            onNodeWithText(getString(Res.string.home_greeting, "Ada")).assertExists()
            onNodeWithText("3").assertExists()
            onNodeWithText("7").assertExists()
            onNodeWithText(getString(Res.string.home_version, "1.0 (1)")).assertDoesNotExist()
        }

    @Test
    fun buildInfo_isShownWhenTheFlagProvidesAVersion() =
        runComposeUiTest {
            setContent { RefTheme { HomeScreen(state = HomeUiState(environmentName = "DEV", version = "1.0 (1)")) } }

            onNodeWithText(getString(Res.string.home_version, "1.0 (1)")).assertExists()
            onNodeWithText(getString(Res.string.home_greeting, "Ada")).assertDoesNotExist()
        }
}
