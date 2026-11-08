package com.example.vinilosmobile.domain.repository

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumCatalog
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.models.NewAlbum
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAlbums(): Flow<DataResult<AlbumCatalog>>
    fun getAlbum(id: Long): Flow<DataResult<Album>>

    /** Throws [com.example.vinilosmobile.domain.models.RequestFailure] if the API does not create it. */
    suspend fun createAlbum(album: NewAlbum): Album
}
