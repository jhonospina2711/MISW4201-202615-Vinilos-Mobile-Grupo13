package com.example.vinilosmobile.feature.home.collectors.models

import com.example.vinilosmobile.domain.models.CollectorSummary
import com.example.vinilosmobile.domain.models.RequestFailureKind

sealed interface CollectorCatalogUiState {
    data object Loading : CollectorCatalogUiState
    data class Content(val collectors: List<CollectorSummary>) : CollectorCatalogUiState
    data object Empty : CollectorCatalogUiState
    data class Error(val kind: RequestFailureKind) : CollectorCatalogUiState
    data object Cancelled : CollectorCatalogUiState
}
