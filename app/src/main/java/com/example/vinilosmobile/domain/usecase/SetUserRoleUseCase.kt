package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import javax.inject.Inject

class SetUserRoleUseCase @Inject constructor(private val repository: UserRoleRepository) {
    suspend operator fun invoke(role: UserRole) = repository.setRole(role)
}
