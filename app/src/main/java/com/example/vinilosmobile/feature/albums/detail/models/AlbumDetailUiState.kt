package com.example.vinilosmobile.feature.albums.detail.models

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.RequestFailureKind

sealed interface AlbumDetailUiState {
    data object Loading : AlbumDetailUiState
    data class Content(val album: Album) : AlbumDetailUiState
    data object NotFound : AlbumDetailUiState
    data class Error(val kind: RequestFailureKind) : AlbumDetailUiState
}
