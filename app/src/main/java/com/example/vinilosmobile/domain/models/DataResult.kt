package com.example.vinilosmobile.domain.models

/** A finite read operation can emit more than one success, such as cache followed by network. */
sealed class DataResult<out T> {
    data object Loading : DataResult<Nothing>()
    data class Success<T>(val value: T) : DataResult<T>()
    data class Error(val reason: DataError) : DataResult<Nothing>()

    /** A request stopped independently while its collecting coroutine is still active. */
    data object Cancelled : DataResult<Nothing>()
}
