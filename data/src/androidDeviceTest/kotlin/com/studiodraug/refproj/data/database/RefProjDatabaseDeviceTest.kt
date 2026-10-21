package com.studiodraug.refproj.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studiodraug.refproj.data.tasks.TaskRepositoryImpl
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith

// INTEGRATION TEST

/**
 * The production database setup on Android: a file in `databases/`, the bundled SQLite native library
 * (which cannot load on the JVM host) and the migrations. Data must survive closing and reopening.
 */
@RunWith(AndroidJUnit4::class)
class RefProjDatabaseDeviceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val path = context.getDatabasePath("device-test.db").absolutePath

    private fun open(): RefProjDatabase = Room.databaseBuilder<RefProjDatabase>(context = context, name = path).buildWithDefaults()

    @AfterTest
    fun tearDown() {
        context.deleteDatabase("device-test.db")
    }

    @Test
    fun tasks_surviveClosingAndReopeningTheDatabase() =
        runTest {
            open().let { database ->
                TaskRepositoryImpl(database.taskDao(), Clock.System).add("Persisted")
                database.close()
            }

            val reopened = open()
            try {
                assertEquals(listOf("Persisted"), TaskRepositoryImpl(reopened.taskDao(), Clock.System).tasks.first().map { it.title })
            } finally {
                reopened.close()
            }
        }
}
