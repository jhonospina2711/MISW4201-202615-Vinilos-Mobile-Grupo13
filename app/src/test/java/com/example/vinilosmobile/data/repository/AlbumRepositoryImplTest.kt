package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.local.AlbumDao
import com.example.vinilosmobile.data.mappers.toEntity
import com.example.vinilosmobile.data.network.errors.NetworkErrorMapper
import com.example.vinilosmobile.data.network.models.AlbumDto
import com.example.vinilosmobile.data.network.models.PerformerDto
import com.example.vinilosmobile.data.network.services.AlbumApiService
import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumSource
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.repository.AlbumNotFoundException
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.net.ConnectException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AlbumRepositoryImplTest {
    private val service = mockk<AlbumApiService>()
    private val dao = mockk<AlbumDao>()
    private val gson = Gson()
    private lateinit var repository: AlbumRepositoryImpl

    @Before fun setUp() {
        repository = AlbumRepositoryImpl(service, dao, gson, NetworkErrorMapper())
    }

    @Test fun remoteCatalogReplacesRoomAndReportsRemoteSource() = runBlocking {
        coEvery { service.getAlbums() } returns listOf(dto(2, " Segundo "), dto(1, "Primero"))
        coEvery { dao.replaceAll(any()) } returns Unit

        val catalog = repository.getAlbums()

        assertEquals(AlbumSource.REMOTE, catalog.source)
        assertEquals(listOf(2L, 1L), catalog.albums.map { it.id })
        assertEquals("Segundo", catalog.albums.first().name)
        coVerify(exactly = 1) {
            dao.replaceAll(match { rows ->
                rows.map { it.id } == listOf(2L, 1L) &&
                    rows.map { it.position } == listOf(0, 1) && rows.all { it.inCatalog }
            })
        }
    }

    @Test fun networkFailureReturnsRoomCatalogInStoredOrder() = runBlocking {
        coEvery { service.getAlbums() } throws ConnectException("offline")
        coEvery { dao.getAll() } returns listOf(
            album(3, "Guardado").toEntity(0, true, gson),
            album(4, "Otro").toEntity(1, true, gson),
        )

        val catalog = repository.getAlbums()

        assertEquals(AlbumSource.CACHE, catalog.source)
        assertEquals(listOf(3L, 4L), catalog.albums.map { it.id })
        coVerify(exactly = 1) { dao.getAll() }
    }

    @Test fun emptyRemoteCatalogReplacesOldRoomRows() = runBlocking {
        coEvery { service.getAlbums() } returns emptyList()
        coEvery { dao.replaceAll(emptyList()) } returns Unit

        val catalog = repository.getAlbums()

        assertEquals(AlbumSource.REMOTE, catalog.source)
        assertEquals(emptyList<Album>(), catalog.albums)
        coVerify(exactly = 1) { dao.replaceAll(emptyList()) }
        coVerify(exactly = 0) { dao.getAll() }
    }

    @Test fun networkFailureWithoutRoomDataBecomesFriendlyFailureKind() = runBlocking {
        coEvery { service.getAlbums() } throws ConnectException("offline")
        coEvery { dao.getAll() } returns emptyList()

        val failure = assertThrows(RequestFailure::class.java) {
            runBlocking { repository.getAlbums() }
        }
        assertEquals(RequestFailureKind.NO_CONNECTION, failure.kind)
    }

    @Test fun remoteDetailUpdatesRoomWithoutAddingItToCatalog() = runBlocking {
        coEvery { service.getAlbum(7) } returns dto(7, "Detalle")
        coEvery { dao.getById(7) } returns null
        coEvery { dao.upsert(any()) } returns Unit

        val detail = repository.getAlbum(7)

        assertEquals(7L, detail.id)
        coVerify(exactly = 1) {
            dao.upsert(match { it.id == 7L && !it.inCatalog && it.position == Int.MAX_VALUE })
        }
    }

    @Test fun networkFailureReturnsCachedDetail() = runBlocking {
        coEvery { service.getAlbum(7) } throws ConnectException("offline")
        coEvery { dao.getById(7) } returns album(7, "Guardado").toEntity(0, true, gson)

        val detail = repository.getAlbum(7)
        assertEquals("Guardado", detail.name)
        assertEquals(null, detail.tracks)
        assertEquals(null, detail.comments)
    }

    @Test fun notFoundDoesNotUseStaleCachedDetail() = runBlocking {
        coEvery { service.getAlbum(7) } throws HttpException(
            Response.error<String>(404, "missing".toResponseBody()),
        )

        assertThrows(AlbumNotFoundException::class.java) {
            runBlocking { repository.getAlbum(7) }
        }
        coVerify(exactly = 0) { dao.getById(any()) }
    }

    @Test fun cancelledRequestIsPropagatedWithoutReadingRoom() = runBlocking {
        coEvery { service.getAlbums() } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { repository.getAlbums() }
        }
        coVerify(exactly = 0) { dao.getAll() }
    }

    private fun dto(id: Long, name: String) = AlbumDto(
        id = id,
        name = name,
        performers = listOf(PerformerDto(9, "Artista")),
        releaseDate = "1984-08-01T00:00:00.000Z",
    )

    private fun album(id: Long, name: String) = Album(
        id = id,
        name = name,
        artistNames = listOf("Artista"),
        genre = "Salsa",
        releaseYear = "1984",
        coverUrl = null,
        recordLabel = null,
        description = null,
    )
}
