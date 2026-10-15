package com.studiodraug.refproj.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

// One key per destination. Keys are the only way features refer to each other's screens.
// Arguments are constructor properties: `data class TaskDetailRoute(val taskId: String) : NavKey`.

@Serializable
data object HomeRoute : NavKey

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
                    subclass(HomeRoute::class, HomeRoute.serializer())
                }
            }
    }
