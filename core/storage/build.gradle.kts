plugins {
    alias(libs.plugins.refproj.kmp.library)
    alias(libs.plugins.refproj.kmp.device.test)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)
            implementation(libs.kotlinx.coroutines.core)
            implementation(projects.core.observability)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(libs.multiplatform.settings.test)
            implementation(libs.turbine)
        }
    }
}
