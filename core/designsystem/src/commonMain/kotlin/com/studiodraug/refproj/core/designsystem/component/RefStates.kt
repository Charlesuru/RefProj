package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.icon.RefIcons
import com.studiodraug.refproj.core.designsystem.resources.Res
import com.studiodraug.refproj.core.designsystem.resources.designsystem_error_title
import com.studiodraug.refproj.core.designsystem.resources.designsystem_loading
import com.studiodraug.refproj.core.designsystem.resources.designsystem_retry
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import org.jetbrains.compose.resources.stringResource

// Full-screen states every screen needs: loading, error and empty. Using these keeps the
// three states consistent across features.

/** Centred progress indicator for a screen whose content is loading. */
@Composable
fun RefLoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.designsystem_loading)
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier =
                Modifier
                    .size(RefTheme.sizes.progressIndicator)
                    .semantics { contentDescription = description },
        )
    }
}

/** A failure the user can retry. [message] explains what failed in plain language. */
@Composable
fun RefErrorState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.designsystem_error_title),
    onRetry: (() -> Unit)? = null,
) {
    MessageState(
        icon = RefIcons.Error,
        iconTint = MaterialTheme.colorScheme.error,
        title = title,
        message = message,
        modifier = modifier,
        action = onRetry?.let { retry -> { RefButton(text = stringResource(Res.string.designsystem_retry), onClick = retry) } },
    )
}

/** Nothing to show yet, optionally with an action that creates the first item. */
@Composable
fun RefEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    MessageState(
        icon = RefIcons.Empty,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        title = title,
        message = message,
        modifier = modifier,
        action =
            if (actionText != null && onAction != null) {
                { RefButton(text = actionText, onClick = onAction, style = RefButtonStyle.Secondary) }
            } else {
                null
            },
    )
}

@Composable
private fun MessageState(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(RefTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(RefTheme.spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Decorative: the title says the same thing.
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(RefTheme.sizes.iconLarge))
        Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        action?.invoke()
    }
}

@Preview
@Composable
private fun RefLoadingStatePreview() {
    RefTheme { RefSurface { RefLoadingState() } }
}

@Preview
@Composable
private fun RefErrorStatePreview() {
    RefTheme { RefSurface { RefErrorState(message = "Check your connection and try again.", onRetry = {}) } }
}

@Preview
@Composable
private fun RefEmptyStatePreview() {
    RefTheme {
        RefSurface {
            RefEmptyState(title = "No tasks yet", message = "Tasks you add show up here.", actionText = "Add task", onAction = {})
        }
    }
}
