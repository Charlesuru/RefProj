package com.studiodraug.refproj.core.featureflags

import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

/** Koin bindings. Replace [RemoteFlagSource] with a Remote Config implementation to go remote. */
val featureFlagsModule: Module =
    module {
        single<RemoteFlagSource> { NoRemoteFlagSource() }
        single { DefaultFeatureFlags(get(), get(), get()) } binds arrayOf(FeatureFlags::class, FeatureFlagOverrides::class)
    }
