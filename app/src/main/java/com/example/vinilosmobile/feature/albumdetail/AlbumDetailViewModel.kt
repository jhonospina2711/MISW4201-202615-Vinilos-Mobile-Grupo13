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
