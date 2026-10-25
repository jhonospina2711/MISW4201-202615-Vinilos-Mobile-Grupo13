package com.example.vinilosmobile

import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeUserRoleRepository(initialRole: UserRole = UserRole.VISITOR) : UserRoleRepository {
    private val role = MutableStateFlow<UserRole?>(initialRole)
    override val selectedRole: StateFlow<UserRole?> = role

    override suspend fun setRole(role: UserRole) {
        this.role.value = role
    }
}
