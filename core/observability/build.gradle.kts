plugins {
    alias(libs.plugins.refproj.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kermit)
            implementation(projects.core.config)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
    }
}
