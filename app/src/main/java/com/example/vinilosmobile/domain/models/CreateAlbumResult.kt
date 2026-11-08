package com.example.vinilosmobile.domain.models

sealed interface CreateAlbumResult {
    data class Invalid(val errors: Map<AlbumField, AlbumFieldError>) : CreateAlbumResult
    data class Created(val album: Album) : CreateAlbumResult
    data class Failed(val kind: RequestFailureKind) : CreateAlbumResult
}
