package com.example.vinilosmobile.feature.albums.detail.models

import com.example.vinilosmobile.domain.models.Album

sealed interface AlbumDetailUiState {
    data object Loading : AlbumDetailUiState
    data class Content(val album: Album) : AlbumDetailUiState
    data object NotFound : AlbumDetailUiState
    data object Error : AlbumDetailUiState
}
