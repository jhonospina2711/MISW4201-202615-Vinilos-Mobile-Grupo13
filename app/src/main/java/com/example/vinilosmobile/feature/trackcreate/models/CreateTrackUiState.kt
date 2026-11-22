package com.example.vinilosmobile.feature.trackcreate.models

import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.models.Track
import com.example.vinilosmobile.domain.models.TrackField
import com.example.vinilosmobile.domain.models.TrackFieldError

sealed interface CreateTrackUiState {
    data object Editing : CreateTrackUiState
    data object Saving : CreateTrackUiState
    data class Invalid(val errors: Map<TrackField, TrackFieldError>) : CreateTrackUiState
    data class Failed(val kind: RequestFailureKind) : CreateTrackUiState
    data object AlbumNotFound : CreateTrackUiState
    data class Saved(val track: Track) : CreateTrackUiState
}
