package com.example.vinilosmobile.feature.albums.catalog.models

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumSource

sealed interface AlbumCatalogUiState {
    data object Loading : AlbumCatalogUiState
    data class Content(val albums: List<Album>, val source: AlbumSource) : AlbumCatalogUiState
    data object Empty : AlbumCatalogUiState
    data object Error : AlbumCatalogUiState
}
