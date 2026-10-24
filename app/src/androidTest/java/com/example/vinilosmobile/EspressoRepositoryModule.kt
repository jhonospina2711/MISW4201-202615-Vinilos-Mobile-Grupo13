package com.example.vinilosmobile

import com.example.vinilosmobile.data.repository.AlbumRepositoryImpl
import com.example.vinilosmobile.data.repository.ArtistRepositoryImpl
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.example.vinilosmobile.domain.repository.ArtistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EspressoRepositoryModule {
    @Binds abstract fun bindAlbums(implementation: AlbumRepositoryImpl): AlbumRepository
    @Binds abstract fun bindArtists(implementation: ArtistRepositoryImpl): ArtistRepository
}
