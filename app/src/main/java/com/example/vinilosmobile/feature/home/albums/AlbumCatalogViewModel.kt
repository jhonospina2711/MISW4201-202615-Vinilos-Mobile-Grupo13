package com.example.vinilosmobile.feature.home.albums

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.usecase.GetAlbumsUseCase
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.feature.home.albums.models.AlbumCatalogUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class AlbumCatalogViewModel @Inject constructor(private val getAlbums: GetAlbumsUseCase) : ViewModel() {
    private val _state = MutableLiveData<AlbumCatalogUiState>(AlbumCatalogUiState.Loading)
    val state: LiveData<AlbumCatalogUiState> = _state
    private var activeRequest = false

    init { reload() }

    fun reload() {
        if (activeRequest) return
        activeRequest = true
        viewModelScope.launch {
            try {
                getAlbums().collect { result ->
                    when (result) {
                        DataResult.Loading -> _state.value = AlbumCatalogUiState.Loading
                        is DataResult.Success -> {
                            val catalog = result.value
                            _state.value = if (catalog.albums.isEmpty()) AlbumCatalogUiState.Empty
                            else AlbumCatalogUiState.Content(catalog.albums, catalog.source)
                        }
                        is DataResult.Error -> {
                            if (_state.value !is AlbumCatalogUiState.Content) {
                                val kind = (result.reason as? DataError.Request)?.kind ?: RequestFailureKind.UNKNOWN
                                _state.value = AlbumCatalogUiState.Error(kind)
                            }
                        }
                        DataResult.Cancelled -> {
                            if (_state.value !is AlbumCatalogUiState.Content) {
                                _state.value = AlbumCatalogUiState.Cancelled
                            }
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.value = AlbumCatalogUiState.Error(
                    (exception as? RequestFailure)?.kind ?: RequestFailureKind.UNKNOWN,
                )
            } finally {
                activeRequest = false
            }
        }
    }

}
