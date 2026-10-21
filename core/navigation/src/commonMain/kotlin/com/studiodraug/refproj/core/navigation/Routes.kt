package com.studiodraug.refproj.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

// One key per destination. Keys are the only way features refer to each other's screens.
// Arguments are constructor properties: `data class TaskDetailRoute(val taskId: String) : NavKey`.

// --- Top level: exactly one of these is on the root back stack, chosen by the app's state ---

@Serializable
data object OnboardingRoute : NavKey

@Serializable
data object LoginRoute : NavKey

/** The logged-in shell with the tab bar. Owned by `app:shared`. */
@Serializable
data object MainRoute : NavKey

// --- Tabs of the logged-in shell: the root of each tab's own back stack ---

@Serializable
data object HomeRoute : NavKey

@Serializable
data object TasksRoute : NavKey

@Serializable
data object ProfileRoute : NavKey

// --- Debug menu: pushed on top of the current top-level destination. Never installed in a PROD release build ---

/** Marks the debug menu's screens: the app hides the debug badge on them and does not report them to analytics. */
interface DebugDestination : NavKey

@Serializable
data object DebugRoute : DebugDestination

@Serializable
data object DebugLogsRoute : DebugDestination

@Serializable
data object DebugCatalogRoute : DebugDestination

/** One sample screen of the catalog, identified by its group title and name. */
@Serializable
data class DebugPreviewRoute(
    val group: String,
    val preview: String,
) : DebugDestination

/**
 * Lets Navigation 3 save and restore the back stack (process death on Android, state restoration
 * on iOS). Non-Android platforms have no reflection, so **every route must be registered here**;
 * `RoutesTest` fails for a route that is not.
 */
val NavSavedStateConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(OnboardingRoute::class, OnboardingRoute.serializer())
                    subclass(LoginRoute::class, LoginRoute.serializer())
                    subclass(MainRoute::class, MainRoute.serializer())
                    subclass(HomeRoute::class, HomeRoute.serializer())
                    subclass(TasksRoute::class, TasksRoute.serializer())
                    subclass(ProfileRoute::class, ProfileRoute.serializer())
                    subclass(DebugRoute::class, DebugRoute.serializer())
                    subclass(DebugLogsRoute::class, DebugLogsRoute.serializer())
                    subclass(DebugCatalogRoute::class, DebugCatalogRoute.serializer())
                    subclass(DebugPreviewRoute::class, DebugPreviewRoute.serializer())
                }
            }
    }
