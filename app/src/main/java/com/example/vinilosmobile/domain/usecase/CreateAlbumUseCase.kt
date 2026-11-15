package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.di.IoDispatcher
import com.example.vinilosmobile.domain.models.CreateAlbumResult
import com.example.vinilosmobile.domain.models.NewAlbum
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.example.vinilosmobile.domain.validation.AlbumValidator
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateAlbumUseCase @Inject constructor(
    private val repository: AlbumRepository,
    private val validator: AlbumValidator,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend operator fun invoke(album: NewAlbum): CreateAlbumResult {
        val errors = withContext(ioDispatcher) { validator.validate(album) }
        if (errors.isNotEmpty()) return CreateAlbumResult.Invalid(errors)
        return try {
            CreateAlbumResult.Created(repository.createAlbum(album))
        } catch (failure: RequestFailure) {
            CreateAlbumResult.Failed(failure.kind)
        }
    }
}
