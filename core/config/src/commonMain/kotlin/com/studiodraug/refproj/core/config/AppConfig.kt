package com.studiodraug.refproj.core.config

/**
 * Build environment. Selected at build time by the platform shell:
 *  - Android: product flavor (dev / sit / stage / prod)
 *  - iOS: Xcode build configuration via xcconfig (Debug-Dev, Release-Prod, ...)
 *
 * Shared code must never decide the environment itself — it is always injected.
 */
enum class AppEnvironment {
    DEV,
    SIT,
    STAGE,
    PROD,
    ;

    val isProduction: Boolean get() = this == PROD

    companion object {
        fun parse(value: String): AppEnvironment =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: error("Unknown environment '$value'. Expected one of ${entries.joinToString()}")
    }
}

/**
 * Immutable, non-secret runtime configuration supplied by the platform shell at startup.
 *
 * SECURITY: Only values that are safe to ship inside the binary belong here
 * (base URLs, public identifiers). Real secrets must never be embedded in the app.
 */
data class AppConfig(
    val environment: AppEnvironment,
    val apiBaseUrl: String,
    val versionName: String,
    val buildNumber: String,
    /** True for debuggable builds. Gates the debug menu and verbose logging. */
    val isDebugBuild: Boolean,
) {
    /**
     * Debug menu and debug-only tools (feature flag overrides, state jumps). Always available in
     * non-PROD environments and in debug builds; never in a PROD release build.
     */
    val isDebugMenuEnabled: Boolean get() = isDebugBuild || !environment.isProduction
}
