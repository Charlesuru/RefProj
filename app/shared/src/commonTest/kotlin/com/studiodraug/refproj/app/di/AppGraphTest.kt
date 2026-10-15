package com.studiodraug.refproj.app.di

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.ObservableSettings
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.storage.KeyValueStore
import com.studiodraug.refproj.core.storage.SecureStore
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.FakeCrashReporter
import com.studiodraug.refproj.core.testing.FakeSecureStore
import com.studiodraug.refproj.core.testing.testAppConfig
import com.studiodraug.refproj.domain.onboarding.CompleteOnboardingUseCase
import com.studiodraug.refproj.domain.onboarding.ObserveOnboardingCompletedUseCase
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import io.ktor.client.HttpClient
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertSame
import org.koin.dsl.koinApplication
import org.koin.dsl.module

// INTEGRATION TEST

/**
 * Builds the real Koin graph and resolves the important types, on Android and iOS. Catches a
 * missing binding at test time instead of as a crash on launch. Only the platform storage
 * (SharedPreferences / NSUserDefaults / Keystore) is replaced, since it needs a device.
 */
class AppGraphTest {
    private val config = testAppConfig()
    private val koin =
        koinApplication {
            allowOverride(true)
            modules(appModules(config, FakeCrashReporter(), FakeAnalyticsTracker()))
            modules(
                module {
                    single<ObservableSettings> { MapSettings() }
                    single<SecureStore> { FakeSecureStore() }
                },
            )
        }.koin

    @AfterTest
    fun tearDown() = koin.close()

    @Test
    fun graph_resolvesEveryEntryPoint() {
        assertSame(config, koin.get<AppConfig>())
        koin.get<KeyValueStore>()
        koin.get<HttpClient>()
        koin.get<OnboardingRepository>()
        koin.get<ObserveOnboardingCompletedUseCase>()
        koin.get<CompleteOnboardingUseCase>()
    }
}
