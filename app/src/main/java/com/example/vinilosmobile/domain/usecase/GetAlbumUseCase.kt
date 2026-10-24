package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.repository.AlbumRepository
import javax.inject.Inject

class GetAlbumUseCase @Inject constructor(private val repository: AlbumRepository) {
    suspend operator fun invoke(id: Long): Album = repository.getAlbum(id)
}
