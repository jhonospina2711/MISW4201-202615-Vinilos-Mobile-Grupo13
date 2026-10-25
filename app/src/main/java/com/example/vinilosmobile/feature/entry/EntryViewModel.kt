package com.example.vinilosmobile.feature.entry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.usecase.GetUserRoleUseCase
import com.example.vinilosmobile.domain.usecase.SetUserRoleUseCase
import com.example.vinilosmobile.feature.entry.models.EntryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val getUserRole: GetUserRoleUseCase,
    private val setUserRole: SetUserRoleUseCase,
) : ViewModel() {
    private val _state = MutableLiveData<EntryUiState>(EntryUiState.Loading)
    val state: LiveData<EntryUiState> = _state
    private var initialized = false
    private var allowRoleChange = false

    fun initialize(allowRoleChange: Boolean) {
        if (initialized) return
        initialized = true
        this.allowRoleChange = allowRoleChange
        readSavedRole()
    }

    fun readSavedRole() {
        if (_state.value != EntryUiState.Loading && _state.value != EntryUiState.ReadError) return
        _state.value = EntryUiState.Loading
        viewModelScope.launch {
            try {
                val savedRole = getUserRole().first()
                _state.value = if (savedRole == null || allowRoleChange) {
                    EntryUiState.Choosing(savedRole)
                } else {
                    EntryUiState.NavigateHome
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.value = EntryUiState.ReadError
            }
        }
    }

    fun choose(role: UserRole) {
        val currentRole = when (val current = _state.value) {
            is EntryUiState.Choosing -> current.currentRole
            is EntryUiState.SaveError -> current.currentRole
            else -> return
        }
        if (role == currentRole) {
            _state.value = EntryUiState.NavigateHome
            return
        }
        _state.value = EntryUiState.Saving
        viewModelScope.launch {
            try {
                setUserRole(role)
                _state.value = EntryUiState.NavigateHome
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.value = EntryUiState.SaveError(currentRole)
            }
        }
    }
}
