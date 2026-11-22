package com.example.vinilosmobile.feature.home.collectors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.usecase.GetCollectorsUseCase
import com.example.vinilosmobile.feature.home.collectors.models.CollectorCatalogUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class CollectorCatalogViewModel @Inject constructor(
    private val getCollectors: GetCollectorsUseCase,
) : ViewModel() {
    private val _state = MutableLiveData<CollectorCatalogUiState>(CollectorCatalogUiState.Loading)
    val state: LiveData<CollectorCatalogUiState> = _state
    private var requestJob: Job? = null

    init { reload() }

    fun reload() {
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            try {
                getCollectors().collect { result ->
                    _state.value = when (result) {
                        DataResult.Loading -> CollectorCatalogUiState.Loading
                        is DataResult.Success -> if (result.value.isEmpty()) CollectorCatalogUiState.Empty
                            else CollectorCatalogUiState.Content(result.value)
                        is DataResult.Error -> CollectorCatalogUiState.Error(
                            (result.reason as? DataError.Request)?.kind ?: RequestFailureKind.UNKNOWN,
                        )
                        DataResult.Cancelled -> CollectorCatalogUiState.Cancelled
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.value = CollectorCatalogUiState.Error(RequestFailureKind.UNKNOWN)
            }
        }
    }
}
