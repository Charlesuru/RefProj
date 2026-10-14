package com.studiodraug.refproj.buildlogic

import org.gradle.api.Project

/**
 * Security pins for tool classpaths that AGP resolves inside every Android module. These libraries
 * run in the build (Android Lint), they are not shipped in the app. The root build.gradle.kts pins the
 * same libraries on the build classpath; versions and advisories are in gradle/libs.versions.toml.
 * Remove once AGP itself depends on fixed versions.
 */
private val TOOL_CONFIGURATIONS = setOf("androidLintTool")

private val PINNED_LIBRARIES = listOf("bouncycastle-bcprov", "bouncycastle-bcpkix", "bouncycastle-bcutil")

private const val REASON = "GHSA-9pwp-9qqc-pr26, GHSA-qp49-qgx5-5m26, GHSA-c3fc-8qff-9hwx"

internal fun Project.pinToolClasspathSecurityVersions() {
    val pins =
        PINNED_LIBRARIES.map { alias ->
            val library = libs.lib(alias).get()
            "${library.module}:${library.versionConstraint.requiredVersion}"
        }
    configurations.matching { it.name in TOOL_CONFIGURATIONS }.configureEach {
        val configuration = name
        pins.forEach { pin -> project.dependencies.constraints.add(configuration, pin) { because(REASON) } }
    }
}
