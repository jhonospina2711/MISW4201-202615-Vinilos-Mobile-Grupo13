package com.example.vinilosmobile.feature.collectordetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.usecase.GetCollectorUseCase
import com.example.vinilosmobile.feature.collectordetail.models.CollectorDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@HiltViewModel
class CollectorDetailViewModel @Inject constructor(
    private val getCollector: GetCollectorUseCase,
) : ViewModel() {
    private val _state = MutableLiveData<CollectorDetailUiState>(CollectorDetailUiState.Loading)
    val state: LiveData<CollectorDetailUiState> = _state
    private var activeRequest = false

    fun load(id: Long) {
        if (id <= 0L) {
            _state.value = CollectorDetailUiState.NotFound
            return
        }
        if ((_state.value as? CollectorDetailUiState.Content)?.collector?.id == id) return
        if (activeRequest) return
        activeRequest = true
        viewModelScope.launch {
            try {
                getCollector(id).collect { result ->
                    if (result != DataResult.Loading) activeRequest = false
                    _state.value = when (result) {
                        DataResult.Loading -> CollectorDetailUiState.Loading
                        is DataResult.Success -> CollectorDetailUiState.Content(result.value)
                        is DataResult.Error -> when (val reason = result.reason) {
                            DataError.NotFound -> CollectorDetailUiState.NotFound
                            is DataError.Request -> CollectorDetailUiState.Error(reason.kind)
                        }
                        DataResult.Cancelled -> CollectorDetailUiState.Cancelled
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.value = CollectorDetailUiState.Error(RequestFailureKind.UNKNOWN)
            } finally {
                activeRequest = false
            }
        }
    }
}
