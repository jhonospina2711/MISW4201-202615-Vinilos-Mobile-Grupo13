package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.network.ArtistServiceAdapter
import com.example.vinilosmobile.data.network.errors.NetworkErrorMapper
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.repository.ArtistNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ArtistRepositoryImplTest {
    private val adapter = mockk<ArtistServiceAdapter>()
    private val repository = ArtistRepositoryImpl(adapter, NetworkErrorMapper())
    private val key = ArtistKey(5, ArtistType.MUSICIAN)
    private val artist = Artist(key, "Músico", null, null, null, emptyList(), emptyList(), emptyList())

    @Test fun listsArtistsFromAdapter() = runBlocking {
        coEvery { adapter.getArtists() } returns listOf(artist)

        assertEquals(listOf(artist), repository.getArtists())
        coVerify(exactly = 1) { adapter.getArtists() }
    }

    @Test fun retrievesDetailWithTypeAndId() = runBlocking {
        coEvery { adapter.getArtist(key) } returns artist

        assertEquals(artist, repository.getArtist(key))
        coVerify(exactly = 1) { adapter.getArtist(key) }
    }

    @Test fun timeoutBecomesTypedFailure() = runBlocking {
        coEvery { adapter.getArtists() } throws SocketTimeoutException("slow")

        val failure = assertThrows(RequestFailure::class.java) {
            runBlocking { repository.getArtists() }
        }
        assertEquals(RequestFailureKind.TIMEOUT, failure.kind)
    }

    @Test fun missingArtistKeepsNotFoundMeaning() = runBlocking {
        coEvery { adapter.getArtist(key) } throws HttpException(
            Response.error<String>(404, "missing".toResponseBody()),
        )

        assertThrows(ArtistNotFoundException::class.java) {
            runBlocking { repository.getArtist(key) }
        }
        Unit
    }

    @Test fun cancellationIsNotConvertedToNetworkFailure() = runBlocking {
        coEvery { adapter.getArtists() } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.getArtists() }
        }
        Unit
    }
}
