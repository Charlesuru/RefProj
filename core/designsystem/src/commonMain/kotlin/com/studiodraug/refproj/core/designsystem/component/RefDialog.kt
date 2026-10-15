package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.resources.Res
import com.studiodraug.refproj.core.designsystem.resources.designsystem_cancel
import com.studiodraug.refproj.core.designsystem.resources.designsystem_confirm
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Asks the user to confirm an action. Set [destructive] for irreversible actions:
 * the confirm button turns red.
 */
@Composable
fun RefConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = stringResource(Res.string.designsystem_confirm),
    dismissText: String = stringResource(Res.string.designsystem_cancel),
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(title) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            RefButton(
                text = confirmText,
                onClick = onConfirm,
                style = if (destructive) RefButtonStyle.Destructive else RefButtonStyle.Tertiary,
            )
        },
        dismissButton = { RefButton(text = dismissText, onClick = onDismiss, style = RefButtonStyle.Tertiary) },
    )
}

@Preview
@Composable
private fun RefConfirmDialogPreview() {
    RefTheme {
        RefConfirmDialog(
            title = "Clear all data?",
            message = "This signs you out and removes everything stored on this device.",
            onConfirm = {},
            onDismiss = {},
            confirmText = "Clear",
            destructive = true,
        )
    }
}
