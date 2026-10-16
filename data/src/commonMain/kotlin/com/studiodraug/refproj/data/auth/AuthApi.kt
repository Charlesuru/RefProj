package com.studiodraug.refproj.data.auth

import com.studiodraug.refproj.core.network.ApiResult

/** The authentication backend. Today only [FakeAuthApi]; a Ktor implementation replaces it per environment. */
internal interface AuthApi {
    suspend fun login(
        email: String,
        password: String,
    ): ApiResult<LoginResponse>
}

/** What the backend returns for a successful login. */
internal data class LoginResponse(
    /** Opaque bearer token. SECURITY: stored only in `SecureStore`, never logged. */
    val token: String,
    val userId: String,
    val displayName: String,
    val email: String,
)
