package com.example.vinilosmobile.feature.albumdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.repository.AlbumNotFoundException
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.usecase.GetAlbumUseCase
import com.example.vinilosmobile.domain.usecase.GetUserRoleUseCase
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.models.Track
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.feature.albumdetail.models.AlbumDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val getAlbum: GetAlbumUseCase,
    private val getUserRole: GetUserRoleUseCase,
) : ViewModel() {
    private val _state = MutableLiveData<AlbumDetailUiState>(AlbumDetailUiState.Loading)
    val state: LiveData<AlbumDetailUiState> = _state
    private val _selectedRole = MutableLiveData<UserRole?>()
    val selectedRole: LiveData<UserRole?> = _selectedRole
    private var activeRequest = false
    private val addedTracks = linkedMapOf<Long, Track>()

    init {
        viewModelScope.launch {
            try {
                getUserRole().collect { _selectedRole.value = it }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _selectedRole.value = null
            }
        }
    }

    fun load(id: Long) {
        if (id <= 0L) {
            _state.value = AlbumDetailUiState.NotFound
            return
        }
        if ((_state.value as? AlbumDetailUiState.Content)?.album?.id == id) return
        if (activeRequest) return
        activeRequest = true
        viewModelScope.launch {
            try {
                getAlbum(id).collect { result ->
                    when (result) {
                        DataResult.Loading -> _state.value = AlbumDetailUiState.Loading
                        is DataResult.Success -> {
                            val album = result.value
                            val mergedTracks = album.tracks.orEmpty() + addedTracks.values.filter { added ->
                                album.tracks.orEmpty().none { it.id == added.id }
                            }
                            _state.value = AlbumDetailUiState.Content(
                                if (addedTracks.isEmpty()) album else album.copy(tracks = mergedTracks),
                            )
                        }
                        is DataResult.Error -> when (val reason = result.reason) {
                            DataError.NotFound -> _state.value = AlbumDetailUiState.NotFound
                            is DataError.Request -> if (_state.value !is AlbumDetailUiState.Content) {
                                _state.value = AlbumDetailUiState.Error(reason.kind)
                            }
                        }
                        DataResult.Cancelled -> if (_state.value !is AlbumDetailUiState.Content) {
                            _state.value = AlbumDetailUiState.Cancelled
                        }
                    }
                }
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

    fun trackAdded(track: Track) {
        val current = (_state.value as? AlbumDetailUiState.Content)?.album ?: return
        if (current.tracks.orEmpty().any { it.id == track.id }) return
        addedTracks[track.id] = track
        _state.value = AlbumDetailUiState.Content(current.copy(tracks = current.tracks.orEmpty() + track))
    }

}
