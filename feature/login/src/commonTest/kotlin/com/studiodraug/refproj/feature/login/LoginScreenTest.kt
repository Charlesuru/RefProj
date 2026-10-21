package com.studiodraug.refproj.feature.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.testing.PlatformTest
import com.studiodraug.refproj.domain.auth.EmailError
import com.studiodraug.refproj.feature.login.resources.Res
import com.studiodraug.refproj.feature.login.resources.login_email
import com.studiodraug.refproj.feature.login.resources.login_error_credentials
import com.studiodraug.refproj.feature.login.resources.login_error_email_invalid
import com.studiodraug.refproj.feature.login.resources.login_password
import com.studiodraug.refproj.feature.login.resources.login_submit
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.getString

// UI TEST

/** The login form as the user sees it: input, submit, validation and error messages, loading. */
@OptIn(ExperimentalTestApi::class)
class LoginScreenTest : PlatformTest() {
    @Test
    fun typingAndSubmitting_sendTheMatchingActions() =
        runComposeUiTest {
            val actions = mutableListOf<LoginAction>()
            // The fields are controlled: the state must follow the input like the ViewModel's does.
            var state by mutableStateOf(LoginUiState())
            setContent {
                RefTheme {
                    LoginScreen(
                        state = state,
                        onAction = { action ->
                            actions += action
                            state =
                                when (action) {
                                    is LoginAction.EmailChanged -> state.copy(email = action.value)
                                    is LoginAction.PasswordChanged -> state.copy(password = action.value)
                                    else -> state
                                }
                        },
                    )
                }
            }

            onNodeWithText(getString(Res.string.login_email)).performTextInput("ada@example.com")
            onNodeWithText(getString(Res.string.login_password)).performTextInput("secret")
            onNode(hasText(getString(Res.string.login_submit)) and hasClickAction()).performClick()

            assertEquals(
                listOf(
                    LoginAction.EmailChanged("ada@example.com"),
                    LoginAction.PasswordChanged("secret"),
                    LoginAction.Submit,
                ),
                actions,
            )
        }

    @Test
    fun validationAndLoginErrors_areShown() =
        runComposeUiTest {
            setContent {
                RefTheme {
                    LoginScreen(
                        state = LoginUiState(email = "ada", emailError = EmailError.Invalid, error = LoginError.WrongCredentials),
                        onAction = {},
                    )
                }
            }

            onNodeWithText(getString(Res.string.login_error_email_invalid)).assertExists()
            onNodeWithText(getString(Res.string.login_error_credentials)).assertExists()
        }

    @Test
    fun whileLoggingIn_theFieldsAreDisabled() =
        runComposeUiTest {
            setContent { RefTheme { LoginScreen(state = LoginUiState(isLoading = true), onAction = {}) } }

            onNodeWithText(getString(Res.string.login_email)).assertIsNotEnabled()
            onNodeWithText(getString(Res.string.login_password)).assertIsNotEnabled()
        }
}
