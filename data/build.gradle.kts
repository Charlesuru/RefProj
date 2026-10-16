// Repository implementations of the domain interfaces, backed by core:storage, core:network and Room.
plugins {
    alias(libs.plugins.refproj.kmp.library)
    alias(libs.plugins.refproj.kmp.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.domain)
            implementation(projects.core.network)
            implementation(projects.core.storage)
            implementation(projects.core.observability)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.ktor.client.mock)
        }
    }
}
