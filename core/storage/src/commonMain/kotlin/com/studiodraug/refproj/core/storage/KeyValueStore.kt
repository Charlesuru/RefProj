package com.studiodraug.refproj.core.storage

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import kotlinx.coroutines.flow.Flow

/**
 * Small, non-sensitive key-value data: preferences, "onboarding completed", debug overrides.
 * Backed by SharedPreferences (Android) and NSUserDefaults (iOS).
 *
 * SECURITY: never store tokens, passwords or personal data here — use [SecureStore].
 */
interface KeyValueStore {
    fun getString(key: String): String?

    fun putString(
        key: String,
        value: String,
    )

    fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean

    fun putBoolean(
        key: String,
        value: Boolean,
    )

    fun observeString(key: String): Flow<String?>

    fun observeBoolean(
        key: String,
        default: Boolean,
    ): Flow<Boolean>

    fun remove(key: String)

    /** Keys currently stored. Used to clear one namespace (e.g. all debug overrides). */
    fun keys(): Set<String>

    fun clear()
}

/** [KeyValueStore] on top of multiplatform-settings. */
@OptIn(ExperimentalSettingsApi::class) // Flow extensions; stable in practice since 1.0.
class SettingsKeyValueStore(
    private val settings: ObservableSettings,
) : KeyValueStore {
    override fun getString(key: String): String? = settings.getStringOrNull(key)

    override fun putString(
        key: String,
        value: String,
    ) = settings.putString(key, value)

    override fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean = settings.getBoolean(key, default)

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) = settings.putBoolean(key, value)

    override fun observeString(key: String): Flow<String?> = settings.getStringOrNullFlow(key)

    override fun observeBoolean(
        key: String,
        default: Boolean,
    ): Flow<Boolean> = settings.getBooleanFlow(key, default)

    override fun remove(key: String) = settings.remove(key)

    override fun keys(): Set<String> = settings.keys

    override fun clear() = settings.clear()
}
