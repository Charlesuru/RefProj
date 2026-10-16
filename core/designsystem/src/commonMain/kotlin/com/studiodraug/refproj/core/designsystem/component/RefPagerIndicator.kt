package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.resources.Res
import com.studiodraug.refproj.core.designsystem.resources.designsystem_page_of
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import org.jetbrains.compose.resources.stringResource

/** Dots showing the current page of a pager. Announced as "Page X of Y". */
@Composable
fun RefPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.designsystem_page_of, currentPage + 1, pageCount)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(RefTheme.spacing.sm),
    ) {
        repeat(pageCount) { page ->
            val color = if (page == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            Box(modifier = Modifier.size(RefTheme.sizes.pagerIndicatorDot).background(color, CircleShape))
        }
    }
}

@Preview
@Composable
private fun RefPagerIndicatorPreview() {
    RefTheme { RefSurface { RefPagerIndicator(pageCount = 3, currentPage = 1) } }
}
