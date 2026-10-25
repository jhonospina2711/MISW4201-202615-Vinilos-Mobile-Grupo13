package com.example.vinilosmobile.data.network

import com.example.vinilosmobile.data.network.models.ArtistAwardDto
import com.example.vinilosmobile.data.network.models.ArtistDto
import com.example.vinilosmobile.data.network.models.PrizeDto
import com.example.vinilosmobile.data.network.services.ArtistApiService
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistServiceAdapterTest {
    private val service = mockk<ArtistApiService>()
    private val adapter = ArtistServiceAdapter(service)

    @Test fun combinesMusiciansAndBandsSortedById() = runBlocking {
        coEvery { service.getMusicians() } returns listOf(ArtistDto(id = 9, name = "Músico"))
        coEvery { service.getBands() } returns listOf(ArtistDto(id = 2, name = "Banda"))

        val artists = adapter.getArtists()

        assertEquals(listOf(2L, 9L), artists.map { it.key.id })
        assertEquals(listOf(ArtistType.BAND, ArtistType.MUSICIAN), artists.map { it.key.type })
        coVerify(exactly = 1) { service.getMusicians() }
        coVerify(exactly = 1) { service.getBands() }
    }

    @Test fun skipsInvalidRecordsWithoutDroppingValidArtists() = runBlocking {
        coEvery { service.getMusicians() } returns listOf(
            ArtistDto(id = null, name = "Sin ID"),
            ArtistDto(id = 2, name = "  "),
            ArtistDto(id = 3, name = " Válido "),
        )
        coEvery { service.getBands() } returns emptyList()

        val artists = adapter.getArtists()

        assertEquals(listOf("Válido"), artists.map { it.name })
    }

    @Test fun musicianDetailResolvesAwardNameFromPrizeCatalog() = runBlocking {
        coEvery { service.getMusician(3) } returns ArtistDto(
            id = 3,
            name = "Músico",
            performerPrizes = listOf(ArtistAwardDto(id = 12, premiationDate = "2000-02-01T00:00:00Z")),
        )
        coEvery { service.getPerformerPrizes() } returns listOf(
            ArtistAwardDto(id = 12, prize = PrizeDto(id = 4, name = "Grammy")),
        )

        val detail = adapter.getArtist(ArtistKey(3, ArtistType.MUSICIAN))

        assertEquals("Grammy", detail.awards.single().name)
        assertEquals("2000-02-01", detail.awards.single().date)
        coVerify(exactly = 0) { service.getBand(any()) }
    }

    @Test fun bandDetailUsesBandEndpointAndMembers() = runBlocking {
        coEvery { service.getBand(4) } returns ArtistDto(
            id = 4,
            name = "Banda",
            creationDate = "1980-01-02T00:00:00Z",
            musicians = listOf(ArtistDto(id = 5, name = "Integrante")),
        )

        val detail = adapter.getArtist(ArtistKey(4, ArtistType.BAND))

        assertEquals("1980-01-02", detail.date)
        assertEquals("Integrante", detail.members.single().name)
        coVerify(exactly = 0) { service.getMusician(any()) }
        coVerify(exactly = 0) { service.getPerformerPrizes() }
    }

    @Test fun prizeEndpointFailureDoesNotHideArtistDetail() = runBlocking {
        coEvery { service.getMusician(3) } returns ArtistDto(
            id = 3, name = "Músico", performerPrizes = listOf(ArtistAwardDto(id = 12)),
        )
        coEvery { service.getPerformerPrizes() } throws IllegalStateException("prizes down")

        val detail = adapter.getArtist(ArtistKey(3, ArtistType.MUSICIAN))

        assertEquals(1, detail.awards.size)
        assertTrue(detail.awards.single().name == null)
    }
}
