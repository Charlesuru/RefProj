package com.studiodraug.refproj.data.database

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask

/**
 * The database lives in `Application Support/databases/` (not visible to the user), excluded from
 * iCloud and computer backups like on Android (`data_extraction_rules.xml`).
 */
internal actual val databaseModule: Module =
    module {
        single<RefProjDatabase> {
            Room
                .databaseBuilder<RefProjDatabase>(name = "${databaseDirectory().path}/${RefProjDatabase.FILE_NAME}")
                .buildWithDefaults()
        }
    }

@OptIn(ExperimentalForeignApi::class) // Foundation interop with NSError out-parameters (null = ignore).
private fun databaseDirectory(): NSURL {
    val fileManager = NSFileManager.defaultManager
    val appSupport =
        fileManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        ) ?: error("Application Support directory is unavailable")
    val directory = appSupport.URLByAppendingPathComponent("databases", isDirectory = true) ?: error("Invalid database directory")
    fileManager.createDirectoryAtURL(directory, withIntermediateDirectories = true, attributes = null, error = null)
    directory.setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
    return directory
}
