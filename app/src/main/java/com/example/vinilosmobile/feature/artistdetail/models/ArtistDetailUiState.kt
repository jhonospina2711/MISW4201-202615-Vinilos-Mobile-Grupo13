package com.example.vinilosmobile.feature.artistdetail.models

import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.RequestFailureKind

sealed interface ArtistDetailUiState {
    data object Loading : ArtistDetailUiState
    data class Content(val artist: Artist) : ArtistDetailUiState
    data object NotFound : ArtistDetailUiState
    data class Error(val kind: RequestFailureKind) : ArtistDetailUiState
    data object Cancelled : ArtistDetailUiState
}
