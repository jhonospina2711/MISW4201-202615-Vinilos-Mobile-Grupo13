package com.example.vinilosmobile.feature.artistdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.repository.ArtistNotFoundException
import com.example.vinilosmobile.domain.usecase.GetArtistUseCase
import com.example.vinilosmobile.feature.artistdetail.models.ArtistDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val getArtist: GetArtistUseCase,
) : ViewModel() {
    private val _state = MutableLiveData<ArtistDetailUiState>(ArtistDetailUiState.Loading)
    val state: LiveData<ArtistDetailUiState> = _state
    private var activeRequest = false

    fun load(key: ArtistKey?) {
        if (key == null || key.id <= 0L) {
            _state.value = ArtistDetailUiState.NotFound
            return
        }
        if ((_state.value as? ArtistDetailUiState.Content)?.artist?.key == key) return
        if (activeRequest) return
        activeRequest = true
        viewModelScope.launch {
            _state.value = ArtistDetailUiState.Loading
            try {
                getArtist(key).collect { artist ->
                    _state.value = ArtistDetailUiState.Content(artist)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: ArtistNotFoundException) {
                _state.value = ArtistDetailUiState.NotFound
            } catch (exception: Exception) {
                _state.value = ArtistDetailUiState.Error(
                    (exception as? RequestFailure)?.kind ?: RequestFailureKind.UNKNOWN,
                )
            } finally {
                activeRequest = false
            }
        }
    }
}
