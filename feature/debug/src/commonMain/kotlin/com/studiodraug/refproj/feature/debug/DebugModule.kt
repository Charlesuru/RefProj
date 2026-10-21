package com.studiodraug.refproj.feature.debug

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.navigation.DebugLogsRoute
import com.studiodraug.refproj.core.navigation.DebugRoute
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import com.studiodraug.refproj.core.navigation.Navigator
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
            onBack = { navigator.back() },
        )
    }
    entry<DebugLogsRoute> {
        val viewModel = koinViewModel<DebugLogsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        DebugLogsScreen(state = state, onAction = viewModel::onAction, onBack = { navigator.back() })
    }
}
