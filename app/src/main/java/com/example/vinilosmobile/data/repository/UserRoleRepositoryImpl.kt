package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.preferences.UserRolePreferencesDataSource
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class UserRoleRepositoryImpl @Inject constructor(
    private val preferences: UserRolePreferencesDataSource,
) : UserRoleRepository {
    override val selectedRole: Flow<UserRole?> = preferences.selectedRole

    override suspend fun setRole(role: UserRole) = preferences.setRole(role)
}
