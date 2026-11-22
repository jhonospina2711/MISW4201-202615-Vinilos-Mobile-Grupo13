package com.example.vinilosmobile.feature.collectordetail.models

import com.example.vinilosmobile.domain.models.CollectorDetail
import com.example.vinilosmobile.domain.models.RequestFailureKind

sealed interface CollectorDetailUiState {
    data object Loading : CollectorDetailUiState
    data class Content(val collector: CollectorDetail) : CollectorDetailUiState
    data object NotFound : CollectorDetailUiState
    data class Error(val kind: RequestFailureKind) : CollectorDetailUiState
    data object Cancelled : CollectorDetailUiState
}
