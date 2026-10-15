package com.studiodraug.refproj.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * Runs a ViewModel test: `viewModelScope` uses `Dispatchers.Main`, which tests replace with a test
 * dispatcher sharing the [runTest] scheduler, so coroutines run eagerly and deterministically.
 */
@OptIn(ExperimentalCoroutinesApi::class) // setMain/resetMain: the documented way to test Main in common code.
fun runViewModelTest(testBody: suspend TestScope.() -> Unit) {
    val dispatcher = UnconfinedTestDispatcher()
    Dispatchers.setMain(dispatcher)
    try {
        runTest(dispatcher) { testBody() }
    } finally {
        Dispatchers.resetMain()
    }
}
