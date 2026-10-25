package com.example.vinilosmobile.feature.entry.models

import com.example.vinilosmobile.domain.models.UserRole

sealed interface EntryUiState {
    data object Loading : EntryUiState
    data class Choosing(val currentRole: UserRole?) : EntryUiState
    data object Saving : EntryUiState
    data object ReadError : EntryUiState
    data class SaveError(val currentRole: UserRole?) : EntryUiState
    data object NavigateHome : EntryUiState
}
