package com.studiodraug.refproj.data.database

/** A fresh, empty [RefProjDatabase] in memory. Close it after the test. */
internal expect fun inMemoryTestDatabase(): RefProjDatabase
