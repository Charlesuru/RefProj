package com.studiodraug.refproj.data.remote

import com.studiodraug.refproj.core.network.ApiError
import com.studiodraug.refproj.core.network.ApiResult
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.model.DataError

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404

/** Translates transport failures into the domain's vocabulary. */
internal fun ApiError.toDataError(): DataError =
    when (this) {
        ApiError.Network, ApiError.Timeout -> {
            DataError.Network
        }

        ApiError.Serialization -> {
            DataError.Server
        }

        is ApiError.Http -> {
            when (statusCode) {
                HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> DataError.Unauthorized
                HTTP_NOT_FOUND -> DataError.NotFound
                else -> DataError.Server
            }
        }
    }

internal fun <T> ApiResult<T>.toAppResult(): AppResult<T> =
    when (this) {
        is ApiResult.Success -> AppResult.Success(value)
        is ApiResult.Failure -> AppResult.Failure(error.toDataError())
    }
