package com.studiodraug.refproj.core.storage

import org.koin.core.module.Module
import org.koin.dsl.module

/** Koin bindings for [KeyValueStore] and [SecureStore]. Platform parts in [platformStorageModule]. */
val storageModule: Module =
    module {
        includes(platformStorageModule)
        single<KeyValueStore> { SettingsKeyValueStore(get()) }
    }

/** Provides the platform `ObservableSettings` and the [SecureStore] implementation. */
internal expect val platformStorageModule: Module

/** File / suite / service names shared by both platforms. */
internal object StorageNames {
    const val SETTINGS = "refproj_settings"
    const val SECURE = "refproj_secure"
}
