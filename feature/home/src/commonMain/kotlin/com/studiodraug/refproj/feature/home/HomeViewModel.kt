package com.studiodraug.refproj.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.featureflags.FeatureFlag
import com.studiodraug.refproj.core.featureflags.FeatureFlags
import com.studiodraug.refproj.domain.auth.ObserveCurrentUserUseCase
import com.studiodraug.refproj.domain.tasks.ObserveTaskSummaryUseCase
import com.studiodraug.refproj.domain.tasks.TaskSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    /** `null` until the user is known; the greeting is hidden meanwhile. */
    val displayName: String? = null,
    val summary: TaskSummary = TaskSummary(open = 0, done = 0),
    val environmentName: String,
    /** Shown only while [FeatureFlag.HOME_BUILD_INFO] is on. */
    val version: String? = null,
)

/** Greeting, task counts and (behind a flag) which build is running. Read-only: no actions. */
class HomeViewModel(
    private val config: AppConfig,
    featureFlags: FeatureFlags,
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeTaskSummary: ObserveTaskSummaryUseCase,
) : ViewModel() {
    private val version = "${config.versionName} (${config.buildNumber})"

    val state: StateFlow<HomeUiState> =
        combine(
            observeCurrentUser(),
            observeTaskSummary(),
            featureFlags.observe(FeatureFlag.HOME_BUILD_INFO),
        ) { user, summary, showBuildInfo ->
            HomeUiState(
                displayName = user?.displayName,
                summary = summary,
                environmentName = config.environment.name,
                version = version.takeIf { showBuildInfo },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue =
                HomeUiState(
                    environmentName = config.environment.name,
                    version = version.takeIf { featureFlags.isEnabled(FeatureFlag.HOME_BUILD_INFO) },
                ),
        )

    private companion object {
        /** Keeps the flow alive through a configuration change. */
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
