package com.studiodraug.refproj.core.featureflags

import com.studiodraug.refproj.core.config.AppEnvironment
import com.studiodraug.refproj.core.config.AppEnvironment.DEV
import com.studiodraug.refproj.core.config.AppEnvironment.SIT
import com.studiodraug.refproj.core.config.AppEnvironment.STAGE

/**
 * Every feature flag in the app (trunk-based development, `CLAUDE.md` §1).
 *
 * Rules:
 *  - Unfinished work ships behind a flag that is **off in PROD** ([enabledByDefaultIn] never contains PROD;
 *    `FeatureFlagTest` enforces it). PROD turns a feature on remotely, never by changing the default.
 *  - [key] is stable: it is the Remote Config parameter name and the debug override key.
 *  - Remove the flag and the dead code path once the feature is fully rolled out.
 */
enum class FeatureFlag(
    val key: String,
    val description: String,
    val enabledByDefaultIn: Set<AppEnvironment>,
) {
    HOME_BUILD_INFO(
        key = "home_build_info",
        description = "Show version and build number on the home screen",
        enabledByDefaultIn = setOf(DEV, SIT, STAGE),
    ),
    ;

    fun isEnabledByDefault(environment: AppEnvironment): Boolean = environment in enabledByDefaultIn
}
