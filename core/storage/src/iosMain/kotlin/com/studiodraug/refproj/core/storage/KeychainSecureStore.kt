package com.studiodraug.refproj.core.storage

import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * [SecureStore] for iOS, backed by the Keychain (attributes in `platformStorageModule`).
 *
 * Keychain errors (thrown by `KeychainSettings` as [IllegalStateException] with the `OSStatus`, never
 * the value) are logged and swallowed, as the [SecureStore] contract requires. Example: `-34018` in
 * an unsigned build, which has no keychain entitlement.
 */
internal class KeychainSecureStore(
    private val keychain: Settings,
    private val ioDispatcher: CoroutineDispatcher,
) : SecureStore {
    private val log = Logger.withTag("SecureStore")

    override suspend fun get(key: String): String? = guarded("read", fallback = null) { keychain.getStringOrNull(key) }

    override suspend fun put(
        key: String,
        value: String,
    ) = guarded("write", fallback = Unit) { keychain.putString(key, value) }

    override suspend fun remove(key: String) = guarded("delete", fallback = Unit) { keychain.remove(key) }

    override suspend fun clear() = guarded("clear", fallback = Unit) { keychain.clear() }

    private suspend fun <T> guarded(
        operation: String,
        fallback: T,
        block: () -> T,
    ): T =
        withContext(ioDispatcher) {
            try {
                block()
            } catch (e: IllegalStateException) {
                log.e(e) { "Keychain $operation failed" }
                fallback
            }
        }
}
