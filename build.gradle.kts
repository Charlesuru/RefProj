// Root build script. Plugins are declared here (apply false) so that every module
// resolves them from the same classloader. Module configuration lives in build-logic.

buildscript {
    dependencies {
        constraints {
            // Security pin for the build classpath (not shipped in the app): AGP 9.4.1 resolves a
            // vulnerable Bouncy Castle. Remove once AGP itself depends on >= 1.85. See libs.versions.toml.
            listOf(libs.bouncycastle.bcprov, libs.bouncycastle.bcpkix, libs.bouncycastle.bcutil).forEach {
                classpath(it) { because("GHSA-9pwp-9qqc-pr26, GHSA-qp49-qgx5-5m26, GHSA-c3fc-8qff-9hwx") }
            }
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.refproj.quality)
}
