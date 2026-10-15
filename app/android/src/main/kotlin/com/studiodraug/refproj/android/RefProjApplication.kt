package com.studiodraug.refproj.android

import android.app.Application
import com.studiodraug.refproj.app.di.startApp
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.config.AppEnvironment
import org.koin.android.ext.koin.androidContext

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

    override fun onCreate() {
        super.onCreate()
        startApp(appConfig) { androidContext(this@RefProjApplication) }
    }
}
