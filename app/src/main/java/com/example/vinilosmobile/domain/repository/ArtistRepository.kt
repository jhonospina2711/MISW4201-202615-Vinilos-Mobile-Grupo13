package com.example.vinilosmobile.domain.repository

import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {
    fun getArtists(): Flow<List<Artist>>
    fun getArtist(key: ArtistKey): Flow<Artist>
}
