package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.AlbumCatalog
import com.example.vinilosmobile.domain.repository.AlbumRepository
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(private val repository: AlbumRepository) {
    suspend operator fun invoke(): AlbumCatalog = repository.getAlbums()
}
