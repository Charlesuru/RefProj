package com.studiodraug.refproj.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.studiodraug.refproj.app.navigation.MainShell
import com.studiodraug.refproj.app.navigation.screenName
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.designsystem.component.RefSurface
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.navigation.BackStackNavigator
import com.studiodraug.refproj.core.navigation.DebugDestination
import com.studiodraug.refproj.core.navigation.DebugRoute
import com.studiodraug.refproj.core.navigation.MainRoute
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.feature.debug.DebugMenuBadge
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable shared by Android and iOS: theme + the top-level destination chosen by
 * [AppViewModel] (onboarding, login or the logged-in shell). Each feature contributes its screens
 * through a [NavEntryInstaller] bound in Koin.
 */
@Composable
fun App(modifier: Modifier = Modifier) {
    RefTheme {
        val viewModel = koinViewModel<AppViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        when (val current = state) {
            AppUiState.Loading -> RefSurface(modifier = modifier.fillMaxSize()) {}
            is AppUiState.Ready -> RootNavigation(destination = current.destination, modifier = modifier)
        }
    }
}

/**
 * The root back stack starts over whenever the destination changes: it is derived from the app's
 * state, so login, logout and finishing onboarding never push or pop it. Screens pushed on top of a
 * destination (none yet) are not restored after process death; the destination always is.
 */
@Composable
private fun RootNavigation(
    destination: NavKey,
    modifier: Modifier = Modifier,
) {
    val backStack = remember(destination) { mutableStateListOf(destination) }
    val navigator = remember(backStack) { BackStackNavigator(backStack) }
    val koin = getKoin()
    val installers = remember(koin) { koin.getAll<NavEntryInstaller>() }

    val analytics = koinInject<AnalyticsTracker>()
    val config = koinInject<AppConfig>()
    val currentRoute = backStack.last()
    // The shell reports its own screens; the debug menu is not product usage.
    LaunchedEffect(currentRoute) {
        if (currentRoute != MainRoute && currentRoute !is DebugDestination) analytics.logScreenView(screenName(currentRoute))
    }

    Box(modifier = modifier) {
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
                    entry<MainRoute> { MainShell(installers = installers) }
                },
        )
        // Never in a PROD release build: there the debug module and its routes are not even bound.
        if (config.isDebugMenuEnabled && currentRoute !is DebugDestination) {
            DebugMenuBadge(
                environment = config.environment.name,
                onClick = { navigator.navigate(DebugRoute) },
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)),
            )
        }
    }
}
