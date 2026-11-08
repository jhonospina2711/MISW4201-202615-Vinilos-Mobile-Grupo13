package com.example.vinilosmobile.domain.validation

import com.example.vinilosmobile.domain.models.AlbumField
import com.example.vinilosmobile.domain.models.AlbumFieldError
import com.example.vinilosmobile.domain.models.NewAlbum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumValidatorTest {
    private val validator = AlbumValidator()
    private val valid = NewAlbum(
        name = "Siembra",
        coverUrl = "https://example.com/siembra.jpg",
        releaseDate = "1978-08-01",
        genre = "Salsa",
        recordLabel = "Fania Records",
        description = "Álbum de Willie Colón y Rubén Blades",
    )

    @Test fun completeAlbumHasNoErrors() {
        assertTrue(validator.validate(valid).isEmpty())
    }

    @Test fun everyEmptyFieldIsRequired() {
        val errors = validator.validate(NewAlbum("", " ", "", "", "", "\n"))

        assertEquals(AlbumField.entries.toSet(), errors.keys)
        assertTrue(errors.values.all { it == AlbumFieldError.REQUIRED })
    }

    @Test fun coverMustBeAWebAddress() {
        listOf("portada.jpg", "ftp://example.com/a.jpg", "https://", "https://ejemplo .com/a.jpg").forEach { cover ->
            assertEquals(
                cover,
                mapOf(AlbumField.COVER_URL to AlbumFieldError.INVALID_URL),
                validator.validate(valid.copy(coverUrl = cover)),
            )
        }
    }

    @Test fun releaseDateMustExistInTheCalendar() {
        listOf("1978-02-30", "1978-13-01", "01/08/1978", "1978-8-1").forEach { date ->
            assertEquals(
                date,
                mapOf(AlbumField.RELEASE_DATE to AlbumFieldError.INVALID_DATE),
                validator.validate(valid.copy(releaseDate = date)),
            )
        }
        assertTrue(validator.validate(valid.copy(releaseDate = "2024-02-29")).isEmpty())
    }

    @Test fun genreAndRecordLabelOnlyAcceptApiValues() {
        val errors = validator.validate(valid.copy(genre = "Jazz", recordLabel = "Sony"))

        assertEquals(
            mapOf(
                AlbumField.GENRE to AlbumFieldError.INVALID_OPTION,
                AlbumField.RECORD_LABEL to AlbumFieldError.INVALID_OPTION,
            ),
            errors,
        )
    }

    @Test fun surroundingSpacesAreIgnored() {
        val padded = valid.copy(name = "  Siembra ", genre = " Salsa ", releaseDate = " 1978-08-01 ")

        assertTrue(validator.validate(padded).isEmpty())
    }
}
