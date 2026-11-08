package com.example.vinilosmobile.feature.albumcreate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.models.CreateAlbumResult
import com.example.vinilosmobile.domain.usecase.CreateAlbumUseCase
import com.example.vinilosmobile.feature.albumcreate.models.AlbumForm
import com.example.vinilosmobile.feature.albumcreate.models.CreateAlbumUiState
import com.example.vinilosmobile.feature.albumcreate.models.toNewAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CreateAlbumViewModel @Inject constructor(
    private val createAlbum: CreateAlbumUseCase,
) : ViewModel() {
    private val _state = MutableLiveData<CreateAlbumUiState>(CreateAlbumUiState.Editing)
    val state: LiveData<CreateAlbumUiState> = _state

    fun save(form: AlbumForm) {
        if (_state.value == CreateAlbumUiState.Saving) return
        _state.value = CreateAlbumUiState.Saving
        viewModelScope.launch {
            _state.value = when (val result = createAlbum(form.toNewAlbum())) {
                is CreateAlbumResult.Invalid -> CreateAlbumUiState.Invalid(result.errors)
                is CreateAlbumResult.Failed -> CreateAlbumUiState.Failed(result.kind)
                is CreateAlbumResult.Created -> CreateAlbumUiState.Saved(result.album.id, result.album.name)
            }
        }
    }
}
