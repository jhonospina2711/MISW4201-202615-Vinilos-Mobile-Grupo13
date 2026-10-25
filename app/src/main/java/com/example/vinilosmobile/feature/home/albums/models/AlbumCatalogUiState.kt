package com.example.vinilosmobile.feature.home.albums.models

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumSource
import com.example.vinilosmobile.domain.models.RequestFailureKind

sealed interface AlbumCatalogUiState {
    data object Loading : AlbumCatalogUiState
    data class Content(val albums: List<Album>, val source: AlbumSource) : AlbumCatalogUiState
    data object Empty : AlbumCatalogUiState
    data class Error(val kind: RequestFailureKind) : AlbumCatalogUiState
    data object Cancelled : AlbumCatalogUiState
}
