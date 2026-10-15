// Feature flags: per-environment defaults, remote values (Remote Config, later) and debug overrides.
plugins {
    alias(libs.plugins.refproj.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.config)
            implementation(projects.core.storage)
            api(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
    }
}
