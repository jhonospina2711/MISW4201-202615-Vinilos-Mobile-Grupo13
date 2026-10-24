package com.example.vinilosmobile.feature.albums.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.repository.AlbumNotFoundException
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.usecase.GetAlbumUseCase
import com.example.vinilosmobile.feature.albums.detail.models.AlbumDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(private val getAlbum: GetAlbumUseCase) : ViewModel() {
    private val _state = MutableLiveData<AlbumDetailUiState>(AlbumDetailUiState.Loading)
    val state: LiveData<AlbumDetailUiState> = _state
    private var activeRequest = false

    fun load(id: Long) {
        if (activeRequest) return
        activeRequest = true
        viewModelScope.launch {
            _state.value = AlbumDetailUiState.Loading
            try {
                _state.value = AlbumDetailUiState.Content(getAlbum(id))
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: AlbumNotFoundException) {
                _state.value = AlbumDetailUiState.NotFound
            } catch (exception: Exception) {
                _state.value = AlbumDetailUiState.Error(
                    (exception as? RequestFailure)?.kind ?: RequestFailureKind.UNKNOWN,
                )
            } finally {
                activeRequest = false
            }
        }
    }

}
