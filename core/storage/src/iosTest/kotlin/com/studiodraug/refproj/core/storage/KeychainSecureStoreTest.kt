package com.studiodraug.refproj.core.storage

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

// UNIT TEST

/**
 * The SecureStore contract on iOS: Keychain errors never crash the app. The real Keychain cannot be used
 * here — the simulator test process has no app host or entitlement (`-25291` / `-34018`) — so the
 * Keychain is replaced by one that fails like it or works like it.
 */
class KeychainSecureStoreTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun keychainErrors_neverThrow_andReadAsMissing() =
        runTest(dispatcher) {
            val store = KeychainSecureStore(FailingKeychain(), dispatcher)

            store.put("session", "token")
            assertNull(store.get("session"))
            store.remove("session")
            store.clear()
        }

    @Test
    fun workingKeychain_storesReadsAndRemovesValues() =
        runTest(dispatcher) {
            val store = KeychainSecureStore(MapSettings(), dispatcher)

            store.put("session", "token")
            assertEquals("token", store.get("session"))
            store.remove("session")
            assertNull(store.get("session"))
            store.put("a", "1")
            store.clear()
            assertNull(store.get("a"))
        }

    /** Throws like `KeychainSettings` does: an IllegalStateException carrying the OSStatus. */
    private class FailingKeychain : Settings by MapSettings() {
        private fun fail(): Nothing = error("Keychain error -34018: A required entitlement isn't present.")

        override fun getStringOrNull(key: String): String? = fail()

        override fun putString(
            key: String,
            value: String,
        ): Unit = fail()

        override fun remove(key: String): Unit = fail()

        override fun clear(): Unit = fail()
    }
}
