plugins {
    alias(libs.plugins.refproj.android.application)
}

android {
    namespace = "com.studiodraug.refproj.android"

    defaultConfig {
        applicationId = "com.studiodraug.refproj"
        // Set by fastlane from the git tag and build number (docs/adr/0005-release-tooling.md).
        versionCode = providers.gradleProperty("refproj.versionCode").orNull?.toInt() ?: 1
        versionName = providers.gradleProperty("refproj.versionName").orNull ?: "0.1.0"
    }

    // One flavor per environment. Each installs side-by-side (distinct applicationId)
    // so testers can run DEV, SIT, STAGE and PROD on the same device.
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "RefProj DEV")
            buildConfigField("String", "APP_ENVIRONMENT", "\"DEV\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api.dev.refproj.example\"")
        }
        create("sit") {
            dimension = "environment"
            applicationIdSuffix = ".sit"
            versionNameSuffix = "-sit"
            resValue("string", "app_name", "RefProj SIT")
            buildConfigField("String", "APP_ENVIRONMENT", "\"SIT\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api.sit.refproj.example\"")
        }
        create("stage") {
            dimension = "environment"
            applicationIdSuffix = ".stage"
            versionNameSuffix = "-stage"
            resValue("string", "app_name", "RefProj STAGE")
            buildConfigField("String", "APP_ENVIRONMENT", "\"STAGE\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api.stage.refproj.example\"")
        }
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "RefProj")
            buildConfigField("String", "APP_ENVIRONMENT", "\"PROD\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api.refproj.example\"")
        }
    }

    // Debug and release of the same environment share one applicationId, mirroring iOS
    // (one bundle ID per environment) and keeping Firebase at one app per env/platform.
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Signed with the upload key when configured (refproj.android.application, docs/secrets.md).
            // Never commit a keystore or its passwords.
        }
    }
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling)
}
