package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.repository.ArtistRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetArtistsUseCase @Inject constructor(private val repository: ArtistRepository) {
    operator fun invoke(): Flow<DataResult<List<Artist>>> = repository.getArtists()
}
