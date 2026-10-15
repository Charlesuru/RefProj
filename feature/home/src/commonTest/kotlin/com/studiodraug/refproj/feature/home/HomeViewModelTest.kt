package com.studiodraug.refproj.feature.home

import com.studiodraug.refproj.core.config.AppEnvironment
import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.core.testing.testAppConfig
import kotlin.test.Test
import kotlin.test.assertEquals

// UNIT TEST
class HomeViewModelTest {
    @Test
    fun state_showsEnvironmentAndVersion() =
        runViewModelTest {
            val viewModel = HomeViewModel(testAppConfig(environment = AppEnvironment.SIT))

            assertEquals(HomeUiState(environmentName = "SIT", version = "1.0.0-test (1)"), viewModel.state.value)
        }
}
