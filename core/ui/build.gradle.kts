// Shared UI infrastructure that is not visual design: the MVVM base class and event helpers.
plugins {
    alias(libs.plugins.refproj.kmp.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.lifecycle.viewmodel.compose)
            api(libs.androidx.lifecycle.runtime.compose)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
    }
}
