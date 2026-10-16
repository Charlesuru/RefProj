package com.studiodraug.refproj.app.di

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.ObservableSettings
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.featureflags.FeatureFlagOverrides
import com.studiodraug.refproj.core.featureflags.FeatureFlags
import com.studiodraug.refproj.core.storage.KeyValueStore
import com.studiodraug.refproj.core.storage.SecureStore
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.FakeCrashReporter
import com.studiodraug.refproj.core.testing.FakeSecureStore
import com.studiodraug.refproj.core.testing.testAppConfig
import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.auth.LoginUseCase
import com.studiodraug.refproj.domain.auth.LogoutUseCase
import com.studiodraug.refproj.domain.auth.ObserveAuthStateUseCase
import com.studiodraug.refproj.domain.auth.ObserveCurrentUserUseCase
import com.studiodraug.refproj.domain.consent.ConsentRepository
import com.studiodraug.refproj.domain.consent.ObserveAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.consent.SetAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.onboarding.CompleteOnboardingUseCase
import com.studiodraug.refproj.domain.onboarding.ObserveOnboardingCompletedUseCase
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import io.ktor.client.HttpClient
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
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
    private val config = testAppConfig(isDebugBuild = false)
    private val analytics = FakeAnalyticsTracker()
    private val koin =
        koinApplication {
            allowOverride(true)
            modules(appModules(config, FakeCrashReporter(), analytics))
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
        koin.get<FeatureFlags>()
        koin.get<FeatureFlagOverrides>()
        koin.get<OnboardingRepository>()
        koin.get<ObserveOnboardingCompletedUseCase>()
        koin.get<CompleteOnboardingUseCase>()
        koin.get<AuthRepository>()
        koin.get<ObserveAuthStateUseCase>()
        koin.get<ObserveCurrentUserUseCase>()
        koin.get<LoginUseCase>()
        koin.get<LogoutUseCase>()
        koin.get<ConsentRepository>()
        koin.get<ObserveAnalyticsConsentUseCase>()
        koin.get<SetAnalyticsConsentUseCase>()
    }

    @Test
    fun startup_appliesTheStoredConsent_soAnalyticsIsOffUntilTheUserOptsIn() {
        assertFalse(analytics.collectionEnabled)
    }
}
