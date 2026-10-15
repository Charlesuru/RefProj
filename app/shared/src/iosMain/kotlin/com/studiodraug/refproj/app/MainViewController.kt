package com.studiodraug.refproj.app

import androidx.compose.ui.window.ComposeUIViewController
import com.studiodraug.refproj.app.di.startApp
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.config.AppEnvironment
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

/**
 * Called once from Swift at launch (`RefProjApp.init`), before any UI. Values come from
 * Info.plist, which is populated by the active xcconfig in iosApp/Configs.
 */
fun startIosApp(
    environment: String,
    apiBaseUrl: String,
    versionName: String,
    buildNumber: String,
    isDebugBuild: Boolean,
) {
    val config =
        AppConfig(
            environment = AppEnvironment.parse(environment),
            apiBaseUrl = apiBaseUrl,
            versionName = versionName,
            buildNumber = buildNumber,
            isDebugBuild = isDebugBuild,
        )
    startApp(config)
}

/** Hosts the shared Compose UI. Requires [startIosApp] to have run. */
@Suppress("FunctionName", "unused") // Called from Swift as MainViewControllerKt.MainViewController()
fun MainViewController(): UIViewController {
    val config = KoinPlatform.getKoin().get<AppConfig>()
    return ComposeUIViewController { App(config) }
}
