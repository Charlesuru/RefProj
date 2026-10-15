package com.studiodraug.refproj.core.storage

import android.content.Context
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformStorageModule: Module =
    module {
        single<ObservableSettings> {
            SharedPreferencesSettings(androidContext().getSharedPreferences(StorageNames.SETTINGS, Context.MODE_PRIVATE))
        }
        single<SecureStore> {
            AndroidKeystoreSecureStore(
                preferences = androidContext().getSharedPreferences(StorageNames.SECURE, Context.MODE_PRIVATE),
                ioDispatcher = Dispatchers.IO,
            )
        }
    }
