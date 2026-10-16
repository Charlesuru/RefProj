package com.studiodraug.refproj.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.studiodraug.refproj.data.tasks.TaskDao
import com.studiodraug.refproj.data.tasks.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module

/**
 * The app's local database (Room). Schemas are exported to `data/schemas` and committed.
 *
 * Changing an entity: bump `version` and add a migration (`addMigrations` in [buildWithDefaults], or an
 * `@AutoMigration` for simple cases). Never use destructive migration — it silently deletes user data.
 */
@Database(entities = [TaskEntity::class], version = 1, exportSchema = true)
@ConstructedBy(RefProjDatabaseConstructor::class)
internal abstract class RefProjDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val FILE_NAME = "refproj.db"
    }
}

// Room's KSP processor generates the `actual` implementations for every platform.
@Suppress("KotlinNoActualForExpect")
internal expect object RefProjDatabaseConstructor : RoomDatabaseConstructor<RefProjDatabase> {
    override fun initialize(): RefProjDatabase
}

/** Shared configuration for the platform builders: bundled SQLite and queries off the main thread. */
internal fun RoomDatabase.Builder<RefProjDatabase>.buildWithDefaults(): RefProjDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

/** Provides the platform's [RefProjDatabase] (the file location differs per platform). */
internal expect val databaseModule: Module
