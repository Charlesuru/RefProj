// Debug menu for developers and testers. Bound by app:shared only while `AppConfig.isDebugMenuEnabled`
// (never in a PROD release build) — see ADR 0010.
plugins {
    alias(libs.plugins.refproj.kmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain)
            implementation(projects.core.config)
            implementation(projects.core.observability)
            implementation(libs.kotlinx.datetime)
        }
    }
}
