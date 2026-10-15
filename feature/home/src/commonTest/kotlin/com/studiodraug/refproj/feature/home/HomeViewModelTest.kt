package com.studiodraug.refproj.feature.home

import app.cash.turbine.test
import com.studiodraug.refproj.core.config.AppEnvironment
import com.studiodraug.refproj.core.featureflags.FeatureFlag
import com.studiodraug.refproj.core.featureflags.FeatureFlagState
import com.studiodraug.refproj.core.featureflags.FeatureFlags
import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.core.testing.testAppConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

// UNIT TEST
class HomeViewModelTest {
    private val buildInfo = MutableStateFlow(true)
    private val flags =
        object : FeatureFlags {
            override fun isEnabled(flag: FeatureFlag) = buildInfo.value

            override fun observe(flag: FeatureFlag): Flow<Boolean> = buildInfo

            override fun observeStates(): Flow<List<FeatureFlagState>> = emptyFlow()
        }

    @Test
    fun buildInfo_followsTheFeatureFlag() =
        runViewModelTest {
            val viewModel = HomeViewModel(testAppConfig(environment = AppEnvironment.SIT), flags)

            viewModel.state.test {
                assertEquals(HomeUiState(environmentName = "SIT", version = "1.0.0-test (1)"), awaitItem())
                buildInfo.value = false
                assertNull(awaitItem().version)
            }
        }
}
