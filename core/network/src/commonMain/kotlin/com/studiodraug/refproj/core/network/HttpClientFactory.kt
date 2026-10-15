package com.studiodraug.refproj.core.network

import co.touchlab.kermit.Logger
import com.studiodraug.refproj.core.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** JSON settings for every API: tolerant to new fields so old app versions keep working. */
val ApiJson: Json =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = false
    }

/**
 * Creates the app's single [HttpClient].
 *
 *  - Base URL from [AppConfig.apiBaseUrl] (per environment); requests use relative paths.
 *  - JSON via kotlinx.serialization, non-2xx responses throw (mapped by [safeApiCall]).
 *  - Timeouts so a dead network never hangs the UI.
 *  - Request logging (headers only, never bodies) in debug builds; credential headers are redacted.
 *  - TLS: the platform default (OkHttp / NSURLSession) with full certificate validation. Never disable it.
 */
fun createHttpClient(
    config: AppConfig,
    logger: Logger,
    engine: HttpClientEngine,
): HttpClient =
    HttpClient(engine) {
        expectSuccess = true

        install(ContentNegotiation) { json(ApiJson) }

        install(HttpTimeout) {
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
            socketTimeoutMillis = SOCKET_TIMEOUT_MS
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
        }

        if (config.isDebugBuild) {
            val networkLogger = logger.withTag("Network")
            install(Logging) {
                this.logger =
                    object : KtorLogger {
                        override fun log(message: String) = networkLogger.d { message }
                    }
                level = LogLevel.HEADERS
                sanitizeHeader { header -> header in REDACTED_HEADERS }
            }
        }

        defaultRequest {
            url(config.apiBaseUrl.trimEnd('/') + "/")
            contentType(ContentType.Application.Json)
        }
    }

private const val CONNECT_TIMEOUT_MS = 15_000L
private const val SOCKET_TIMEOUT_MS = 30_000L
private const val REQUEST_TIMEOUT_MS = 30_000L

private val REDACTED_HEADERS =
    setOf(HttpHeaders.Authorization, HttpHeaders.Cookie, HttpHeaders.SetCookie, HttpHeaders.ProxyAuthorization)
