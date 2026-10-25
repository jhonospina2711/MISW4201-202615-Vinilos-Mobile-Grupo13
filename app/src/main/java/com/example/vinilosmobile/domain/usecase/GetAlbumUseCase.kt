package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.repository.AlbumRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAlbumUseCase @Inject constructor(private val repository: AlbumRepository) {
    operator fun invoke(id: Long): Flow<Album> = repository.getAlbum(id)
}
