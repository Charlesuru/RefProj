import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

// Umbrella module: assembles all features into the root UI and the DI graph.
// It is the only module the iOS app links (as the `Shared` framework).
plugins {
    alias(libs.plugins.refproj.kmp.compose)
}

kotlin {
    val xcf = XCFramework("Shared")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
            // Swift implements these interfaces (Firebase bridges), so they must be visible in the framework.
            export(projects.core.config)
            export(projects.core.observability)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.config)
            api(projects.core.observability)
            implementation(projects.core.designsystem)
            implementation(projects.core.featureflags)
            implementation(projects.core.navigation)
            implementation(projects.core.network)
            implementation(projects.core.storage)
            implementation(projects.core.ui)
            implementation(projects.data)
            implementation(projects.domain)
            implementation(projects.feature.home)
            implementation(projects.feature.login)
            implementation(projects.feature.onboarding)
            implementation(projects.feature.profile)
            implementation(projects.feature.tasks)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
    }
}
