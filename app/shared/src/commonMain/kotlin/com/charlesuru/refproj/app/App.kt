package com.charlesuru.refproj.app

import androidx.compose.runtime.Composable
import com.charlesuru.refproj.core.config.AppConfig
import com.charlesuru.refproj.core.designsystem.theme.RefTheme
import com.charlesuru.refproj.feature.home.HomeScreen

/**
 * Root composable shared by Android and iOS.
 * Navigation (Nav3), DI (Koin) and session handling are wired here in step 4.
 */
@Composable
fun App(config: AppConfig) {
    RefTheme {
        HomeScreen(environmentName = config.environment.name)
    }
}
