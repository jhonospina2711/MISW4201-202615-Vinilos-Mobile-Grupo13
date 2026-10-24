package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.repository.ArtistRepository
import javax.inject.Inject

class GetArtistsUseCase @Inject constructor(private val repository: ArtistRepository) {
    suspend operator fun invoke(): List<Artist> = repository.getArtists()
}
