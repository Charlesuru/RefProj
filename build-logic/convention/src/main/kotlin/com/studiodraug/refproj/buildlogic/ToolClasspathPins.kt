package com.studiodraug.refproj.buildlogic

import org.gradle.api.Project

/**
 * Security pins for tool classpaths that AGP and the Kotlin plugin resolve inside every module. These
 * libraries run in the build (Android Lint, Kotlin swift-export), they are not shipped in the app.
 * The root build.gradle.kts pins the same bundle on the build classpath; versions, advisories and the
 * removal rule are in gradle/libs.versions.toml (bundle `build-tool-security-pins`).
 */
private val TOOL_CONFIGURATIONS = setOf("androidLintTool", "swiftExportClasspathResolvable")

private const val PIN_BUNDLE = "build-tool-security-pins"

private const val REASON = "Known vulnerabilities in the version the toolchain resolves; see libs.versions.toml"

internal fun Project.pinToolClasspathSecurityVersions() {
    val pins =
        libs
            .findBundle(PIN_BUNDLE)
            .orElseThrow { IllegalArgumentException("Missing bundle '$PIN_BUNDLE' in libs.versions.toml") }
            .get()
            .map { "${it.module}:${it.versionConstraint.requiredVersion}" }
    configurations.matching { it.name in TOOL_CONFIGURATIONS }.configureEach {
        val configuration = name
        pins.forEach { pin -> project.dependencies.constraints.add(configuration, pin) { because(REASON) } }
    }
}
