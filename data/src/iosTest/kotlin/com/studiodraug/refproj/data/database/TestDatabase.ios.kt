package com.studiodraug.refproj.data.database

import androidx.room.Room

/** Same configuration as the app: bundled SQLite. */
internal actual fun inMemoryTestDatabase(): RefProjDatabase = Room.inMemoryDatabaseBuilder<RefProjDatabase>().buildWithDefaults()
