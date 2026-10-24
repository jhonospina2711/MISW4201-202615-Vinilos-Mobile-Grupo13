package com.example.vinilosmobile.feature.home.artists.models

import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.RequestFailureKind

sealed interface ArtistCatalogUiState {
    data object Loading : ArtistCatalogUiState
    data class Content(val artists: List<Artist>) : ArtistCatalogUiState
    data object Empty : ArtistCatalogUiState
    data class Error(val kind: RequestFailureKind) : ArtistCatalogUiState
}
