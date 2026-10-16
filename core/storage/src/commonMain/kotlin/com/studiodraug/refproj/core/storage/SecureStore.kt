package com.studiodraug.refproj.core.storage

/**
 * Encrypted storage for secrets the app receives at runtime (session tokens, refresh tokens).
 *  - Android: AES-256-GCM, key held in the Android Keystore (`AndroidKeystoreSecureStore`).
 *  - iOS: Keychain, `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` (never synced or restored to another device).
 *
 * Values are never logged. A value that cannot be decrypted (e.g. the key was invalidated) reads as `null`.
 *
 * Failures never throw: a secure store that cannot be read must not crash the app. Reads then return
 * `null`, failed writes are reported as non-fatal errors (a session then lasts until the app restarts).
 */
interface SecureStore {
    suspend fun get(key: String): String?

    suspend fun put(
        key: String,
        value: String,
    )

    suspend fun remove(key: String)

    suspend fun clear()
}
