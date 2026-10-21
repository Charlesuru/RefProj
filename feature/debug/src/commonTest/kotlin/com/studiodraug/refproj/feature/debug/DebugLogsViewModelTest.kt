package com.studiodraug.refproj.feature.debug

import co.touchlab.kermit.Severity
import com.studiodraug.refproj.core.observability.LogBuffer
import com.studiodraug.refproj.core.testing.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

// UNIT TEST
class DebugLogsViewModelTest {
    private val buffer = LogBuffer(clock = FixedClock(Instant.parse("2026-10-21T09:41:02.120Z")))
    private val viewModel by lazy { DebugLogsViewModel(buffer, TimeZone.UTC) }

    @Test
    fun lines_areNewestFirst_withTheTimeOfDay() =
        runViewModelTest {
            buffer.log(Severity.Info, "first", "App", null)
            buffer.log(Severity.Warn, "second", "Network", null)

            assertEquals(
                listOf(
                    LogLine("09:41:02.120", Severity.Warn, "Network", "second", stackTrace = null),
                    LogLine("09:41:02.120", Severity.Info, "App", "first", stackTrace = null),
                ),
                viewModel.state.value.lines,
            )
        }

    @Test
    fun newLines_showUpWhileTheScreenIsOpen() =
        runViewModelTest {
            viewModel.state.value
            buffer.log(Severity.Info, "later", "App", null)

            assertEquals(
                listOf("later"),
                viewModel.state.value.lines
                    .map { it.message },
            )
        }

    @Test
    fun filtering_hidesLinesBelowTheSelectedLevel() =
        runViewModelTest {
            buffer.log(Severity.Debug, "debug", "App", null)
            buffer.log(Severity.Info, "info", "App", null)
            buffer.log(Severity.Error, "error", "App", null)

            viewModel.onAction(DebugLogsAction.MinSeveritySelected(Severity.Info))

            assertEquals(Severity.Info, viewModel.state.value.minSeverity)
            assertEquals(
                listOf("error", "info"),
                viewModel.state.value.lines
                    .map { it.message },
            )
        }

    @Test
    fun clear_emptiesTheBuffer() =
        runViewModelTest {
            buffer.log(Severity.Info, "line", "App", null)
            viewModel.onAction(DebugLogsAction.Clear)

            assertTrue(
                viewModel.state.value.lines
                    .isEmpty(),
            )
            assertTrue(buffer.entries.value.isEmpty())
        }

    private class FixedClock(
        private val instant: Instant,
    ) : Clock {
        override fun now(): Instant = instant
    }
}
