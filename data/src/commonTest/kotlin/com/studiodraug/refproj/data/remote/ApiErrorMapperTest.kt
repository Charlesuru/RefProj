package com.studiodraug.refproj.data.remote

import com.studiodraug.refproj.core.network.ApiError
import com.studiodraug.refproj.core.network.ApiResult
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.model.DataError
import kotlin.test.Test
import kotlin.test.assertEquals

// UNIT TEST
class ApiErrorMapperTest {
    @Test
    fun transportErrors_mapToDomainErrors() {
        mapOf(
            ApiError.Network to DataError.Network,
            ApiError.Timeout to DataError.Network,
            ApiError.Serialization to DataError.Server,
            ApiError.Http(401) to DataError.Unauthorized,
            ApiError.Http(403) to DataError.Unauthorized,
            ApiError.Http(404) to DataError.NotFound,
            ApiError.Http(409) to DataError.Server,
            ApiError.Http(503) to DataError.Server,
        ).forEach { (apiError, expected) -> assertEquals(expected, apiError.toDataError(), "$apiError") }
    }

    @Test
    fun results_keepValueOrMapError() {
        assertEquals(AppResult.Success(42), ApiResult.Success(42).toAppResult())
        assertEquals(AppResult.Failure(DataError.Network), ApiResult.Failure(ApiError.Timeout).toAppResult())
    }
}
