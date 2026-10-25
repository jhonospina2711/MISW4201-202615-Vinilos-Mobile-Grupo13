package com.example.vinilosmobile.data.network.errors

import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import retrofit2.HttpException

/** Translates transport exceptions without exposing HTTP details to the UI. */
class NetworkErrorMapper @Inject constructor() {
    fun map(cause: Throwable): RequestFailure {
        if (cause is RequestFailure) return cause
        val kind = when (cause) {
            is SocketTimeoutException -> RequestFailureKind.TIMEOUT
            is UnknownHostException, is ConnectException -> RequestFailureKind.NO_CONNECTION
            is HttpException -> when (cause.code()) {
                401, 403 -> RequestFailureKind.UNAUTHORIZED
                in 500..599 -> RequestFailureKind.SERVER
                else -> RequestFailureKind.UNKNOWN
            }
            is IOException -> RequestFailureKind.NO_CONNECTION
            else -> RequestFailureKind.UNKNOWN
        }
        return RequestFailure(kind, cause)
    }
}
