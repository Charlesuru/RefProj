package com.studiodraug.refproj.feature.onboarding

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import com.studiodraug.refproj.core.navigation.OnboardingRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin bindings and navigation entries of the onboarding feature. */
val onboardingModule: Module =
    module {
        viewModelOf(::OnboardingViewModel)
        single<NavEntryInstaller>(named("onboarding")) { NavEntryInstaller { onboardingEntries() } }
    }

private fun EntryProviderScope<NavKey>.onboardingEntries() {
    entry<OnboardingRoute> {
        val viewModel = koinViewModel<OnboardingViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        OnboardingScreen(state = state, onAction = viewModel::onAction)
    }
}
