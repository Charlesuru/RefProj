package com.studiodraug.refproj.core.network

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.config.AppEnvironment
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.Serializable

// INTEGRATION TEST

/** The real client configuration against Ktor's MockEngine: base URL, JSON, error mapping, log redaction. */
class HttpClientTest {
    @Serializable
    private data class Item(
        val id: Int,
        val name: String,
    )

    private val logLines = mutableListOf<String>()
    private val logger =
        Logger(
            loggerConfigInit(
                object : LogWriter() {
                    override fun log(
                        severity: Severity,
                        message: String,
                        tag: String,
                        throwable: Throwable?,
                    ) {
                        logLines += message
                    }
                },
            ),
        )

    private fun client(
        isDebugBuild: Boolean = true,
        handler: MockRequestHandler,
    ) = createHttpClient(
        config =
            AppConfig(
                environment = AppEnvironment.DEV,
                apiBaseUrl = "https://api.test.example/v1",
                versionName = "1.0.0",
                buildNumber = "1",
                isDebugBuild = isDebugBuild,
            ),
        logger = logger,
        engine = MockEngine(handler),
    )

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun relativePath_isResolvedAgainstEnvironmentBaseUrl_andJsonIsParsed() =
        runTest {
            var requestedUrl = ""
            val client =
                client { request ->
                    requestedUrl = request.url.toString()
                    respond("""{"id":1,"name":"Milk","unknown":true}""", HttpStatusCode.OK, jsonHeaders)
                }

            val result = safeApiCall { client.get("items/1").body<Item>() }

            assertEquals("https://api.test.example/v1/items/1", requestedUrl)
            assertEquals(ApiResult.Success(Item(1, "Milk")), result)
        }

    @Test
    fun httpErrors_areMappedToStatusCode() =
        runTest {
            listOf(HttpStatusCode.NotFound, HttpStatusCode.Unauthorized, HttpStatusCode.InternalServerError).forEach { status ->
                val client = client { respond("", status) }
                assertEquals(
                    ApiResult.Failure(ApiError.Http(status.value)),
                    safeApiCall { client.get("items").body<Item>() },
                )
            }
        }

    @Test
    fun malformedBody_isSerializationError() =
        runTest {
            val client = client { respond("""{"id":"not a number"}""", HttpStatusCode.OK, jsonHeaders) }
            assertEquals(ApiResult.Failure(ApiError.Serialization), safeApiCall { client.get("items/1").body<Item>() })
        }

    @Test
    fun connectionFailure_isNetworkError() =
        runTest {
            val client = client { throw IOException("connection reset") }
            assertEquals(ApiResult.Failure(ApiError.Network), safeApiCall { client.get("items").body<Item>() })
        }

    @Test
    fun timeouts_areTimeoutErrors() =
        runTest {
            listOf(
                ConnectTimeoutException("connect timed out"),
                SocketTimeoutException("read timed out"),
            ).forEach { timeout ->
                val client = client { throw timeout }
                assertEquals(ApiResult.Failure(ApiError.Timeout), safeApiCall { client.get("items").body<Item>() }, "$timeout")
            }
        }

    @Test
    fun debugLogging_redactsCredentials() =
        runTest {
            val client = client { respond("""{"id":1,"name":"x"}""", HttpStatusCode.OK, jsonHeaders) }

            client.get("items/1") { header(HttpHeaders.Authorization, "Bearer secret-token-123") }

            assertTrue(logLines.any { it.contains("items/1") }, "request should be logged in debug builds")
            assertFalse(logLines.any { it.contains("secret-token-123") }, "credentials must never be logged")
        }

    @Test
    fun releaseBuild_doesNotLogRequests() =
        runTest {
            val client = client(isDebugBuild = false) { respond("""{"id":1,"name":"x"}""", HttpStatusCode.OK, jsonHeaders) }

            client.get("items/1")

            assertTrue(logLines.isEmpty())
        }
}
