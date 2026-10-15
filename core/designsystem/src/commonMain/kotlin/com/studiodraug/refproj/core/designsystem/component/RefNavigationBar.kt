package com.studiodraug.refproj.core.designsystem.component

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.icon.RefIcons
import com.studiodraug.refproj.core.designsystem.theme.RefTheme

/** One tab in a [RefNavigationBar]. */
@Immutable
data class RefNavigationItem(
    val label: String,
    val icon: ImageVector,
)

/** Bottom tab bar for the logged-in shell. The label doubles as the accessibility name. */
@Composable
fun RefNavigationBar(
    items: List<RefNavigationItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                // The label is always visible and already names the tab.
                icon = { Icon(imageVector = item.icon, contentDescription = null) },
                label = { Text(item.label) },
            )
        }
    }
}

@Preview
@Composable
private fun RefNavigationBarPreview() {
    RefTheme {
        RefNavigationBar(
            items =
                listOf(
                    RefNavigationItem("Home", RefIcons.Empty),
                    RefNavigationItem("Tasks", RefIcons.ChevronRight),
                    RefNavigationItem("Profile", RefIcons.Error),
                ),
            selectedIndex = 0,
            onSelect = {},
        )
    }
}
