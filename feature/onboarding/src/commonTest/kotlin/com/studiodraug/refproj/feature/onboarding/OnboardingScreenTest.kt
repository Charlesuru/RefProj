package com.studiodraug.refproj.feature.onboarding

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.testing.PlatformTest
import com.studiodraug.refproj.feature.onboarding.resources.Res
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_consent_switch
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_consent_title
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_get_started
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_next
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_private_title
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_skip
import com.studiodraug.refproj.feature.onboarding.resources.onboarding_tasks_title
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.getString

// UI TEST

/** Paging through onboarding, the opt-in consent switch (off by default) and finishing. */
@OptIn(ExperimentalTestApi::class)
class OnboardingScreenTest : PlatformTest() {
    @Test
    fun next_pagesThroughToTheConsentPage_whereTheUserFinishes() =
        runComposeUiTest {
            val actions = mutableListOf<OnboardingAction>()
            setContent { RefTheme { OnboardingScreen(state = OnboardingUiState(), onAction = { actions += it }) } }

            onNodeWithText(getString(Res.string.onboarding_tasks_title)).assertExists()
            onNodeWithText(getString(Res.string.onboarding_next)).performClick()
            onNodeWithText(getString(Res.string.onboarding_private_title)).assertExists()
            onNodeWithText(getString(Res.string.onboarding_next)).performClick()
            onNodeWithText(getString(Res.string.onboarding_consent_title)).assertExists()
            onNodeWithText(getString(Res.string.onboarding_consent_switch)).assertIsOff().performClick()
            onNodeWithText(getString(Res.string.onboarding_get_started)).performClick()

            assertEquals(listOf(OnboardingAction.AnalyticsConsentChanged(true), OnboardingAction.Finish), actions)
        }

    @Test
    fun skip_jumpsToTheLastPage() =
        runComposeUiTest {
            setContent { RefTheme { OnboardingScreen(state = OnboardingUiState(), onAction = {}) } }

            onNodeWithText(getString(Res.string.onboarding_skip)).performClick()

            onNodeWithText(getString(Res.string.onboarding_get_started)).assertExists()
            onNodeWithText(getString(Res.string.onboarding_skip)).assertDoesNotExist()
        }
}
