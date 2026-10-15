package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.studiodraug.refproj.core.designsystem.theme.RefTheme

/** Background surface for screens and previews. */
@Composable
fun RefSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.background, content = content)
}

/**
 * Centres [content] and caps its width at [RefSizes.contentMaxWidth][com.studiodraug.refproj.core.designsystem.theme.RefSizes],
 * so screens stay readable on tablets and in landscape.
 */
@Composable
fun RefContentWidth(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier.widthIn(max = RefTheme.sizes.contentMaxWidth).fillMaxWidth(),
            content = content,
        )
    }
}
