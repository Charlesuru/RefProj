package com.studiodraug.refproj.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.featureflags.FeatureFlag
import com.studiodraug.refproj.core.featureflags.FeatureFlags
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val environmentName: String,
    /** Shown only while [FeatureFlag.HOME_BUILD_INFO] is on. */
    val version: String?,
)

/** Placeholder home: shows which build is running. Replaced by the real home tab in step 5. */
class HomeViewModel(
    private val config: AppConfig,
    featureFlags: FeatureFlags,
) : ViewModel() {
    private val version = "${config.versionName} (${config.buildNumber})"

    private fun state(showBuildInfo: Boolean) =
        HomeUiState(environmentName = config.environment.name, version = version.takeIf { showBuildInfo })

    val state: StateFlow<HomeUiState> =
        featureFlags
            .observe(FeatureFlag.HOME_BUILD_INFO)
            .map(::state)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = state(featureFlags.isEnabled(FeatureFlag.HOME_BUILD_INFO)),
            )

    private companion object {
        /** Keeps the flow alive through a configuration change. */
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
