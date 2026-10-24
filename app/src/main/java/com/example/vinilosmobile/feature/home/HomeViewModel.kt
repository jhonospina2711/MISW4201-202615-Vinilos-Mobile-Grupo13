package com.example.vinilosmobile.feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.usecase.GetUserRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserRole: GetUserRoleUseCase,
) : ViewModel() {
    private val _selectedRole = MutableLiveData<UserRole?>()
    val selectedRole: LiveData<UserRole?> = _selectedRole

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
}
