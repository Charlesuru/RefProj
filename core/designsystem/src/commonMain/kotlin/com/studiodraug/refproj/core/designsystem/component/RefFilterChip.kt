package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.theme.RefTheme

/** One option of a filter, e.g. a log level. Use a row of them where exactly one or several can be selected. */
@Composable
fun RefFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(text) }, modifier = modifier)
}

@Preview
@Composable
private fun RefFilterChipPreview() {
    RefTheme {
        RefSurface {
            Row(modifier = Modifier.padding(RefTheme.spacing.lg), horizontalArrangement = Arrangement.spacedBy(RefTheme.spacing.sm)) {
                RefFilterChip(text = "Info", selected = true, onClick = {})
                RefFilterChip(text = "Error", selected = false, onClick = {})
            }
        }
    }
}
