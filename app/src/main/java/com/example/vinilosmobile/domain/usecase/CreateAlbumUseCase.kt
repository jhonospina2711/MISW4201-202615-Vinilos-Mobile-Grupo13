package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.CreateAlbumResult
import com.example.vinilosmobile.domain.models.NewAlbum
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.example.vinilosmobile.domain.validation.AlbumValidator
import javax.inject.Inject

class CreateAlbumUseCase @Inject constructor(
    private val repository: AlbumRepository,
    private val validator: AlbumValidator,
) {
    suspend operator fun invoke(album: NewAlbum): CreateAlbumResult {
        val errors = validator.validate(album)
        if (errors.isNotEmpty()) return CreateAlbumResult.Invalid(errors)
        return try {
            CreateAlbumResult.Created(repository.createAlbum(album))
        } catch (failure: RequestFailure) {
            CreateAlbumResult.Failed(failure.kind)
        }
    }
}
