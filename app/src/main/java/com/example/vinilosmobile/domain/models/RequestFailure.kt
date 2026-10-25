package com.example.vinilosmobile.domain.models

class RequestFailure(
    val kind: RequestFailureKind,
    cause: Throwable,
) : Exception(cause)
