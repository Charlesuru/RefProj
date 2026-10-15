package com.studiodraug.refproj.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.Module
import org.koin.dsl.module

/** Koin bindings for the shared [HttpClient]. */
val networkModule: Module =
    module {
        single<HttpClient> { createHttpClient(config = get(), logger = get(), engine = platformHttpEngine()) }
    }

/** OkHttp on Android, NSURLSession (Darwin) on iOS. */
internal expect fun platformHttpEngine(): HttpClientEngine
