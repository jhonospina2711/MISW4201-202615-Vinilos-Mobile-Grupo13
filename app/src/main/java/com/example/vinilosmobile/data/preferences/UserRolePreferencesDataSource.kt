package com.example.vinilosmobile.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.vinilosmobile.domain.models.UserRole
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRolePreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val roleKey = stringPreferencesKey("selected_user_role")

    val selectedRole: Flow<UserRole?> = dataStore.data.map { preferences ->
        UserRole.fromStoredValue(preferences[roleKey])
    }

    suspend fun setRole(role: UserRole) {
        dataStore.edit { preferences -> preferences[roleKey] = role.storedValue }
    }
}
