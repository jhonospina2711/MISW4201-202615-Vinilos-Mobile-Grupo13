package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetUserRoleUseCase @Inject constructor(private val repository: UserRoleRepository) {
    operator fun invoke(): Flow<UserRole?> = repository.selectedRole
}
