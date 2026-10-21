package com.studiodraug.refproj.buildlogic

import java.util.Properties
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportFilter
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Per-module minimum line coverage; the ratchet lives in this one file (docs/testing.md#coverage). */
internal const val COVERAGE_FLOORS_FILE = "config/kover/coverage-floors.properties"

/** Key in [COVERAGE_FLOORS_FILE] for the merged report of all modules. */
internal const val TOTAL_COVERAGE_KEY = "total"

private const val MAX_PERCENT = 100

/**
 * Applies Kover to a module and adds its coverage floor. Kover measures the JVM (Android host) tests;
 * the iOS simulator runs the same `commonTest` sources but Kotlin/Native has no coverage agent.
 */
internal fun Project.configureCoverage() {
    pluginManager.apply(libs.pluginId("kover"))
    extensions.configure<KoverProjectExtension> {
        reports {
            filters { excludes { excludeNonLogic() } }
            verify { rule("Line coverage of $path") { minBound(coverageFloor(coverageKey)) } }
        }
    }
}

/** This module's key in [COVERAGE_FLOORS_FILE]: `:core:ui` -> `core.ui` (a colon would end a properties key). */
private val Project.coverageKey: String
    get() = path.removePrefix(":").replace(':', '.')

/**
 * Code that coverage says nothing useful about. UI is covered by UI and screenshot tests, not by a
 * percentage; generated code is not ours to test.
 */
internal fun KoverReportFilter.excludeNonLogic() {
    annotatedBy(
        "androidx.compose.runtime.Composable",
        "androidx.compose.ui.tooling.preview.Preview",
        "org.jetbrains.compose.ui.tooling.preview.Preview",
    )
    classes(
        // Compose: lambdas of composables and the generated resource accessors (`<module>.resources.Res`).
        "*ComposableSingletons*",
        "$BASE_PACKAGE.*.resources.*",
        // Room: generated DAO and database implementations.
        "*_Impl",
        "*_Impl$*",
        "*DatabaseConstructor",
    )
    // Test doubles shared by the test source sets; measured only as a side effect of other modules' tests.
    packages("$BASE_PACKAGE.core.testing")
}

/** Reads this module's floor from [COVERAGE_FLOORS_FILE]; a module without a floor fails the build. */
internal fun Project.coverageFloor(key: String): Int {
    val text =
        providers
            .fileContents(rootProject.layout.projectDirectory.file(COVERAGE_FLOORS_FILE))
            .asText
            .orNull ?: error("Missing $COVERAGE_FLOORS_FILE")
    val floors = Properties().apply { load(text.reader()) }
    val value =
        floors.getProperty(key)
            ?: error("No coverage floor for '$key'. Add '$key=<percent>' to $COVERAGE_FLOORS_FILE (docs/testing.md#coverage).")
    return value.trim().toIntOrNull()?.takeIf { it in 0..MAX_PERCENT }
        ?: error("Coverage floor for '$key' in $COVERAGE_FLOORS_FILE must be a whole percentage, got '$value'.")
}
