package com.studiodraug.refproj.feature.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.studiodraug.refproj.core.designsystem.component.RefButton
import com.studiodraug.refproj.core.designsystem.component.RefButtonStyle
import com.studiodraug.refproj.core.designsystem.component.RefCard
import com.studiodraug.refproj.core.designsystem.component.RefCheckboxListItem
import com.studiodraug.refproj.core.designsystem.component.RefDivider
import com.studiodraug.refproj.core.designsystem.component.RefEmptyState
import com.studiodraug.refproj.core.designsystem.component.RefErrorState
import com.studiodraug.refproj.core.designsystem.component.RefFilterChip
import com.studiodraug.refproj.core.designsystem.component.RefListItem
import com.studiodraug.refproj.core.designsystem.component.RefLoadingState
import com.studiodraug.refproj.core.designsystem.component.RefSurface
import com.studiodraug.refproj.core.designsystem.component.RefSwitchListItem
import com.studiodraug.refproj.core.designsystem.component.RefTextField
import com.studiodraug.refproj.core.designsystem.component.RefTextFieldType
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.ui.ScreenPreview
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup

/** The design system's tokens and components, for the screen catalog. Sample texts are developer-facing. */
internal val designSystemPreviews =
    ScreenPreviewGroup(
        title = "Design system",
        previews =
            listOf(
                ScreenPreview("Colors") { Gallery { ColorTokens() } },
                ScreenPreview("Typography") { Gallery { TypographyTokens() } },
                ScreenPreview("Buttons and inputs") { Gallery { ButtonsAndInputs() } },
                ScreenPreview("Lists and cards") { Gallery { ListsAndCards() } },
                ScreenPreview("Loading state") { RefSurface(Modifier.fillMaxSize()) { RefLoadingState() } },
                ScreenPreview("Empty state") {
                    RefSurface(Modifier.fillMaxSize()) {
                        RefEmptyState(
                            title = "No tasks yet",
                            message = "Tasks you add show up here.",
                            actionText = "Add task",
                            onAction = {},
                        )
                    }
                },
                ScreenPreview("Error state") {
                    RefSurface(Modifier.fillMaxSize()) { RefErrorState(message = "Check your connection and try again.", onRetry = {}) }
                },
            ),
    )

/** A scrolling page with screen padding; [content] lays out its own items in a [Section]. */
@Composable
private fun Gallery(content: @Composable () -> Unit) {
    RefSurface(Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RefTheme.spacing.screenHorizontal, vertical = RefTheme.spacing.lg),
        ) { content() }
    }
}

@Composable
private fun Section(content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(RefTheme.spacing.md), content = content)
}

@Composable
private fun ColorTokens() {
    Section {
        val scheme = MaterialTheme.colorScheme
        listOf(
            "primary" to scheme.primary,
            "onPrimary" to scheme.onPrimary,
            "secondary" to scheme.secondary,
            "tertiary" to scheme.tertiary,
            "error" to scheme.error,
            "background" to scheme.background,
            "surface" to scheme.surface,
            "surfaceContainerLow" to scheme.surfaceContainerLow,
            "onSurface" to scheme.onSurface,
            "onSurfaceVariant" to scheme.onSurfaceVariant,
            "outline" to scheme.outline,
            "outlineVariant" to scheme.outlineVariant,
        ).forEach { (name, color) -> ColorSwatch(name, color) }
    }
}

@Composable
private fun ColorSwatch(
    name: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(RefTheme.spacing.md)) {
        Box(
            modifier =
                Modifier
                    .size(RefTheme.sizes.minTouchTarget)
                    .background(color, MaterialTheme.shapes.small),
        )
        Text(text = name, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TypographyTokens() {
    Section {
        val type = MaterialTheme.typography
        listOf(
            "displaySmall" to type.displaySmall,
            "headlineMedium" to type.headlineMedium,
            "titleLarge" to type.titleLarge,
            "titleMedium" to type.titleMedium,
            "titleSmall" to type.titleSmall,
            "bodyLarge" to type.bodyLarge,
            "bodyMedium" to type.bodyMedium,
            "bodySmall" to type.bodySmall,
            "labelLarge" to type.labelLarge,
            "labelSmall" to type.labelSmall,
        ).forEach { (name, style) -> TypeSample(name, style) }
    }
}

@Composable
private fun TypeSample(
    name: String,
    style: TextStyle,
) {
    Text(text = name, style = style)
}

@Composable
private fun ButtonsAndInputs() {
    Section {
        RefButtonStyle.entries.forEach { style ->
            RefButton(text = style.name, onClick = {}, modifier = Modifier.fillMaxWidth(), style = style)
        }
        RefButton(text = "Loading", onClick = {}, modifier = Modifier.fillMaxWidth(), loading = true)
        RefButton(text = "Disabled", onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false)
        RefTextField(value = "ada@example.com", onValueChange = {}, label = "E-mail", type = RefTextFieldType.Email)
        RefTextField(value = "secret", onValueChange = {}, label = "Password", type = RefTextFieldType.Password)
        RefTextField(value = "ada", onValueChange = {}, label = "With error", error = "Enter a valid e-mail address")
        Row(horizontalArrangement = Arrangement.spacedBy(RefTheme.spacing.sm)) {
            RefFilterChip(text = "Selected", selected = true, onClick = {})
            RefFilterChip(text = "Not selected", selected = false, onClick = {})
        }
    }
}

@Composable
private fun ListsAndCards() {
    Section {
        RefCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Card title", style = MaterialTheme.typography.titleMedium)
            Text(text = "Cards group related content.", style = MaterialTheme.typography.bodyMedium)
        }
        Column {
            RefListItem(title = "List item", subtitle = "With a subtitle", onClick = {})
            RefDivider()
            RefSwitchListItem(title = "Switch item", checked = true, onCheckedChange = {})
            RefDivider()
            RefCheckboxListItem(title = "Open task", checked = false, onCheckedChange = {})
            RefCheckboxListItem(title = "Done task", checked = true, onCheckedChange = {})
        }
    }
}
