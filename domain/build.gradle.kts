// Pure Kotlin business logic: use cases, models and repository interfaces.
// No Compose, no platform APIs, no networking, no DI framework (wired in app:shared).
plugins {
    alias(libs.plugins.refproj.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.turbine)
        }
    }
}
