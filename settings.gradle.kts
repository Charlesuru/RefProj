pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Modules may not declare their own repositories — keeps the supply chain auditable.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "RefProj"

// --- App (entry points) ---
include(":app:android")
include(":app:shared")

// --- Core (cross-cutting infrastructure) ---
include(":core:config")
include(":core:designsystem")
include(":core:featureflags")
include(":core:navigation")
include(":core:network")
include(":core:observability")
include(":core:storage")
include(":core:testing")
include(":core:ui")

// --- Domain (pure Kotlin business logic) and data (repositories) ---
include(":domain")
include(":data")

// --- Features ---
include(":feature:home")
