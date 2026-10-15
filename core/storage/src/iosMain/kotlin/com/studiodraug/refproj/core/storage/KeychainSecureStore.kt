package com.studiodraug.refproj.core.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** [SecureStore] for iOS, backed by the Keychain (attributes in `platformStorageModule`). */
internal class KeychainSecureStore(
    private val keychain: Settings,
    private val ioDispatcher: CoroutineDispatcher,
) : SecureStore {
    override suspend fun get(key: String): String? = withContext(ioDispatcher) { keychain.getStringOrNull(key) }

    override suspend fun put(
        key: String,
        value: String,
    ) = withContext(ioDispatcher) { keychain.putString(key, value) }

    override suspend fun remove(key: String) = withContext(ioDispatcher) { keychain.remove(key) }

    override suspend fun clear() = withContext(ioDispatcher) { keychain.clear() }
}
