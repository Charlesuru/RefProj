package com.studiodraug.refproj.data.remote

import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import com.studiodraug.refproj.core.network.createHttpClient
import com.studiodraug.refproj.core.network.safeApiCall
import com.studiodraug.refproj.core.testing.testAppConfig
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.model.DataError
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

// INTEGRATION TEST

/**
 * A failed call all the way from the HTTP engine to the error a use case sees: the app's real Ktor client
 * (core:network), `safeApiCall` and the data layer's mapping to [DataError]. Only the engine is fake.
 */
class RemoteErrorChainTest {
    // A JSON tree instead of a DTO: data has no DTOs yet (the auth backend is fake), and the chain is the same.
    private suspend fun call(handler: MockRequestHandler): AppResult<JsonObject> {
        val client = createHttpClient(testAppConfig(isDebugBuild = false), Logger(loggerConfigInit()), MockEngine(handler))
        return safeApiCall { client.get("profile").body<JsonObject>() }.toAppResult()
    }

    @Test
    fun success_returnsTheParsedBody() =
        runTest {
            val result = call { respond("""{"name":"Ada"}""", HttpStatusCode.OK, JSON) }
            assertEquals(
                "Ada",
                (result as AppResult.Success)
                    .value
                    .getValue("name")
                    .jsonPrimitive.content,
            )
        }

    @Test
    fun failures_reachTheDomainAsDataErrors() =
        runTest {
            val cases: List<Triple<String, MockRequestHandler, DataError>> =
                listOf(
                    Triple("401", { respond("", HttpStatusCode.Unauthorized) }, DataError.Unauthorized),
                    Triple("403", { respond("", HttpStatusCode.Forbidden) }, DataError.Unauthorized),
                    Triple("404", { respond("", HttpStatusCode.NotFound) }, DataError.NotFound),
                    Triple("503", { respond("", HttpStatusCode.ServiceUnavailable) }, DataError.Server),
                    Triple("malformed body", { respond("""{"name":""", HttpStatusCode.OK, JSON) }, DataError.Server),
                    Triple("offline", { throw IOException("no route to host") }, DataError.Network),
                    Triple("timeout", { throw ConnectTimeoutException("connect timed out") }, DataError.Network),
                )
            cases.forEach { (name, handler, expected) -> assertEquals(AppResult.Failure(expected), call(handler), name) }
        }

    private companion object {
        val JSON = headersOf(HttpHeaders.ContentType, "application/json")
    }
}
