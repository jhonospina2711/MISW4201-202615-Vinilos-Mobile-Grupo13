package com.example.vinilosmobile.domain.repository

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumCatalog
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAlbums(): Flow<AlbumCatalog>
    fun getAlbum(id: Long): Flow<Album>
}
