package com.studiodraug.refproj.app.navigation

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.navigation.HomeRoute
import com.studiodraug.refproj.core.navigation.Navigator
import com.studiodraug.refproj.core.navigation.ProfileRoute

/** Tabs of the logged-in shell, in display order. [route] is the root of the tab's own back stack. */
internal enum class MainTab(
    val route: NavKey,
) {
    Home(HomeRoute),
    Profile(ProfileRoute),
}

/**
 * One back stack per tab (each tab keeps its place when the user switches away), plus the selected
 * tab. It is the [Navigator] for every screen inside the shell: navigation happens in the selected tab.
 *
 * Back: pops the selected tab; from the root of any other tab it returns to [START]; from the root of
 * [START] it is not handled, so the platform leaves the app.
 */
@Stable
internal class MainTabsState(
    private val stacks: Map<MainTab, MutableList<NavKey>>,
    private val selectedIndex: MutableIntState,
) : Navigator {
    val selected: MainTab get() = MainTab.entries[selectedIndex.intValue]

    /** What the shell's `NavDisplay` shows: the start tab's stack, with the selected tab's stack on top. */
    val backStack: List<NavKey>
        get() = if (selected == START) stack(START) else stack(START) + stack(selected)

    /** Selecting the current tab again returns it to its root (platform convention on Android and iOS). */
    fun select(tab: MainTab) {
        if (tab == selected) {
            stack(tab).popToRoot()
        } else {
            selectedIndex.intValue = tab.ordinal
        }
    }

    override fun navigate(route: NavKey) {
        val stack = stack(selected)
        if (stack.lastOrNull() != route) stack.add(route)
    }

    override fun back(): Boolean {
        val stack = stack(selected)
        return when {
            stack.size > 1 -> {
                stack.removeAt(stack.lastIndex)
                true
            }

            selected != START -> {
                selectedIndex.intValue = START.ordinal
                true
            }

            else -> {
                false
            }
        }
    }

    override fun replaceAll(route: NavKey) {
        val stack = stack(selected)
        stack.clear()
        stack.add(route)
    }

    private fun stack(tab: MainTab): MutableList<NavKey> = stacks.getValue(tab)

    private fun MutableList<NavKey>.popToRoot() {
        while (size > 1) removeAt(lastIndex)
    }

    companion object {
        val START = MainTab.Home
    }
}
