// Root build script. Plugins are declared here (apply false) so that every module
// resolves them from the same classloader. Module configuration lives in build-logic.

buildscript {
    dependencies {
        constraints {
            // Security pins for the build classpath (not shipped in the app): AGP 9.4.1 resolves
            // vulnerable versions of these libraries. Advisories and removal rule: libs.versions.toml.
            // Tool classpaths inside the modules are pinned by build-logic (ToolClasspathPins.kt).
            libs.bundles.build.tool.security.pins.get().forEach {
                classpath(it) { because("Known vulnerabilities in the version AGP resolves; see libs.versions.toml") }
            }
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.refproj.quality)
    alias(libs.plugins.refproj.coverage)
}
