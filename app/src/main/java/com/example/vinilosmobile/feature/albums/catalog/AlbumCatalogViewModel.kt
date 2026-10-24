package com.example.vinilosmobile.feature.albums.catalog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.usecase.GetAlbumsUseCase
import com.example.vinilosmobile.feature.albums.catalog.models.AlbumCatalogUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@HiltViewModel
class AlbumCatalogViewModel @Inject constructor(private val getAlbums: GetAlbumsUseCase) : ViewModel() {
    private val _state = MutableLiveData<AlbumCatalogUiState>(AlbumCatalogUiState.Loading)
    val state: LiveData<AlbumCatalogUiState> = _state

    init { reload() }

    fun reload() {
        if (activeRequest) return
        activeRequest = true
        viewModelScope.launch {
            _state.value = AlbumCatalogUiState.Loading
            try {
                val catalog = getAlbums()
                _state.value = if (catalog.albums.isEmpty()) AlbumCatalogUiState.Empty
                else AlbumCatalogUiState.Content(catalog.albums, catalog.source)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.value = AlbumCatalogUiState.Error
            } finally {
                activeRequest = false
            }
        }
    }

    private var activeRequest = false
}
