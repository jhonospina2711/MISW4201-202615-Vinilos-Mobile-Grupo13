package com.example.vinilosmobile.feature.trackcreate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.models.CreateTrackResult
import com.example.vinilosmobile.domain.models.NewTrack
import com.example.vinilosmobile.domain.usecase.CreateTrackUseCase
import com.example.vinilosmobile.feature.trackcreate.models.CreateTrackUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CreateTrackViewModel @Inject constructor(private val createTrack: CreateTrackUseCase) : ViewModel() {
    private val _state = MutableLiveData<CreateTrackUiState>(CreateTrackUiState.Editing)
    val state: LiveData<CreateTrackUiState> = _state

    fun save(albumId: Long, name: String, duration: String) {
        if (_state.value == CreateTrackUiState.Saving || _state.value is CreateTrackUiState.Saved) return
        _state.value = CreateTrackUiState.Saving
        viewModelScope.launch {
            _state.value = when (val result = createTrack(albumId, NewTrack(name, duration))) {
                is CreateTrackResult.Invalid -> CreateTrackUiState.Invalid(result.errors)
                is CreateTrackResult.Failed -> CreateTrackUiState.Failed(result.kind)
                CreateTrackResult.AlbumNotFound -> CreateTrackUiState.AlbumNotFound
                is CreateTrackResult.Created -> CreateTrackUiState.Saved(result.track)
            }
        }
    }
}
