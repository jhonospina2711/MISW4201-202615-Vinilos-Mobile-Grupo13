package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumField
import com.example.vinilosmobile.domain.models.AlbumFieldError
import com.example.vinilosmobile.domain.models.CreateAlbumResult
import com.example.vinilosmobile.domain.models.NewAlbum
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.example.vinilosmobile.domain.validation.AlbumValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateAlbumUseCaseTest {
    private val repository = mockk<AlbumRepository>()
    private val createAlbum = CreateAlbumUseCase(repository, AlbumValidator())
    private val newAlbum = NewAlbum(
        name = "Siembra",
        coverUrl = "https://example.com/siembra.jpg",
        releaseDate = "1978-08-01",
        genre = "Salsa",
        recordLabel = "Fania Records",
        description = "Salsa de 1978",
    )
    private val created = Album(
        id = 120, name = "Siembra", artistNames = emptyList(), genre = "Salsa",
        releaseYear = "1978", coverUrl = newAlbum.coverUrl, recordLabel = "Fania Records",
        description = "Salsa de 1978", tracks = emptyList(), comments = emptyList(),
    )

    @Test fun invalidAlbumNeverReachesTheRepository() = runBlocking {
        val result = createAlbum(newAlbum.copy(name = " "))

        assertEquals(CreateAlbumResult.Invalid(mapOf(AlbumField.NAME to AlbumFieldError.REQUIRED)), result)
        coVerify(exactly = 0) { repository.createAlbum(any()) }
    }

    @Test fun validAlbumIsCreated() = runBlocking {
        coEvery { repository.createAlbum(newAlbum) } returns created

        assertEquals(CreateAlbumResult.Created(created), createAlbum(newAlbum))
    }

    @Test fun repositoryFailureKeepsItsKind() = runBlocking {
        coEvery { repository.createAlbum(newAlbum) } throws
            RequestFailure(RequestFailureKind.NO_CONNECTION, IOException("offline"))

        assertEquals(CreateAlbumResult.Failed(RequestFailureKind.NO_CONNECTION), createAlbum(newAlbum))
    }
}
