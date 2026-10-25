package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.repository.ArtistRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetArtistUseCase @Inject constructor(private val repository: ArtistRepository) {
    operator fun invoke(key: ArtistKey): Flow<DataResult<Artist>> = repository.getArtist(key)
}
