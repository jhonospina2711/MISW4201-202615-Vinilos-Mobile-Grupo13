package com.example.vinilosmobile.data.mappers

import com.example.vinilosmobile.data.network.models.AlbumDto
import com.example.vinilosmobile.data.network.models.ArtistAwardDto
import com.example.vinilosmobile.data.network.models.ArtistDto
import com.example.vinilosmobile.data.network.models.PrizeDto
import com.example.vinilosmobile.domain.models.ArtistType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistMappersTest {
    @Test fun invalidArtistIsIgnored() {
        assertNull(ArtistDto(name = "Sin id").toDomain(ArtistType.MUSICIAN))
        assertNull(ArtistDto(id = 1, name = " ").toDomain(ArtistType.BAND))
    }

    @Test fun musicianUsesBirthDateAlbumsAndPrizeFallback() {
        val dto = ArtistDto(
            id = 3,
            name = "  Rubén Blades  ",
            birthDate = "1948-07-16T00:00:00Z",
            albums = listOf(AlbumDto(id = 7, name = "Buscando América")),
            performerPrizes = listOf(ArtistAwardDto(id = 5, premiationDate = "2000-01-02T00:00:00Z")),
            musicians = listOf(ArtistDto(id = 99, name = "No aplica")),
        )
        val awards = mapOf(5L to ArtistAwardDto(5, prize = PrizeDto(1, "Grammy")))

        val artist = requireNotNull(dto.toDomain(ArtistType.MUSICIAN, awards))

        assertEquals("Rubén Blades", artist.name)
        assertEquals("1948-07-16", artist.date)
        assertEquals(listOf("Rubén Blades"), artist.albums.single().artistNames)
        assertEquals("Grammy", artist.awards.single().name)
        assertEquals("2000-01-02", artist.awards.single().date)
        assertTrue(artist.members.isEmpty())
    }

    @Test fun bandUsesCreationDateAndValidMembersOnly() {
        val dto = ArtistDto(
            id = 4,
            name = " Queen ",
            creationDate = "1970-01-01T00:00:00Z",
            birthDate = "1940-01-01T00:00:00Z",
            musicians = listOf(
                ArtistDto(id = 11, name = " Freddie Mercury "),
                ArtistDto(id = null, name = "Sin id"),
                ArtistDto(id = 12, name = " "),
            ),
        )

        val artist = requireNotNull(dto.toDomain(ArtistType.BAND))

        assertEquals("Queen", artist.name)
        assertEquals("1970-01-01", artist.date)
        assertEquals(listOf("Freddie Mercury"), artist.members.map { it.name })
    }
}
