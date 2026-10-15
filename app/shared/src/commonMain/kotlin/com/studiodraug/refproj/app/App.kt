package com.studiodraug.refproj.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.navigation.BackStackNavigator
import com.studiodraug.refproj.core.navigation.HomeRoute
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import com.studiodraug.refproj.core.navigation.NavSavedStateConfiguration
import org.koin.compose.getKoin

/**
 * Root composable shared by Android and iOS: theme + the Navigation 3 back stack.
 * Each feature contributes its screens through a [NavEntryInstaller] bound in Koin.
 */
@Composable
fun App() {
    RefTheme {
        val backStack = rememberNavBackStack(NavSavedStateConfiguration, HomeRoute)
        val navigator = remember(backStack) { BackStackNavigator(backStack) }
        val koin = getKoin()
        val installers = remember(koin) { koin.getAll<NavEntryInstaller>() }

        NavDisplay(
            backStack = backStack,
            onBack = { navigator.back() },
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    // Scopes each screen's ViewModel to its back stack entry.
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider =
                entryProvider {
                    installers.forEach { installer -> with(installer) { install(navigator) } }
                },
        )
    }
}
