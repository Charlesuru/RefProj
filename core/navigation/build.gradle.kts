// Route keys (one per screen), the Navigator abstraction and the serializers Navigation 3 needs on iOS.
// Features navigate only through these keys, never by referencing another feature.
plugins {
    alias(libs.plugins.refproj.kmp.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.navigation3.ui)
            api(libs.kotlinx.serialization.json)
        }
    }
}
