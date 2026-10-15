package com.studiodraug.refproj.core.testing

import com.russhwolf.settings.MapSettings
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.config.AppEnvironment
import com.studiodraug.refproj.core.storage.KeyValueStore
import com.studiodraug.refproj.core.storage.SettingsKeyValueStore

/** An [AppConfig] for tests. Defaults to a DEV debug build. */
fun testAppConfig(
    environment: AppEnvironment = AppEnvironment.DEV,
    isDebugBuild: Boolean = true,
    apiBaseUrl: String = "https://api.test.example",
): AppConfig =
    AppConfig(
        environment = environment,
        apiBaseUrl = apiBaseUrl,
        versionName = "1.0.0-test",
        buildNumber = "1",
        isDebugBuild = isDebugBuild,
    )

/** The real [KeyValueStore] implementation over an in-memory map. */
fun inMemoryKeyValueStore(): KeyValueStore = SettingsKeyValueStore(MapSettings())
