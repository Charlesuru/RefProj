package com.studiodraug.refproj.feature.home

import androidx.lifecycle.ViewModel
import com.studiodraug.refproj.core.config.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val environmentName: String,
    val version: String,
)

/** Placeholder home: shows which build is running. Replaced by the real home tab in step 5. */
class HomeViewModel(
    config: AppConfig,
) : ViewModel() {
    private val mutableState =
        MutableStateFlow(
            HomeUiState(
                environmentName = config.environment.name,
                version = "${config.versionName} (${config.buildNumber})",
            ),
        )
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()
}
