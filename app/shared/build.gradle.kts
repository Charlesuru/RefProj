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
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.config)
            implementation(projects.core.designsystem)
            implementation(projects.feature.home)
        }
    }
}
