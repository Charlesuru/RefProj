plugins {
    alias(libs.plugins.refproj.kmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain)
            implementation(projects.core.observability)
        }
    }
}
