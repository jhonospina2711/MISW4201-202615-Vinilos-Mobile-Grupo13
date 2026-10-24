package com.example.vinilosmobile.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.vinilosmobile.domain.models.UserRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UserRolePreferencesDataSourceTest {
    private val dataStore = mockk<DataStore<Preferences>>()
    private val roleKey = stringPreferencesKey("selected_user_role")

    @Test fun unknownStoredRoleIsTreatedAsNoSelection() = runBlocking {
        val stored = emptyPreferences().toMutablePreferences().apply { this[roleKey] = "invalid" }
        every { dataStore.data } returns flow { emit(stored) }

        assertEquals(null, UserRolePreferencesDataSource(dataStore).selectedRole.first())
    }

    @Test fun savesAndReadsCollectorRole() = runBlocking {
        var stored: Preferences = emptyPreferences()
        every { dataStore.data } returns flow { emit(stored) }
        coEvery { dataStore.updateData(any()) } coAnswers {
            stored = firstArg<suspend (Preferences) -> Preferences>()(stored)
            stored
        }
        val source = UserRolePreferencesDataSource(dataStore)

        source.setRole(UserRole.COLLECTOR)

        assertEquals("collector", stored[roleKey])
        assertEquals(UserRole.COLLECTOR, source.selectedRole.first())
        coVerify(exactly = 1) { dataStore.updateData(any()) }
    }
}
