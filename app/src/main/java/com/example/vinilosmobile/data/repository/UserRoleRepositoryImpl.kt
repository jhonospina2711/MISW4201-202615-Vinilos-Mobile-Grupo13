package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.preferences.UserRolePreferencesDataSource
import com.example.vinilosmobile.di.IoDispatcher
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRoleRepositoryImpl @Inject constructor(
    private val preferences: UserRolePreferencesDataSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UserRoleRepository {
    override val selectedRole: Flow<UserRole?> = preferences.selectedRole

    override suspend fun setRole(role: UserRole) = withContext(ioDispatcher) {
        preferences.setRole(role)
    }
}
