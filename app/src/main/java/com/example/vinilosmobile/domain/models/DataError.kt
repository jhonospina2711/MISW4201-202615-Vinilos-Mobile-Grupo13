package com.example.vinilosmobile.domain.models

sealed interface DataError {
    data class Request(val kind: RequestFailureKind) : DataError
    data object NotFound : DataError
}
