package com.example.vinilosmobile.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.userRoleDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_role",
)

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Provides
    @Singleton
    fun provideUserRoleDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.userRoleDataStore
}
