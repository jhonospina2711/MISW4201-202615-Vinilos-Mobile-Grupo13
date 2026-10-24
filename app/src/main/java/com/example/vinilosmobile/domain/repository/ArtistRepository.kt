package com.example.vinilosmobile.domain.repository

import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey

interface ArtistRepository {
    suspend fun getArtists(): List<Artist>
    suspend fun getArtist(key: ArtistKey): Artist
}
