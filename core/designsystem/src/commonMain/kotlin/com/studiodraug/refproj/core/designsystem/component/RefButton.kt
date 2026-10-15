package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.resources.Res
import com.studiodraug.refproj.core.designsystem.resources.designsystem_loading
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import org.jetbrains.compose.resources.stringResource

/** Visual weight of a [RefButton]. Use one [Primary] per screen at most. */
enum class RefButtonStyle {
    /** The main action of a screen. */
    Primary,

    /** Alternative actions next to a primary one. */
    Secondary,

    /** Low-emphasis actions, e.g. "Skip". */
    Tertiary,

    /** Irreversible actions, e.g. "Delete account". */
    Destructive,
}

/**
 * The only button features should use.
 *
 * @param loading shows a progress indicator instead of the label and ignores clicks, so a
 *  request cannot be sent twice.
 */
@Composable
fun RefButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: RefButtonStyle = RefButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val loadingDescription = stringResource(Res.string.designsystem_loading)
    val buttonModifier =
        modifier
            .heightIn(min = RefTheme.sizes.buttonHeight)
            .semantics { if (loading) stateDescription = loadingDescription }
    val onClickUnlessLoading = { if (!loading) onClick() }
    val content: @Composable () -> Unit = { ButtonContent(text = text, loading = loading) }

    when (style) {
        RefButtonStyle.Primary -> {
            Button(onClick = onClickUnlessLoading, modifier = buttonModifier, enabled = enabled) { content() }
        }

        RefButtonStyle.Secondary -> {
            OutlinedButton(onClick = onClickUnlessLoading, modifier = buttonModifier, enabled = enabled) { content() }
        }

        RefButtonStyle.Tertiary -> {
            TextButton(onClick = onClickUnlessLoading, modifier = buttonModifier, enabled = enabled) { content() }
        }

        RefButtonStyle.Destructive -> {
            Button(
                onClick = onClickUnlessLoading,
                modifier = buttonModifier,
                enabled = enabled,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
            ) { content() }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(RefTheme.sizes.progressIndicatorInline),
            color = LocalContentColor.current,
            strokeWidth = RefTheme.sizes.progressStrokeInline,
        )
    } else {
        Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Preview
@Composable
private fun RefButtonPreview() {
    RefTheme {
        RefSurface {
            Column {
                RefButtonStyle.entries.forEach { style ->
                    RefButton(text = style.name, onClick = {}, style = style, modifier = Modifier.fillMaxWidth())
                }
                RefButton(text = "Loading", onClick = {}, loading = true, modifier = Modifier.fillMaxWidth())
                RefButton(text = "Disabled", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
