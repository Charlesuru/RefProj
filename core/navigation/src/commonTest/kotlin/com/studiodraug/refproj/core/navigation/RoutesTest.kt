package com.studiodraug.refproj.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json

// UNIT TEST

/** Every route must survive save/restore on every platform (see [NavSavedStateConfiguration]). */
class RoutesTest {
    private val json = Json { serializersModule = NavSavedStateConfiguration.serializersModule }
    private val serializer = PolymorphicSerializer(NavKey::class)

    // Add every new route here as well.
    private val allRoutes: List<NavKey> = listOf(OnboardingRoute, LoginRoute, MainRoute, HomeRoute, TasksRoute, ProfileRoute)

    @Test
    fun everyRoute_roundTripsThroughTheRegisteredSerializers() {
        allRoutes.forEach { route ->
            val restored = json.decodeFromString(serializer, json.encodeToString(serializer, route))
            assertEquals(route, restored)
        }
    }
}
