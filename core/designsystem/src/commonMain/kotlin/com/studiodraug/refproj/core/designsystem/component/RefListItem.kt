package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.icon.RefIcons
import com.studiodraug.refproj.core.designsystem.theme.RefTheme

/** A row in a list. Shows a chevron and becomes clickable when [onClick] is set. */
@Composable
fun RefListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent =
            trailing ?: onClick?.let {
                {
                    // Decorative: the whole row is the click target and is announced as a button.
                    Icon(imageVector = RefIcons.ChevronRight, contentDescription = null)
                }
            },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

/** A row with a switch. The whole row toggles, so the touch target is large. */
@Composable
fun RefSwitchListItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    ListItem(
        headlineContent = { Text(title) },
        modifier =
            modifier.toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        supportingContent = subtitle?.let { { Text(it) } },
        // onCheckedChange = null: the row handles the toggle and its semantics.
        trailingContent = { Switch(checked = checked, onCheckedChange = null, enabled = enabled) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

/**
 * A row with a leading checkbox, e.g. a to-do item. The whole row toggles; [trailing] holds an
 * optional extra action (it keeps its own click target).
 */
@Composable
fun RefCheckboxListItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (checked) TextDecoration.LineThrough else null,
            )
        },
        modifier = modifier.toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        // onCheckedChange = null: the row handles the toggle and its semantics.
        leadingContent = { Checkbox(checked = checked, onCheckedChange = null) },
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

/** Thin separator between list rows or sections. */
@Composable
fun RefDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = RefTheme.sizes.divider,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Preview
@Composable
private fun RefListItemPreview() {
    RefTheme {
        RefSurface {
            Column {
                RefListItem(title = "Account", subtitle = "Name, e-mail", onClick = {})
                RefDivider()
                RefSwitchListItem(title = "Notifications", subtitle = "Daily reminder", checked = true, onCheckedChange = {})
                RefDivider()
                RefCheckboxListItem(title = "Buy milk", checked = false, onCheckedChange = {})
                RefCheckboxListItem(title = "Walk the dog", checked = true, onCheckedChange = {})
            }
        }
    }
}
