package com.studiodraug.refproj.app.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.studiodraug.refproj.app.shared.resources.Res
import com.studiodraug.refproj.app.shared.resources.app_tab_home
import com.studiodraug.refproj.app.shared.resources.app_tab_profile
import com.studiodraug.refproj.app.shared.resources.app_tab_tasks
import com.studiodraug.refproj.core.designsystem.component.RefNavigationBar
import com.studiodraug.refproj.core.designsystem.component.RefNavigationItem
import com.studiodraug.refproj.core.designsystem.icon.RefIcons
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import com.studiodraug.refproj.core.navigation.NavSavedStateConfiguration
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * The logged-in shell: tab bar + one back stack per tab ([MainTabsState]). Back stacks and the
 * selected tab survive configuration changes and process death.
 *
 * Known limitation: a tab's screens leave the `NavDisplay` while another tab is selected, so their
 * ViewModels are recreated on return. Screens rebuild their state from repositories, so nothing is lost.
 */
@Composable
internal fun MainShell(
    installers: List<NavEntryInstaller>,
    modifier: Modifier = Modifier,
) {
    val stacks = MainTab.entries.associateWith { tab -> rememberNavBackStack(NavSavedStateConfiguration, tab.route) }
    val selectedIndex = rememberSaveable(saver = MutableIntStateSaver) { mutableIntStateOf(MainTabsState.START.ordinal) }
    val tabs = remember(stacks, selectedIndex) { MainTabsState(stacks, selectedIndex) }

    val analytics = koinInject<AnalyticsTracker>()
    val currentRoute = tabs.backStack.last()
    LaunchedEffect(currentRoute) { analytics.logScreenView(screenName(currentRoute)) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            RefNavigationBar(
                items = MainTab.entries.map { RefNavigationItem(label = stringResource(it.label), icon = it.icon()) },
                selectedIndex = tabs.selected.ordinal,
                onSelect = { tabs.select(MainTab.entries[it]) },
            )
        },
        // Screens handle the top insets themselves (their top app bar); the tab bar handles the bottom.
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        NavDisplay(
            backStack = tabs.backStack,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            onBack = { tabs.back() },
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider =
                entryProvider {
                    installers.forEach { installer -> with(installer) { install(tabs) } }
                },
        )
    }
}

private val MainTab.label: StringResource
    get() =
        when (this) {
            MainTab.Home -> Res.string.app_tab_home
            MainTab.Tasks -> Res.string.app_tab_tasks
            MainTab.Profile -> Res.string.app_tab_profile
        }

@Composable
private fun MainTab.icon(): ImageVector =
    when (this) {
        MainTab.Home -> RefIcons.Home
        MainTab.Tasks -> RefIcons.Tasks
        MainTab.Profile -> RefIcons.Person
    }

private val MutableIntStateSaver = Saver<MutableIntState, Int>(save = { it.intValue }, restore = { mutableIntStateOf(it) })
