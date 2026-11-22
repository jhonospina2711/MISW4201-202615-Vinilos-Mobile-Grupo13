package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.CreateTrackResult
import com.example.vinilosmobile.domain.models.NewTrack
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.models.Track
import com.example.vinilosmobile.domain.models.TrackField
import com.example.vinilosmobile.domain.models.TrackFieldError
import com.example.vinilosmobile.domain.repository.AlbumNotFoundException
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.example.vinilosmobile.domain.validation.TrackValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateTrackUseCaseTest {
    private val repository = mockk<AlbumRepository>()
    private val useCase = CreateTrackUseCase(repository, TrackValidator())
    private val track = NewTrack("Decisiones", "5:05")

    @Test fun invalidDataNeverPosts() = runBlocking {
        assertEquals(CreateTrackResult.Invalid(mapOf(TrackField.DURATION to TrackFieldError.INVALID_DURATION)),
            useCase(100, track.copy(duration = "5:80")))
        coVerify(exactly = 0) { repository.addTrack(any(), any()) }
    }

    @Test fun validTrackReturnsCreatedTrack() = runBlocking {
        coEvery { repository.addTrack(100, track) } returns Track(502, "Decisiones", "5:05")
        assertEquals(CreateTrackResult.Created(Track(502, "Decisiones", "5:05")), useCase(100, track))
    }

    @Test fun missingAlbumAndNetworkFailureHaveDistinctResults() = runBlocking {
        assertEquals(CreateTrackResult.AlbumNotFound, useCase(-1, track))
        coEvery { repository.addTrack(100, track) } throws AlbumNotFoundException()
        assertEquals(CreateTrackResult.AlbumNotFound, useCase(100, track))
        coEvery { repository.addTrack(100, track) } throws RequestFailure(RequestFailureKind.NO_CONNECTION, IOException())
        assertEquals(CreateTrackResult.Failed(RequestFailureKind.NO_CONNECTION), useCase(100, track))
    }
}
