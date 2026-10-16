package com.studiodraug.refproj.data

import com.studiodraug.refproj.data.auth.AuthApi
import com.studiodraug.refproj.data.auth.AuthRepositoryImpl
import com.studiodraug.refproj.data.auth.FakeAuthApi
import com.studiodraug.refproj.data.consent.ConsentRepositoryImpl
import com.studiodraug.refproj.data.onboarding.OnboardingRepositoryImpl
import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.consent.ConsentRepository
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/** Binds every domain repository interface to its implementation. */
val dataModule: Module =
    module {
        single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }
        single<AuthApi> { FakeAuthApi() }
        single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get(), get()) }
        // Created at startup: applies the stored consent to analytics before anything is logged.
        single<ConsentRepository>(createdAtStart = true) { ConsentRepositoryImpl(get(), get()) }
    }
