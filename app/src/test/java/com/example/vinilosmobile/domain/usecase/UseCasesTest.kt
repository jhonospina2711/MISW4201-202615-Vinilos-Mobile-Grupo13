package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumCatalog
import com.example.vinilosmobile.domain.models.AlbumSource
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.example.vinilosmobile.domain.repository.ArtistRepository
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UseCasesTest {
    private val albums = mockk<AlbumRepository>()
    private val artists = mockk<ArtistRepository>()
    private val roles = mockk<UserRoleRepository>()
    private val album = Album(1, "Álbum", emptyList(), null, releaseYear = null,
        coverUrl = null, recordLabel = null, description = null)
    private val artistKey = ArtistKey(2, ArtistType.BAND)
    private val artist = Artist(artistKey, "Banda", null, null, null,
        emptyList(), emptyList(), emptyList())

    @Test fun getAlbumsPreservesSourceInformation() = runBlocking {
        val expected = AlbumCatalog(listOf(album), AlbumSource.CACHE)
        every { albums.getAlbums() } returns flowOf(DataResult.Loading, DataResult.Success(expected))

        assertEquals(
            listOf(DataResult.Loading, DataResult.Success(expected)),
            GetAlbumsUseCase(albums)().toList(),
        )
        verify(exactly = 1) { albums.getAlbums() }
    }

    @Test fun getAlbumPassesSelectedId() = runBlocking {
        every { albums.getAlbum(1) } returns flowOf(DataResult.Success(album))

        assertEquals(listOf(DataResult.Success(album)), GetAlbumUseCase(albums)(1).toList())
        verify(exactly = 1) { albums.getAlbum(1) }
    }

    @Test fun getArtistsPassesCatalog() = runBlocking {
        every { artists.getArtists() } returns flowOf(DataResult.Success(listOf(artist)))

        assertEquals(listOf(DataResult.Success(listOf(artist))), GetArtistsUseCase(artists)().toList())
    }

    @Test fun getArtistPassesTypeAndId() = runBlocking {
        every { artists.getArtist(artistKey) } returns flowOf(DataResult.Success(artist))

        assertEquals(listOf(DataResult.Success(artist)), GetArtistUseCase(artists)(artistKey).toList())
        verify(exactly = 1) { artists.getArtist(artistKey) }
    }

    @Test fun getRoleExposesPersistedFlow() = runBlocking {
        every { roles.selectedRole } returns flowOf(null, UserRole.COLLECTOR)

        assertEquals(listOf(null, UserRole.COLLECTOR), GetUserRoleUseCase(roles)().toList())
    }

    @Test fun setRoleDelegatesSelection() = runBlocking {
        coEvery { roles.setRole(UserRole.VISITOR) } returns Unit

        SetUserRoleUseCase(roles)(UserRole.VISITOR)

        coVerify(exactly = 1) { roles.setRole(UserRole.VISITOR) }
    }
}
