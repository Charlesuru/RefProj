package com.studiodraug.refproj.feature.debug

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.navigation.DebugCatalogRoute
import com.studiodraug.refproj.core.navigation.DebugLogsRoute
import com.studiodraug.refproj.core.navigation.DebugPreviewRoute
import com.studiodraug.refproj.core.navigation.DebugRoute
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import com.studiodraug.refproj.core.navigation.Navigator
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin bindings and navigation entries of the debug menu.
 *
 * SECURITY: `app:shared` adds this module only while `AppConfig.isDebugMenuEnabled` — never in a PROD
 * release build, so there the debug routes do not exist at all (ADR 0010).
 */
val debugModule: Module =
    module {
        factory { EnvironmentInfo.from(get<AppConfig>(), deviceDescription()) }
        factoryOf(::AppStateShortcuts)
        viewModelOf(::DebugViewModel)
        viewModel { DebugLogsViewModel(get()) }
        single<ScreenPreviewGroup>(named("designsystem")) { designSystemPreviews }
        single { ScreenCatalog(getAll()) }
        viewModelOf(::DebugCatalogViewModel)
        single<NavEntryInstaller>(named("debug")) { NavEntryInstaller { navigator -> debugEntries(navigator) } }
    }

private fun EntryProviderScope<NavKey>.debugEntries(navigator: Navigator) {
    entry<DebugRoute> {
        val viewModel = koinViewModel<DebugViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        DebugScreen(
            state = state,
            onAction = viewModel::onAction,
            onOpenLogs = { navigator.navigate(DebugLogsRoute) },
            onOpenCatalog = { navigator.navigate(DebugCatalogRoute) },
            onBack = { navigator.back() },
        )
    }
    entry<DebugLogsRoute> {
        val viewModel = koinViewModel<DebugLogsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        DebugLogsScreen(state = state, onAction = viewModel::onAction, onBack = { navigator.back() })
    }
    entry<DebugCatalogRoute> {
        val viewModel = koinViewModel<DebugCatalogViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        DebugCatalogScreen(
            state = state,
            onOpenPreview = { group, preview -> navigator.navigate(DebugPreviewRoute(group, preview)) },
            onBack = { navigator.back() },
        )
    }
    // A container for a stateless sample screen: no ViewModel of its own.
    entry<DebugPreviewRoute> { route ->
        val catalog = koinInject<ScreenCatalog>()
        DebugPreviewScreen(
            title = "${route.group} · ${route.preview}",
            preview = remember(route) { catalog.find(route.group, route.preview) },
            onBack = { navigator.back() },
        )
    }
}
