package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.repository.ArtistRepository
import javax.inject.Inject

class GetArtistUseCase @Inject constructor(private val repository: ArtistRepository) {
    suspend operator fun invoke(key: ArtistKey): Artist = repository.getArtist(key)
}
