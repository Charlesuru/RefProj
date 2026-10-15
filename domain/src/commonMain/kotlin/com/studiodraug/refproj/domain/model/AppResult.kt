package com.studiodraug.refproj.domain.model

/**
 * Outcome of an operation that can fail in an expected way. Use cases and repositories return
 * this instead of throwing, so the UI must handle every [DataError].
 */
sealed interface AppResult<out T> {
    data class Success<T>(
        val value: T,
    ) : AppResult<T>

    data class Failure(
        val error: DataError,
    ) : AppResult<Nothing>
}

/** Expected failures, independent of where the data comes from. The UI maps each to a message. */
sealed interface DataError {
    /** No connection or a timeout: worth retrying. */
    data object Network : DataError

    /** The session is missing or expired: the user must sign in again. */
    data object Unauthorized : DataError

    data object NotFound : DataError

    /** The backend failed (5xx) or returned something unexpected. */
    data object Server : DataError
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(value))
        is AppResult.Failure -> this
    }
