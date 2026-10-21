package com.studiodraug.refproj.data.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Under Robolectric the bundled SQLite cannot load (its native libraries are built for Android devices,
 * not the JVM host), so host tests use the framework driver, which Robolectric backs with a real SQLite.
 * The production driver is exercised by the device tests and on iOS.
 */
internal actual fun inMemoryTestDatabase(): RefProjDatabase =
    Room
        .inMemoryDatabaseBuilder<RefProjDatabase>(ApplicationProvider.getApplicationContext<Context>())
        .setDriver(AndroidSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
