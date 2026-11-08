package com.example.vinilosmobile.feature.albumcreate

import com.example.vinilosmobile.feature.albumcreate.models.AlbumForm
import com.example.vinilosmobile.feature.albumcreate.models.toNewAlbum
import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumFormTest {
    private val form = AlbumForm("Siembra", "https://example.com/a.jpg", "01/08/1978", "Salsa", "Fania Records", "Salsa")

    @Test fun dateIsConvertedForTheDomain() {
        assertEquals("1978-08-01", form.toNewAlbum().releaseDate)
    }

    @Test fun unreadableDateIsKeptSoValidationCanReportIt() {
        assertEquals("30/02/1978", form.copy(releaseDate = "30/02/1978").toNewAlbum().releaseDate)
    }
}
