package com.studiodraug.refproj.data

import com.studiodraug.refproj.data.onboarding.OnboardingRepositoryImpl
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/** Binds every domain repository interface to its implementation. */
val dataModule: Module =
    module {
        single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }
    }
