package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.network.ArtistServiceAdapter
import com.example.vinilosmobile.data.network.errors.NetworkErrorMapper
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.models.RequestFailureKind
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
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

        assertEquals(
            listOf(DataResult.Loading, DataResult.Success(listOf(artist))),
            repository.getArtists().toList(),
        )
        coVerify(exactly = 1) { adapter.getArtists() }
    }

    @Test fun retrievesDetailWithTypeAndId() = runBlocking {
        coEvery { adapter.getArtist(key) } returns artist

        assertEquals(
            listOf(DataResult.Loading, DataResult.Success(artist)),
            repository.getArtist(key).toList(),
        )
        coVerify(exactly = 1) { adapter.getArtist(key) }
    }

    @Test fun timeoutBecomesTypedFailure() = runBlocking {
        coEvery { adapter.getArtists() } throws SocketTimeoutException("slow")

        assertEquals(
            listOf(DataResult.Loading, DataResult.Error(DataError.Request(RequestFailureKind.TIMEOUT))),
            repository.getArtists().toList(),
        )
    }

    @Test fun missingArtistKeepsNotFoundMeaning() = runBlocking {
        coEvery { adapter.getArtist(key) } throws HttpException(
            Response.error<String>(404, "missing".toResponseBody()),
        )

        assertEquals(
            listOf(DataResult.Loading, DataResult.Error(DataError.NotFound)),
            repository.getArtist(key).toList(),
        )
    }

    @Test fun independentCancellationIsNotConvertedToNetworkFailure() = runBlocking {
        coEvery { adapter.getArtists() } throws CancellationException("cancelled")

        assertEquals(
            listOf(DataResult.Loading, DataResult.Cancelled),
            repository.getArtists().toList(),
        )
    }
}
