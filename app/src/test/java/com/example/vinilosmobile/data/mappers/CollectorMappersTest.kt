package com.example.vinilosmobile.data.mappers

import com.example.vinilosmobile.data.network.models.AlbumCommentDto
import com.example.vinilosmobile.data.network.models.AlbumDto
import com.example.vinilosmobile.data.network.models.ArtistDto
import com.example.vinilosmobile.data.network.models.CollectorAlbumDto
import com.example.vinilosmobile.data.network.models.CollectorDto
import com.example.vinilosmobile.domain.models.ArtistType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CollectorMappersTest {
    @Test fun invalidCollectorIsNotShown() {
        assertNull(CollectorDto(name = "Sin id").toSummary())
        assertNull(CollectorDto(id = 2, name = " ").toDetail(emptyList()))
    }

    @Test fun summaryCountsRelationsAndDetailMapsFavoritesAlbumsAndComments() {
        val dto = CollectorDto(
            id = 7, name = "  Carlos Molina ", telephone = " 3101234567 ", email = " carlos@example.com ",
            favoritePerformers = listOf(
                ArtistDto(id = 1, name = "Rubén Blades", birthDate = "1948-07-16"),
                ArtistDto(id = 2, name = "Queen", type = "Band"),
                ArtistDto(name = "Incompleto"),
            ),
            collectorAlbums = listOf(CollectorAlbumDto(id = 11)),
            comments = listOf(AlbumCommentDto(30, "  Gran colección ", 5)),
        )
        val detail = requireNotNull(dto.toDetail(listOf(
            CollectorAlbumDto(11, 65000.0, "Active", AlbumDto(id = 100, name = "Buscando América")),
            CollectorAlbumDto(12, 0.0, "Inactive", AlbumDto(name = "Inválido")),
        )))

        assertEquals("Carlos Molina", dto.toSummary()?.name)
        assertEquals(1, dto.toSummary()?.albumCount)
        assertEquals(3, dto.toSummary()?.favoriteCount)
        assertEquals("3101234567", detail.telephone)
        assertEquals(listOf(ArtistType.MUSICIAN, ArtistType.BAND), detail.favorites.map { it.key.type })
        assertEquals("Buscando América", detail.albums?.single()?.album?.name)
        assertEquals("Gran colección", detail.comments.single().description)
    }

    @Test fun unavailableAlbumEndpointIsDistinctFromEmptyCollection() {
        val dto = CollectorDto(id = 7, name = "Carlos")
        assertNull(dto.toDetail(null)?.albums)
        assertEquals(emptyList<Any>(), dto.toDetail(emptyList())?.albums)
    }
}
