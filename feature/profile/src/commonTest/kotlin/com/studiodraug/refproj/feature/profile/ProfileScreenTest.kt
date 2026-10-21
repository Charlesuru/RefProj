package com.studiodraug.refproj.feature.profile

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.testing.PlatformTest
import com.studiodraug.refproj.feature.profile.resources.Res
import com.studiodraug.refproj.feature.profile.resources.profile_analytics
import com.studiodraug.refproj.feature.profile.resources.profile_logout
import com.studiodraug.refproj.feature.profile.resources.profile_logout_message
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.getString

// UI TEST

/** The profile: user details, the consent switch and the logout confirmation. */
@OptIn(ExperimentalTestApi::class)
class ProfileScreenTest : PlatformTest() {
    private val state =
        ProfileUiState(displayName = "Ada Lovelace", email = "ada@example.com", analyticsConsent = true, version = "1.2.3 (45)")

    @Test
    fun showsTheUser_andTogglesConsent() =
        runComposeUiTest {
            val actions = mutableListOf<ProfileAction>()
            setContent { RefTheme { ProfileScreen(state = state, onAction = { actions += it }) } }

            onNodeWithText("Ada Lovelace").assertExists()
            onNodeWithText("ada@example.com").assertExists()
            onNodeWithText("1.2.3 (45)").assertExists()
            onNodeWithText(getString(Res.string.profile_analytics)).assertIsOn().performClick()

            assertEquals(listOf<ProfileAction>(ProfileAction.AnalyticsConsentChanged(false)), actions)
        }

    @Test
    fun logout_isConfirmedInADialog() =
        runComposeUiTest {
            val actions = mutableListOf<ProfileAction>()
            setContent { RefTheme { ProfileScreen(state = state.copy(showLogoutConfirmation = true), onAction = { actions += it }) } }

            onNodeWithText(getString(Res.string.profile_logout_message)).assertExists()
            // The dialog's confirm button is the last "Log out" action on screen (the page button is behind it).
            onAllNodes(hasText(getString(Res.string.profile_logout)) and hasClickAction()).let { buttons ->
                buttons.fetchSemanticsNodes().size.let { count -> buttons[count - 1].performClick() }
            }

            assertEquals(listOf<ProfileAction>(ProfileAction.LogoutConfirmed), actions)
        }
}
