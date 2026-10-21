package com.studiodraug.refproj.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.security.KeyStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith

// INTEGRATION TEST

/**
 * The real Android Keystore and AES-GCM on a device or emulator: Robolectric has no AndroidKeyStore provider.
 * Runs in the test APK's own sandbox, so the Keystore key and preferences are not the app's.
 */
@RunWith(AndroidJUnit4::class)
class AndroidKeystoreSecureStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var preferences: SharedPreferences
    private lateinit var store: AndroidKeystoreSecureStore

    @BeforeTest
    fun setUp() {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        store = AndroidKeystoreSecureStore(preferences, Dispatchers.IO)
    }

    @AfterTest
    fun tearDown() {
        preferences.edit().clear().commit()
    }

    @Test
    fun values_roundTrip_andAreEncryptedAtRest() =
        runTest {
            store.put("session", SECRET)

            assertEquals(SECRET, store.get("session"))
            val stored = requireNotNull(preferences.getString("session", null))
            assertFalse(stored.contains(SECRET), "the plaintext must never be stored")
        }

    @Test
    fun aCiphertextCopiedToAnotherKey_doesNotDecrypt_andIsDropped() =
        runTest {
            store.put("session", SECRET)
            preferences.edit().putString("other", preferences.getString("session", null)).commit()

            assertNull(store.get("other"))
            assertFalse(preferences.contains("other"))
        }

    @Test
    fun aLostKeystoreKey_makesValuesUnreadable_withoutCrashing() =
        runTest {
            store.put("session", SECRET)
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(KEY_ALIAS)

            assertNull(store.get("session"))
            store.put("session", "new")
            assertEquals("new", store.get("session"), "a new key is created on the next write")
        }

    @Test
    fun removeAndClear_deleteValues() =
        runTest {
            store.put("a", "1")
            store.put("b", "2")
            store.remove("a")
            assertNull(store.get("a"))
            assertEquals("2", store.get("b"))
            store.clear()
            assertNull(store.get("b"))
        }

    private companion object {
        const val PREFERENCES = "secure-store-device-test"
        const val SECRET = "token-1234567890"

        /** Same alias as AndroidKeystoreSecureStore; the test APK has its own Keystore namespace. */
        const val KEY_ALIAS = "refproj_secure_store_v1"
    }
}
