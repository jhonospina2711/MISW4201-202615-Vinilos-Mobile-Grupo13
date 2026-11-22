package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.CreateTrackResult
import com.example.vinilosmobile.domain.models.NewTrack
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.repository.AlbumNotFoundException
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.example.vinilosmobile.domain.validation.TrackValidator
import javax.inject.Inject

class CreateTrackUseCase @Inject constructor(
    private val repository: AlbumRepository,
    private val validator: TrackValidator,
) {
    suspend operator fun invoke(albumId: Long, track: NewTrack): CreateTrackResult {
        if (albumId <= 0) return CreateTrackResult.AlbumNotFound
        val errors = validator.validate(track)
        if (errors.isNotEmpty()) return CreateTrackResult.Invalid(errors)
        return try {
            CreateTrackResult.Created(repository.addTrack(albumId, track))
        } catch (_: AlbumNotFoundException) {
            CreateTrackResult.AlbumNotFound
        } catch (failure: RequestFailure) {
            CreateTrackResult.Failed(failure.kind)
        }
    }
}
