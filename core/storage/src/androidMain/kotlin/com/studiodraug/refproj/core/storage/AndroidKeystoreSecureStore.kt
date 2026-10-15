package com.studiodraug.refproj.core.storage

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * [SecureStore] for Android: values are encrypted with AES-256-GCM using a key that never leaves
 * the Android Keystore, and stored as `base64(iv || ciphertext)` in a private SharedPreferences file.
 * The entry key is bound as associated data, so a ciphertext copied to another key fails to decrypt.
 *
 * The SharedPreferences file is excluded from backup (data_extraction_rules.xml, allowBackup=false);
 * a restored copy would be useless anyway because Keystore keys are not backed up.
 */
internal class AndroidKeystoreSecureStore(
    private val preferences: SharedPreferences,
    private val ioDispatcher: CoroutineDispatcher,
) : SecureStore {
    override suspend fun get(key: String): String? =
        withContext(ioDispatcher) {
            val stored = preferences.getString(key, null) ?: return@withContext null
            runCatchingUnrecoverable { decrypt(key, stored) }
                ?: run {
                    // Key invalidated or data corrupted: the value is unrecoverable, drop it.
                    preferences.edit().remove(key).apply()
                    null
                }
        }

    override suspend fun put(
        key: String,
        value: String,
    ) = withContext(ioDispatcher) {
        preferences.edit().putString(key, encrypt(key, value)).apply()
    }

    override suspend fun remove(key: String) =
        withContext(ioDispatcher) {
            preferences.edit().remove(key).apply()
        }

    override suspend fun clear() =
        withContext(ioDispatcher) {
            preferences.edit().clear().apply()
        }

    private fun encrypt(
        key: String,
        plaintext: String,
    ): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        cipher.updateAAD(key.encodeToByteArray())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.encodeToByteArray())
        return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(
        key: String,
        stored: String,
    ): String {
        val bytes = Base64.decode(stored, Base64.NO_WRAP)
        require(bytes.size > IV_LENGTH_BYTES) { "Stored value is too short" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_LENGTH_BITS, bytes, 0, IV_LENGTH_BYTES))
        cipher.updateAAD(key.encodeToByteArray())
        return cipher.doFinal(bytes, IV_LENGTH_BYTES, bytes.size - IV_LENGTH_BYTES).decodeToString()
    }

    /** `null` for the failures that mean "this ciphertext can never be read again". */
    private inline fun runCatchingUnrecoverable(block: () -> String): String? =
        try {
            block()
        } catch (_: GeneralSecurityException) {
            null
        } catch (_: IllegalArgumentException) {
            null // invalid Base64 or truncated value
        }

    @Synchronized
    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val spec =
            KeyGenParameterSpec
                .Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .setRandomizedEncryptionRequired(true) // the Keystore picks a fresh IV for every encryption
                .build()
        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "refproj_secure_store_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val TAG_LENGTH_BITS = 128
        const val IV_LENGTH_BYTES = 12
    }
}
