package com.example.vinilosmobile.domain.models

sealed interface CreateTrackResult {
    data class Invalid(val errors: Map<TrackField, TrackFieldError>) : CreateTrackResult
    data class Created(val track: Track) : CreateTrackResult
    data class Failed(val kind: RequestFailureKind) : CreateTrackResult
    data object AlbumNotFound : CreateTrackResult
}
