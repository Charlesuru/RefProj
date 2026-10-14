package com.studiodraug.refproj.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal const val BASE_PACKAGE = "com.studiodraug.refproj"

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalArgumentException("Missing library '$alias' in libs.versions.toml") }

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias)
        .orElseThrow { IllegalArgumentException("Missing version '$alias' in libs.versions.toml") }
        .requiredVersion

internal fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias)
        .orElseThrow { IllegalArgumentException("Missing plugin '$alias' in libs.versions.toml") }
        .get()
        .pluginId

/**
 * Derives a unique Android namespace from the Gradle path, e.g.
 * `:core:designsystem` -> `com.studiodraug.refproj.core.designsystem`.
 */
internal val Project.derivedNamespace: String
    get() = BASE_PACKAGE + path.replace(':', '.').replace('-', '_')
