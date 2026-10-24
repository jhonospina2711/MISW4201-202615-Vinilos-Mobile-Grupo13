package com.example.vinilosmobile.domain.repository

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumCatalog

interface AlbumRepository {
    suspend fun getAlbums(): AlbumCatalog
    suspend fun getAlbum(id: Long): Album
}
