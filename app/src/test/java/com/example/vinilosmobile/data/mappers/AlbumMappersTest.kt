package com.example.vinilosmobile.data.mappers

import com.example.vinilosmobile.data.network.models.AlbumCommentDto
import com.example.vinilosmobile.data.network.models.AlbumDto
import com.example.vinilosmobile.data.network.models.PerformerDto
import com.example.vinilosmobile.data.network.models.TrackDto
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlbumMappersTest {
    @Test fun missingIdOrBlankTitleIsIgnored() {
        assertNull(AlbumDto(name = "Sin id").toDomain())
        assertNull(AlbumDto(id = 1, name = "  ").toDomain())
    }

    @Test fun mapsValidAlbumAndFiltersBrokenRelatedRecords() {
        val dto = AlbumDto(
            id = 8,
            name = "  Buscando América  ",
            releaseDate = "1984-08-01T00:00:00.000Z",
            performers = listOf(PerformerDto(1, " Rubén Blades "), PerformerDto(2, " ")),
            tracks = listOf(TrackDto(4, " Decisiones ", "5:05"), TrackDto(null, "Sin id")),
            comments = listOf(AlbumCommentDto(7, " Gran disco ", 5), AlbumCommentDto(8, "  ")),
        )

        val album = requireNotNull(dto.toDomain())

        assertEquals("Buscando América", album.name)
        assertEquals(listOf("Rubén Blades"), album.artistNames)
        assertEquals("1984-08-01", album.releaseDate)
        assertEquals("1984", album.releaseYear)
        assertEquals("Decisiones", album.tracks?.single()?.name)
        assertEquals("Gran disco", album.comments?.single()?.description)
    }

    @Test fun roomRoundTripPreservesBasicFieldsAndMarksMissingDetailData() {
        val gson = Gson()
        val original = requireNotNull(AlbumDto(
            id = 9,
            name = "Álbum",
            performers = listOf(PerformerDto(2, "Artista")),
            tracks = listOf(TrackDto(3, "Track", "3:00")),
            comments = listOf(AlbumCommentDto(4, "Comentario", 4)),
        ).toDomain())

        val entity = original.toEntity(position = 2, inCatalog = true, gson = gson)
        val restored = entity.toDomain(gson)

        assertEquals(9L, entity.id)
        assertEquals(2, entity.position)
        assertEquals(listOf("Artista"), restored.artistNames)
        assertNull(restored.tracks)
        assertNull(restored.comments)
        assertNull(restored.releaseDate)
    }
}
