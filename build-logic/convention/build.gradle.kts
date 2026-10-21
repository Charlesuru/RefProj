plugins {
    `kotlin-dsl`
}

group = "com.studiodraug.refproj.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.spotless.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "refproj.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "refproj.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("kmpFeature") {
            id = "refproj.kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
        register("kmpRoom") {
            id = "refproj.kmp.room"
            implementationClass = "KmpRoomConventionPlugin"
        }
        register("androidApplication") {
            id = "refproj.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("coverage") {
            id = "refproj.coverage"
            implementationClass = "CoverageConventionPlugin"
        }
        register("quality") {
            id = "refproj.quality"
            implementationClass = "QualityConventionPlugin"
        }
    }
}
