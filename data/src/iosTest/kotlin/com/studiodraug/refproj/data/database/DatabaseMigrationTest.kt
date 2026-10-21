package com.studiodraug.refproj.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSTemporaryDirectory
import platform.posix.getenv

// INTEGRATION TEST

/**
 * Upgrades a database created from every committed schema (`data/schemas`) to the current version with
 * [ALL_MIGRATIONS], and lets Room validate the result against the current entities. A missing or wrong
 * migration fails here instead of on a user's phone.
 *
 * Runs on the iOS simulator, which reads the schemas straight from the repository (the path comes from
 * Gradle, `data/build.gradle.kts`). The schemas and the generated SQL are the same on Android.
 */
class DatabaseMigrationTest {
    private val schemaDirectory = Path(requireNotNull(schemaDirectoryFromGradle()) { "$SCHEMA_DIR_VARIABLE is not set" })
    private val databaseFile = "${NSTemporaryDirectory()}migration-test.db"

    private fun helper() =
        MigrationTestHelper(
            // The helper appends the database class name itself.
            schemaDirectoryPath = requireNotNull(schemaDirectory.parent).toString(),
            fileName = databaseFile,
            driver = BundledSQLiteDriver(),
            databaseClass = RefProjDatabase::class,
            databaseFactory = { RefProjDatabaseConstructor.initialize() },
        )

    @AfterTest
    fun tearDown() {
        listOf("", "-wal", "-shm").map { Path(databaseFile + it) }.filter { SystemFileSystem.exists(it) }.forEach {
            SystemFileSystem.delete(it)
        }
    }

    @Test
    fun newestExportedSchema_isTheCurrentVersion() {
        assertEquals(RefProjDatabase.VERSION, exportedVersions().max(), "Bump RefProjDatabase.VERSION together with the entities")
    }

    @Test
    fun everyExportedSchema_migratesToTheCurrentVersion() {
        val versions = exportedVersions()
        assertTrue(versions.isNotEmpty(), "No schemas in $schemaDirectory")
        versions.forEach { version ->
            tearDown()
            val helper = helper()
            helper.createDatabase(version).close()
            helper.runMigrationsAndValidate(RefProjDatabase.VERSION, ALL_MIGRATIONS).close()
        }
    }

    /** Versions of the committed schemas: `1.json`, `2.json`, … */
    private fun exportedVersions(): List<Int> =
        SystemFileSystem.list(schemaDirectory).mapNotNull { it.name.removeSuffix(".json").toIntOrNull() }.sorted()

    private companion object {
        const val SCHEMA_DIR_VARIABLE = "ROOM_SCHEMA_DIR"

        @OptIn(ExperimentalForeignApi::class) // getenv: C interop.
        fun schemaDirectoryFromGradle(): String? = getenv(SCHEMA_DIR_VARIABLE)?.toKString()
    }
}
