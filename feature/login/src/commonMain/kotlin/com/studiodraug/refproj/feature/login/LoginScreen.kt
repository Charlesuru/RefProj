package com.studiodraug.refproj.feature.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.component.RefButton
import com.studiodraug.refproj.core.designsystem.component.RefContentWidth
import com.studiodraug.refproj.core.designsystem.component.RefSurface
import com.studiodraug.refproj.core.designsystem.component.RefTextField
import com.studiodraug.refproj.core.designsystem.component.RefTextFieldType
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.domain.auth.EmailError
import com.studiodraug.refproj.domain.auth.PasswordError
import com.studiodraug.refproj.feature.login.resources.Res
import com.studiodraug.refproj.feature.login.resources.login_email
import com.studiodraug.refproj.feature.login.resources.login_error_credentials
import com.studiodraug.refproj.feature.login.resources.login_error_email_blank
import com.studiodraug.refproj.feature.login.resources.login_error_email_invalid
import com.studiodraug.refproj.feature.login.resources.login_error_network
import com.studiodraug.refproj.feature.login.resources.login_error_password_blank
import com.studiodraug.refproj.feature.login.resources.login_error_unknown
import com.studiodraug.refproj.feature.login.resources.login_password
import com.studiodraug.refproj.feature.login.resources.login_submit
import com.studiodraug.refproj.feature.login.resources.login_subtitle
import com.studiodraug.refproj.feature.login.resources.login_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onAction(LoginAction.Submit)
    }

    RefSurface(modifier = modifier.fillMaxSize()) {
        // safeDrawing includes the keyboard, so the button stays reachable while typing.
        RefContentWidth(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = RefTheme.spacing.screenHorizontal, vertical = RefTheme.spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(RefTheme.spacing.md),
            ) {
                Text(text = stringResource(Res.string.login_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = stringResource(Res.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RefTextField(
                    value = state.email,
                    onValueChange = { onAction(LoginAction.EmailChanged(it)) },
                    label = stringResource(Res.string.login_email),
                    modifier = Modifier.padding(top = RefTheme.spacing.lg).testTag(LoginTestTags.EMAIL),
                    type = RefTextFieldType.Email,
                    error = state.emailError?.let { stringResource(it.message()) },
                    enabled = !state.isLoading,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                )
                RefTextField(
                    value = state.password,
                    onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                    label = stringResource(Res.string.login_password),
                    modifier = Modifier.testTag(LoginTestTags.PASSWORD),
                    type = RefTextFieldType.Password,
                    error = state.passwordError?.let { stringResource(it.message()) },
                    enabled = !state.isLoading,
                    imeAction = ImeAction.Done,
                    onImeAction = submit,
                )
                state.error?.let { error ->
                    Text(
                        text = stringResource(error.message()),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                RefButton(
                    text = stringResource(Res.string.login_submit),
                    onClick = submit,
                    modifier = Modifier.fillMaxWidth().padding(top = RefTheme.spacing.sm).testTag(LoginTestTags.SUBMIT),
                    loading = state.isLoading,
                )
            }
        }
    }
}

private fun EmailError.message() =
    when (this) {
        EmailError.Blank -> Res.string.login_error_email_blank
        EmailError.Invalid -> Res.string.login_error_email_invalid
    }

private fun PasswordError.message() =
    when (this) {
        PasswordError.Blank -> Res.string.login_error_password_blank
    }

private fun LoginError.message() =
    when (this) {
        LoginError.WrongCredentials -> Res.string.login_error_credentials
        LoginError.Network -> Res.string.login_error_network
        LoginError.Unknown -> Res.string.login_error_unknown
    }

@Preview
@Composable
private fun LoginScreenPreview() {
    RefTheme {
        LoginScreen(
            state = LoginUiState(email = "test@example.com", error = LoginError.WrongCredentials),
            onAction = {},
        )
    }
}

/** Stable IDs for UI automation (end-to-end tests): independent of the language, never shown to users. */
object LoginTestTags {
    const val EMAIL = "login.email"
    const val PASSWORD = "login.password"
    const val SUBMIT = "login.submit"
}
