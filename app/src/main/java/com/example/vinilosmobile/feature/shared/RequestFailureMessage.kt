package com.example.vinilosmobile.feature.shared

import androidx.annotation.StringRes
import com.example.vinilosmobile.R
import com.example.vinilosmobile.domain.models.RequestFailureKind

@StringRes
fun RequestFailureKind.messageRes(): Int = when (this) {
    RequestFailureKind.NO_CONNECTION -> R.string.request_no_connection
    RequestFailureKind.TIMEOUT -> R.string.request_timeout
    RequestFailureKind.UNAUTHORIZED -> R.string.request_unauthorized
    RequestFailureKind.SERVER -> R.string.request_server
    RequestFailureKind.UNKNOWN -> R.string.request_unknown
}
