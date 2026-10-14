package com.studiodraug.refproj.app

import androidx.compose.ui.window.ComposeUIViewController
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.config.AppEnvironment
import platform.UIKit.UIViewController

/**
 * Entry point called from Swift. Values come from Info.plist, which is populated
 * by the active xcconfig in iosApp/Configs.
 */
@Suppress("FunctionName", "unused") // Called from Swift as MainViewControllerKt.MainViewController(...)
fun MainViewController(
    environment: String,
    apiBaseUrl: String,
    versionName: String,
    buildNumber: String,
    isDebugBuild: Boolean,
): UIViewController {
    val config =
        AppConfig(
            environment = AppEnvironment.parse(environment),
            apiBaseUrl = apiBaseUrl,
            versionName = versionName,
            buildNumber = buildNumber,
            isDebugBuild = isDebugBuild,
        )
    return ComposeUIViewController { App(config) }
}
