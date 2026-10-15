package com.studiodraug.refproj.core.storage

import app.cash.turbine.test
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

// UNIT TEST
class SettingsKeyValueStoreTest {
    private val store = SettingsKeyValueStore(MapSettings())

    @Test
    fun putAndGet_roundTrips() {
        store.putString("name", "value")
        store.putBoolean("flag", true)

        assertEquals("value", store.getString("name"))
        assertTrue(store.getBoolean("flag", default = false))
        assertEquals(setOf("name", "flag"), store.keys())
    }

    @Test
    fun missingKeys_returnDefaults() {
        assertNull(store.getString("missing"))
        assertFalse(store.getBoolean("missing", default = false))
    }

    @Test
    fun observeBoolean_emitsCurrentValueThenChanges() =
        runTest {
            store.observeBoolean("flag", default = false).test {
                assertFalse(awaitItem())
                store.putBoolean("flag", true)
                assertTrue(awaitItem())
                store.remove("flag")
                assertFalse(awaitItem())
            }
        }

    @Test
    fun clear_removesEverything() {
        store.putString("a", "1")
        store.putString("b", "2")
        store.clear()
        assertTrue(store.keys().isEmpty())
    }
}
