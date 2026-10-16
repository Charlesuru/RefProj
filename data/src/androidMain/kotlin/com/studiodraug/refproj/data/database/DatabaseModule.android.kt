package com.studiodraug.refproj.data.database

import android.content.Context
import androidx.room.Room
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The database file lives in the app's private `databases/` directory, which the backup rules
 * (`data_extraction_rules.xml`) exclude from cloud backup and device transfer.
 */
internal actual val databaseModule: Module =
    module {
        single<RefProjDatabase> {
            val context = get<Context>()
            Room
                .databaseBuilder<RefProjDatabase>(
                    context = context,
                    name = context.getDatabasePath(RefProjDatabase.FILE_NAME).absolutePath,
                ).buildWithDefaults()
        }
    }
