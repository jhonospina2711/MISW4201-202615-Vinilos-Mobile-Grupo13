package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.preferences.UserRolePreferencesDataSource
import com.example.vinilosmobile.domain.models.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UserRoleRepositoryImplTest {
    private val preferences = mockk<UserRolePreferencesDataSource>()

    @Test fun exposesRoleChangesFromDataStoreBoundary() = runBlocking {
        every { preferences.selectedRole } returns flowOf(null, UserRole.VISITOR, UserRole.COLLECTOR)

        val roles = UserRoleRepositoryImpl(preferences).selectedRole.toList()

        assertEquals(listOf(null, UserRole.VISITOR, UserRole.COLLECTOR), roles)
    }

    @Test fun writesChosenRoleToDataStoreBoundary() = runBlocking {
        every { preferences.selectedRole } returns flowOf(null)
        coEvery { preferences.setRole(UserRole.COLLECTOR) } returns Unit

        UserRoleRepositoryImpl(preferences).setRole(UserRole.COLLECTOR)

        coVerify(exactly = 1) { preferences.setRole(UserRole.COLLECTOR) }
    }
}
