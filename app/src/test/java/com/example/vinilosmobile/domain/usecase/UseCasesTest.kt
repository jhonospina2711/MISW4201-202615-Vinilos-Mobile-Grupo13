package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumCatalog
import com.example.vinilosmobile.domain.models.AlbumSource
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.example.vinilosmobile.domain.repository.ArtistRepository
import com.example.vinilosmobile.domain.repository.UserRoleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
        coEvery { albums.getAlbums() } returns expected

        assertEquals(expected, GetAlbumsUseCase(albums)())
        coVerify(exactly = 1) { albums.getAlbums() }
    }

    @Test fun getAlbumPassesSelectedId() = runBlocking {
        coEvery { albums.getAlbum(1) } returns album

        assertEquals(album, GetAlbumUseCase(albums)(1))
        coVerify(exactly = 1) { albums.getAlbum(1) }
    }

    @Test fun getArtistsPassesCatalog() = runBlocking {
        coEvery { artists.getArtists() } returns listOf(artist)

        assertEquals(listOf(artist), GetArtistsUseCase(artists)())
    }

    @Test fun getArtistPassesTypeAndId() = runBlocking {
        coEvery { artists.getArtist(artistKey) } returns artist

        assertEquals(artist, GetArtistUseCase(artists)(artistKey))
        coVerify(exactly = 1) { artists.getArtist(artistKey) }
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
