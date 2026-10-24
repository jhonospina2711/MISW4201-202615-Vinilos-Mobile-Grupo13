package com.example.vinilosmobile.di

import com.example.vinilosmobile.data.repository.AlbumRepositoryImpl
import com.example.vinilosmobile.data.repository.UserRoleRepositoryImpl
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAlbumRepository(implementation: AlbumRepositoryImpl): AlbumRepository

    @Binds
    @Singleton
    abstract fun bindUserRoleRepository(implementation: UserRoleRepositoryImpl): UserRoleRepository
}
