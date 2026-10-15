package com.studiodraug.refproj.core.featureflags

import kotlinx.coroutines.flow.Flow

/** Read access to feature flags. Inject this into ViewModels and use cases. */
interface FeatureFlags {
    fun isEnabled(flag: FeatureFlag): Boolean

    /** Emits the current value and every change (remote fetch, debug override). */
    fun observe(flag: FeatureFlag): Flow<Boolean>

    /** Every flag with its value and where the value comes from. For the debug menu. */
    fun observeStates(): Flow<List<FeatureFlagState>>
}

/** Debug-menu write access. Ignored in PROD release builds (see `AppConfig.isDebugMenuEnabled`). */
interface FeatureFlagOverrides {
    /** `null` removes the override, so the flag falls back to its remote or default value. */
    fun setOverride(
        flag: FeatureFlag,
        enabled: Boolean?,
    )

    fun clearOverrides()
}

data class FeatureFlagState(
    val flag: FeatureFlag,
    val enabled: Boolean,
    val source: Source,
) {
    enum class Source { DEFAULT, REMOTE, OVERRIDE }
}
