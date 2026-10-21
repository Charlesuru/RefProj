package com.studiodraug.refproj.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import com.github.takahirom.roborazzi.captureRoboImage
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.ObservableSettings
import com.studiodraug.refproj.app.di.appModules
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.observability.LogBuffer
import com.studiodraug.refproj.core.storage.SecureStore
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.FakeCrashReporter
import com.studiodraug.refproj.core.testing.FakeSecureStore
import com.studiodraug.refproj.core.testing.ROBOLECTRIC_SDK
import com.studiodraug.refproj.core.testing.testAppConfig
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup
import java.util.Locale
import kotlin.test.Test
import org.junit.runner.RunWith
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// SCREENSHOT TEST

/**
 * Every sample state of the screen catalog (each feature's previews and the design system gallery)
 * rendered to an image and compared with the committed reference in `src/androidHostTest/screenshots`.
 * Light and dark in English, plus light in Swedish (longer words, other wrapping).
 *
 * After an intended UI change: `./gradlew :app:shared:recordRoborazziAndroidHostTest` and commit the images
 * (docs/testing.md#screenshot-tests). The previews come from the catalog, so a new sample state is covered
 * without touching this test.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [ROBOLECTRIC_SDK], qualifiers = DEVICE)
class ScreenCatalogScreenshotTest(
    private val shot: Shot,
) {
    @Test
    fun matchesTheReferenceImage() {
        RuntimeEnvironment.setQualifiers(shot.variant.qualifiers)
        // Compose resources follow the JVM default locale, not only the Android configuration.
        val defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag(shot.variant.language))
        try {
            capture()
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    private fun capture() {
        runComposeUiTest {
            // A paused clock: loading indicators and other endless animations would otherwise never let the
            // UI become idle, and a fixed frame time makes every image deterministic.
            mainClock.autoAdvance = false
            setContent { RefTheme { shot.content() } }
            mainClock.advanceTimeBy(SETTLE_MS)
            onRoot().captureRoboImage(filePath = "$REFERENCE_DIR/${shot.fileName}.png")
        }
    }

    /** One image: a catalog preview in one appearance. */
    class Shot(
        group: String,
        preview: String,
        val variant: Variant,
        val content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        val fileName = listOf(group, preview, variant.name).joinToString("_") { it.toFileName() }

        override fun toString() = fileName // the test name in reports
    }

    enum class Variant(
        val language: String,
        val qualifiers: String,
    ) {
        Light("en", "+en-notnight"),
        Dark("en", "+en-night"),
        SwedishLight("sv", "+sv-notnight"),
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun shots(): List<Shot> =
            catalog().flatMap { group ->
                group.previews.flatMap { preview ->
                    Variant.entries.map { variant -> Shot(group.title, preview.name, variant, preview.content) }
                }
            }

        /** The catalog as the app binds it: a DEV graph, where the debug menu (and its catalog) exists. */
        private fun catalog(): List<ScreenPreviewGroup> {
            val config = testAppConfig()
            val app =
                koinApplication {
                    allowOverride(true)
                    modules(appModules(config, FakeCrashReporter(), FakeAnalyticsTracker(), LogBuffer()))
                    modules(
                        module {
                            single<ObservableSettings> { MapSettings() }
                            single<SecureStore> { FakeSecureStore() }
                        },
                    )
                }
            return try {
                app.koin.getAll<ScreenPreviewGroup>().sortedBy { it.title }
            } finally {
                app.close()
            }
        }

        /** Long enough for entry animations (pager, dialogs) to finish. */
        const val SETTLE_MS = 1_000L

        private fun String.toFileName() = lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    }
}

/** Relative to the module directory (the test's working directory); same as `roborazzi.outputDir`. */
private const val REFERENCE_DIR = "src/androidHostTest/screenshots"

/** A mid-size phone at a modest density: realistic layout, small reference images. */
private const val DEVICE = "w360dp-h780dp-hdpi"
