package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.theme.RefTheme

/** What a [RefTextField] holds. Drives the keyboard, masking and autofill-friendly defaults. */
enum class RefTextFieldType {
    Text,
    Email,
    Password,
    Number,
}

/**
 * Single-line text input with a label and an optional error.
 *
 * @param error shown below the field and announced by screen readers; `null` = valid.
 */
@Composable
fun RefTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    type: RefTextFieldType = RefTextFieldType.Text,
    error: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    val keyboardType =
        when (type) {
            RefTextFieldType.Text -> KeyboardType.Text
            RefTextFieldType.Email -> KeyboardType.Email
            RefTextFieldType.Password -> KeyboardType.Password
            RefTextFieldType.Number -> KeyboardType.Number
        }
    val belowField = error ?: supportingText
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        supportingText = belowField?.let { { Text(it) } },
        isError = error != null,
        enabled = enabled,
        singleLine = true,
        visualTransformation =
            if (type == RefTextFieldType.Password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
                autoCorrectEnabled = type == RefTextFieldType.Text,
            ),
        keyboardActions = KeyboardActions(onAny = { onImeAction() }),
    )
}

@Preview
@Composable
private fun RefTextFieldPreview() {
    RefTheme {
        RefSurface {
            Column {
                RefTextField(value = "", onValueChange = {}, label = "Email", type = RefTextFieldType.Email)
                RefTextField(value = "secret", onValueChange = {}, label = "Password", type = RefTextFieldType.Password)
                RefTextField(value = "abc", onValueChange = {}, label = "Email", error = "Enter a valid email address")
            }
        }
    }
}
