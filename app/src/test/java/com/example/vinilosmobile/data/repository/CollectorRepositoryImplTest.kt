package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.network.errors.NetworkErrorMapper
import com.example.vinilosmobile.data.network.models.AlbumDto
import com.example.vinilosmobile.data.network.models.CollectorAlbumDto
import com.example.vinilosmobile.data.network.models.CollectorDto
import com.example.vinilosmobile.data.network.services.CollectorApiService
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.models.RequestFailureKind
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class CollectorRepositoryImplTest {
    private val service = mockk<CollectorApiService>()
    private val repository = CollectorRepositoryImpl(service, NetworkErrorMapper(), Dispatchers.Unconfined)
    private val collector = CollectorDto(id = 7, name = "Carlos", telephone = "3101234567")

    @Test fun listMapsValidCollectorsAndKeepsEmptyResult() = runBlocking {
        coEvery { service.getCollectors() } returns listOf(collector, CollectorDto(name = "Sin id"))
        val events = repository.getCollectors().toList()
        assertEquals(DataResult.Loading, events.first())
        assertEquals(listOf("Carlos"), (events.last() as DataResult.Success).value.map { it.name })
        coVerify(exactly = 1) { service.getCollectors() }

        coEvery { service.getCollectors() } returns emptyList()
        assertEquals(DataResult.Success(emptyList<Any>()), repository.getCollectors().toList().last())
    }

    @Test fun detailRequestsSeparateAlbumRelation() = runBlocking {
        coEvery { service.getCollector(7) } returns collector
        coEvery { service.getCollectorAlbums(7) } returns listOf(
            CollectorAlbumDto(11, 65000.0, "Active", AlbumDto(id = 100, name = "Buscando América")),
        )
        val result = repository.getCollector(7).toList().last() as DataResult.Success
        assertEquals("Buscando América", result.value.albums?.single()?.album?.name)
        coVerify(exactly = 1) { service.getCollectorAlbums(7) }
    }

    @Test fun detailRemainsUsableWhenAlbumRelationFails() = runBlocking {
        coEvery { service.getCollector(7) } returns collector
        coEvery { service.getCollectorAlbums(7) } throws SocketTimeoutException("slow")
        val result = repository.getCollector(7).toList().last() as DataResult.Success
        assertEquals("Carlos", result.value.name)
        assertNull(result.value.albums)
    }

    @Test fun listFailureAndMissingDetailKeepDistinctErrors() = runBlocking {
        coEvery { service.getCollectors() } throws SocketTimeoutException("slow")
        coEvery { service.getCollector(7) } throws HttpException(Response.error<CollectorDto>(404, "{}".toResponseBody()))
        assertEquals(
            DataResult.Error(DataError.Request(RequestFailureKind.TIMEOUT)),
            repository.getCollectors().toList().last(),
        )
        assertEquals(DataResult.Error(DataError.NotFound), repository.getCollector(7).toList().last())
    }

    @Test fun independentCancellationIsReported() = runBlocking {
        coEvery { service.getCollectors() } throws CancellationException("cancelled")
        assertEquals(DataResult.Cancelled, repository.getCollectors().toList().last())
    }
}
