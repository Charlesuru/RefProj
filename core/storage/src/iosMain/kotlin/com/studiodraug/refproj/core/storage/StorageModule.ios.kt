package com.studiodraug.refproj.core.storage

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSUserDefaults
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrService

// KeychainSettings is marked experimental; it is a thin wrapper over the SecItem* calls.
@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class, ExperimentalForeignApi::class)
internal actual val platformStorageModule: Module =
    module {
        single<ObservableSettings> {
            NSUserDefaultsSettings(NSUserDefaults(suiteName = StorageNames.SETTINGS))
        }
        single<SecureStore> {
            KeychainSecureStore(
                keychain =
                    KeychainSettings(
                        // Retained once for the app lifetime (singleton), like KeychainSettings(service) does.
                        kSecAttrService to CFBridgingRetain(StorageNames.SECURE),
                        // Readable after the first unlock (background refresh works), never leaves this device.
                        kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
                    ),
                ioDispatcher = Dispatchers.IO,
            )
        }
    }
