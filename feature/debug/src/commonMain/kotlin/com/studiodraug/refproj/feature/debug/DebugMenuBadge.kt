package com.studiodraug.refproj.feature.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.feature.debug.resources.Res
import com.studiodraug.refproj.feature.debug.resources.debug_badge_description
import org.jetbrains.compose.resources.stringResource

/**
 * The entry to the debug menu: a small tab on the screen's start edge showing the environment
 * (`DEV`, `SIT`, …), so testers always see which environment they are in. The app draws it over
 * every screen while the debug menu is enabled — never in a PROD release build.
 */
@Composable
fun DebugMenuBadge(
    environment: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.debug_badge_description)
    Surface(
        onClick = onClick,
        modifier =
            modifier.testTag(DebugTestTags.BADGE).semantics {
                contentDescription = description
                role = Role.Button
            },
        // Flat against the screen edge, rounded on the inside.
        shape = MaterialTheme.shapes.small.copy(topStart = CornerSize(0), bottomStart = CornerSize(0)),
        color = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        shadowElevation = RefTheme.elevation.medium,
    ) {
        Box(
            modifier = Modifier.sizeIn(minWidth = RefTheme.sizes.minTouchTarget, minHeight = RefTheme.sizes.minTouchTarget),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = environment,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = RefTheme.spacing.sm),
            )
        }
    }
}

@Preview
@Composable
private fun DebugMenuBadgePreview() {
    RefTheme { DebugMenuBadge(environment = "DEV", onClick = {}) }
}
