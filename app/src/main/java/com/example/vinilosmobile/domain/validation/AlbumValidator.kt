package com.example.vinilosmobile.domain.validation

import com.example.vinilosmobile.domain.models.AlbumField
import com.example.vinilosmobile.domain.models.AlbumFieldError
import com.example.vinilosmobile.domain.models.AlbumOptions
import com.example.vinilosmobile.domain.models.NewAlbum
import java.net.URI
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AlbumValidator @Inject constructor() {
    fun validate(album: NewAlbum): Map<AlbumField, AlbumFieldError> {
        val errors = linkedMapOf<AlbumField, AlbumFieldError>()
        fun check(field: AlbumField, value: String, rule: (String) -> AlbumFieldError?) {
            val text = value.trim()
            val error = if (text.isEmpty()) AlbumFieldError.REQUIRED else rule(text)
            if (error != null) errors[field] = error
        }
        check(AlbumField.NAME, album.name) { null }
        check(AlbumField.COVER_URL, album.coverUrl) { if (isWebUrl(it)) null else AlbumFieldError.INVALID_URL }
        check(AlbumField.RELEASE_DATE, album.releaseDate) { if (isDate(it)) null else AlbumFieldError.INVALID_DATE }
        check(AlbumField.GENRE, album.genre) {
            if (it in AlbumOptions.GENRES) null else AlbumFieldError.INVALID_OPTION
        }
        check(AlbumField.RECORD_LABEL, album.recordLabel) {
            if (it in AlbumOptions.RECORD_LABELS) null else AlbumFieldError.INVALID_OPTION
        }
        check(AlbumField.DESCRIPTION, album.description) { null }
        return errors
    }

    private fun isWebUrl(value: String): Boolean = try {
        val uri = URI(value)
        (uri.scheme == "http" || uri.scheme == "https") && !uri.host.isNullOrBlank()
    } catch (_: Exception) {
        false
    }

    private fun isDate(value: String): Boolean {
        if (!ISO_DATE.matches(value)) return false
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val position = ParsePosition(0)
        return format.parse(value, position) != null && position.index == value.length
    }

    private companion object {
        val ISO_DATE = Regex("""\d{4}-\d{2}-\d{2}""")
    }
}
