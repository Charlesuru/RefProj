package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.theme.RefTheme

/** Groups related content. Clickable when [onClick] is set. */
@Composable
fun RefCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    val elevation = CardDefaults.cardElevation(defaultElevation = RefTheme.elevation.low)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, colors = colors, elevation = elevation) {
            CardBody(content)
        }
    } else {
        Card(modifier = modifier, colors = colors, elevation = elevation) {
            CardBody(content)
        }
    }
}

@Composable
private fun CardBody(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.padding(RefTheme.spacing.lg),
        content = content,
    )
}

@Preview
@Composable
private fun RefCardPreview() {
    RefTheme {
        RefSurface {
            RefCard(modifier = Modifier.padding(RefTheme.spacing.lg)) {
                Text("Card title", style = MaterialTheme.typography.titleMedium)
                Text("Supporting text inside a card.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
