package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.icon.RefIcons
import com.studiodraug.refproj.core.designsystem.resources.Res
import com.studiodraug.refproj.core.designsystem.resources.designsystem_back
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Screen title bar. Shows a back button when [onBack] is set.
 */
@OptIn(ExperimentalMaterial3Api::class) // TopAppBar is still experimental in Material 3; stable in practice.
@Composable
fun RefTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = modifier,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = RefIcons.ArrowBack, contentDescription = stringResource(Res.string.designsystem_back))
                }
            }
        },
        actions = actions,
    )
}

@Preview
@Composable
private fun RefTopAppBarPreview() {
    RefTheme { RefTopAppBar(title = "Settings", onBack = {}) }
}
