package com.studiodraug.refproj.architecture

import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue as assertThat

// ARCHITECTURE TEST

/**
 * The dependency direction `app → feature → domain ← data`, everything may use `core`
 * (docs/architecture.md). Checked twice: on the imports of the production sources, and on the
 * Gradle module dependencies, because a module dependency alone already leaks types transitively.
 */
class LayerDependencyTest {
    @Test
    fun layers_dependOnlyInTheAllowedDirection() {
        // Every reference to one of our packages counts, imported or fully qualified.
        productionSources.files.assertTrue(additionalMessage = "app → feature → domain ← data; every layer may use core") { file ->
            val layer = file.packagee?.name?.let(::layerOf) ?: return@assertTrue true
            val referenced = OWN_PACKAGE_REFERENCE.findAll(file.text.withoutComments()).mapNotNull { layerOf(it.value) }
            referenced.all { it in allowedDependencies(layer) }
        }
    }

    @Test
    fun featureList_isDiscovered() {
        // Guards the rule above: an empty list would make every feature check pass vacuously.
        assertThat(featureModules.size >= MIN_EXPECTED_FEATURES, "Found features: $featureModules")
    }

    @Test
    fun domain_isPureKotlin() {
        productionSourcesOf("domain").imports.assertFalse(
            additionalMessage = "domain must stay pure Kotlin: no Compose, platform APIs, networking, storage or DI",
        ) { import -> FORBIDDEN_IN_DOMAIN.any { import.name.startsWith(it) } }
    }

    @Test
    fun features_useNoDataSourceLibraries() {
        featureModules.forEach { feature ->
            productionSourcesOf("feature/$feature").imports.assertFalse(
                additionalMessage = "Features reach data only through domain use cases",
            ) { import -> FORBIDDEN_IN_FEATURES.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun featureModules_declareNoDependencyOnOtherFeaturesOrData() {
        featureModules.forEach { feature ->
            val buildFile = File(repositoryRoot, "feature/$feature/build.gradle.kts").readText()
            assertThat(
                FORBIDDEN_MODULE_DEPENDENCY.findAll(buildFile).none(),
                "feature:$feature must not depend on another feature or on data (navigate via core:navigation route keys)",
            )
        }
    }

    @Test
    fun domainModule_declaresNoProjectDependencies() {
        val buildFile = File(repositoryRoot, "domain/build.gradle.kts").readText()
        assertThat(
            PROJECT_DEPENDENCY.findAll(buildFile).none { !it.value.contains("core") },
            "domain may only depend on core modules",
        )
    }

    @Test
    fun businessLogic_usesConstructorInjectionOnly() {
        productionSources.classes().assertTrue(
            additionalMessage = "No service locators in business logic: inject through the constructor (Koin modules only)",
        ) { declaration -> declaration.parents().none { it.name.substringAfterLast('.') == "KoinComponent" } }
    }

    /** `core`, `domain`, `data`, `app` or `feature.<name>` for a package or qualified name of this repository. */
    private fun layerOf(qualifiedName: String): String? {
        if (!qualifiedName.startsWith("$BASE_PACKAGE.")) return null
        val path = qualifiedName.removePrefix("$BASE_PACKAGE.").split('.')
        return when (path.first()) {
            "core", "domain", "data", "app" -> path.first()
            "feature" -> "feature.${path.getOrNull(1)}"
            else -> null
        }
    }

    private fun allowedDependencies(layer: String): Set<String> =
        when {
            layer == "core" -> setOf("core")
            layer == "domain" -> setOf("core", "domain")
            layer == "data" -> setOf("core", "domain", "data")
            layer.startsWith("feature.") -> setOf("core", "domain", layer)
            else -> setOf("core", "domain", "data", "app") + featureModules.map { "feature.$it" } // app: composition root
        }

    private companion object {
        const val MIN_EXPECTED_FEATURES = 5
        val OWN_PACKAGE_REFERENCE = Regex("""\b${BASE_PACKAGE.replace(".", "\\.")}(\.[a-z][a-z0-9_]*)+""")

        val FORBIDDEN_IN_DOMAIN =
            listOf(
                "android.",
                "androidx.",
                "org.jetbrains.compose.",
                "io.ktor.",
                "org.koin.",
                "com.russhwolf.",
                "co.touchlab.",
                "platform.",
                "kotlinx.cinterop.",
                "$BASE_PACKAGE.data.",
                "$BASE_PACKAGE.feature.",
            )

        val FORBIDDEN_IN_FEATURES =
            listOf("io.ktor.", "androidx.room.", "androidx.sqlite.", "com.russhwolf.", "$BASE_PACKAGE.data.")

        /** `projects.feature.x`, `projects.data`, `project(":feature:x")`, `project(":data")`. */
        val FORBIDDEN_MODULE_DEPENDENCY = Regex("""projects\.(feature\.\w+|data\b)|project\(":(feature:[\w-]+|data)"\)""")

        val PROJECT_DEPENDENCY = Regex("""projects\.[\w.]+|project\(":[\w:-]+"\)""")
    }
}
