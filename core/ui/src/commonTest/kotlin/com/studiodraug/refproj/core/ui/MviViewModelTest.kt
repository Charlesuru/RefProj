package com.studiodraug.refproj.core.ui

import app.cash.turbine.test
import com.studiodraug.refproj.core.testing.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals

// UNIT TEST
class MviViewModelTest {
    private class CounterViewModel : MviViewModel<Int, CounterViewModel.Action, String>(initialState = 0) {
        enum class Action { Increment, Announce }

        override fun onAction(action: Action) {
            when (action) {
                Action.Increment -> updateState { this + 1 }
                Action.Announce -> sendEvent("count=$currentState")
            }
        }
    }

    @Test
    fun actions_updateState_andEventsAreDeliveredOnce() =
        runViewModelTest {
            val viewModel = CounterViewModel()

            viewModel.onAction(CounterViewModel.Action.Increment)
            viewModel.onAction(CounterViewModel.Action.Increment)
            viewModel.onAction(CounterViewModel.Action.Announce)

            assertEquals(2, viewModel.state.value)
            viewModel.events.test {
                assertEquals("count=2", awaitItem())
                expectNoEvents()
            }
        }
}
