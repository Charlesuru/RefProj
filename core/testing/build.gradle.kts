// Test doubles and helpers shared by the test source sets of every module.
// Only ever added as a test dependency, never to a main source set.
plugins {
    alias(libs.plugins.refproj.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.config)
            api(projects.core.observability)
            api(projects.core.storage)
            api(libs.multiplatform.settings.test)
            api(libs.kotlinx.coroutines.test)
            api(libs.kotlin.test)
            api(libs.turbine)
        }
        androidMain.dependencies {
            // PlatformTest: Robolectric runner and the application Context for host tests.
            api(libs.junit4)
            api(libs.robolectric)
            api(libs.androidx.test.core)
        }
    }
}
