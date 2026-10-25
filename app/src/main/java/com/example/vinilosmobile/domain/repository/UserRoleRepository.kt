package com.example.vinilosmobile.domain.repository

import com.example.vinilosmobile.domain.models.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRoleRepository {
    val selectedRole: Flow<UserRole?>

    suspend fun setRole(role: UserRole)
}
