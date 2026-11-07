package com.example.vinilosmobile.feature.albumcreate.models

import com.example.vinilosmobile.domain.models.AlbumField
import com.example.vinilosmobile.domain.models.AlbumFieldError
import com.example.vinilosmobile.domain.models.RequestFailureKind

sealed interface CreateAlbumUiState {
    data object Editing : CreateAlbumUiState
    data object Saving : CreateAlbumUiState
    data class Invalid(val errors: Map<AlbumField, AlbumFieldError>) : CreateAlbumUiState
    data class Failed(val kind: RequestFailureKind) : CreateAlbumUiState
    data class Saved(val albumId: Long, val albumName: String) : CreateAlbumUiState
}
