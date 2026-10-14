package com.studiodraug.refproj.app

import androidx.compose.runtime.Composable
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.feature.home.HomeScreen

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
