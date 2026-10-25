package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.AlbumCatalog
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.repository.AlbumRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAlbumsUseCase @Inject constructor(private val repository: AlbumRepository) {
    operator fun invoke(): Flow<DataResult<AlbumCatalog>> = repository.getAlbums()
}
