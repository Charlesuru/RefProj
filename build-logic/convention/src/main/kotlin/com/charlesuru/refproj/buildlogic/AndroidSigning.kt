package com.charlesuru.refproj.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

/**
 * Release signing with the Play **upload key**. Google re-signs with the app signing key
 * (Play App Signing), so a leaked upload key can be reset without losing the app.
 *
 * Values are read from Gradle properties (`~/.gradle/gradle.properties`, never the committed file)
 * or environment variables (CI). Nothing secret is ever written to the repository. See docs/secrets.md.
 *
 * | Gradle property                 | Environment variable             |
 * |---------------------------------|----------------------------------|
 * | refproj.signing.storeFile       | REFPROJ_SIGNING_STORE_FILE       |
 * | refproj.signing.storePassword   | REFPROJ_SIGNING_STORE_PASSWORD   |
 * | refproj.signing.keyAlias        | REFPROJ_SIGNING_KEY_ALIAS        |
 * | refproj.signing.keyPassword     | REFPROJ_SIGNING_KEY_PASSWORD     |
 *
 * When any value is missing, release builds are produced **unsigned**: local release builds and PR
 * checks still work, and fastlane refuses to upload an unsigned artifact.
 */
internal fun Project.configureReleaseSigning(extension: ApplicationExtension) {
    val values = readSigningValues()
    if (values == null) {
        logger.info("Release signing not configured; release artifacts will be unsigned.")
        return
    }

    val releaseSigning =
        extension.signingConfigs.create("release") {
            storeFile = file(values.storeFile)
            storePassword = values.storePassword
            keyAlias = values.keyAlias
            keyPassword = values.keyPassword
            enableV1Signing = false // minSdk 26: APK Signature Scheme v2+ only.
            enableV2Signing = true
            enableV3Signing = true
        }
    extension.buildTypes.getByName("release").signingConfig = releaseSigning
}

private class SigningValues(
    val storeFile: String,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
)

/**
 * Returns null unless all four values are present, so a half-configured key never yields a broken build.
 * ReturnCount is suppressed: one guard clause per required value reads clearer than nested lets.
 */
@Suppress("ReturnCount")
private fun Project.readSigningValues(): SigningValues? {
    val storeFile = signingValue("storeFile", "STORE_FILE") ?: return null
    val storePassword = signingValue("storePassword", "STORE_PASSWORD") ?: return null
    val keyAlias = signingValue("keyAlias", "KEY_ALIAS") ?: return null
    val keyPassword = signingValue("keyPassword", "KEY_PASSWORD") ?: return null
    return SigningValues(storeFile, storePassword, keyAlias, keyPassword)
}

private fun Project.signingValue(
    property: String,
    envSuffix: String,
): String? =
    providers
        .gradleProperty("refproj.signing.$property")
        .orElse(providers.environmentVariable("REFPROJ_SIGNING_$envSuffix"))
        .orNull
        ?.takeIf { it.isNotBlank() }
