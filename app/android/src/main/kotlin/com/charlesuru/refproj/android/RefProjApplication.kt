package com.charlesuru.refproj.android

import android.app.Application
import com.charlesuru.refproj.core.config.AppConfig
import com.charlesuru.refproj.core.config.AppEnvironment

class RefProjApplication : Application() {
    val appConfig: AppConfig by lazy {
        AppConfig(
            environment = AppEnvironment.parse(BuildConfig.APP_ENVIRONMENT),
            apiBaseUrl = BuildConfig.API_BASE_URL,
            versionName = BuildConfig.VERSION_NAME,
            buildNumber = BuildConfig.VERSION_CODE.toString(),
            isDebugBuild = BuildConfig.DEBUG,
        )
    }
}
