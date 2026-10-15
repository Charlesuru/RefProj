package com.studiodraug.refproj.core.network

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/** Why an API call failed. Mapped to domain errors by the data layer. */
sealed interface ApiError {
    /** The server answered with a non-2xx status. */
    data class Http(
        val statusCode: Int,
    ) : ApiError

    /** No connection, DNS failure, connection reset. */
    data object Network : ApiError

    data object Timeout : ApiError

    /** The response body did not match the expected model. */
    data object Serialization : ApiError
}

sealed interface ApiResult<out T> {
    data class Success<T>(
        val value: T,
    ) : ApiResult<T>

    data class Failure(
        val error: ApiError,
    ) : ApiResult<Nothing>
}

/**
 * Runs an API call and turns the expected failures into [ApiResult.Failure]. Cancellation is
 * always rethrown. Anything else is a bug and propagates.
 */
suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: ResponseException) {
        ApiResult.Failure(ApiError.Http(e.response.status.value))
    } catch (_: HttpRequestTimeoutException) {
        ApiResult.Failure(ApiError.Timeout)
    } catch (_: ConnectTimeoutException) {
        ApiResult.Failure(ApiError.Timeout)
    } catch (_: SocketTimeoutException) {
        ApiResult.Failure(ApiError.Timeout)
    } catch (_: ContentConvertException) {
        ApiResult.Failure(ApiError.Serialization)
    } catch (_: SerializationException) {
        ApiResult.Failure(ApiError.Serialization)
    } catch (_: IOException) {
        ApiResult.Failure(ApiError.Network)
    }
