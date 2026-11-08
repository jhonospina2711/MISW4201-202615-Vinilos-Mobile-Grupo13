package com.example.vinilosmobile.domain.models

/** BackVynils rejects an album whose genre or record label is outside these lists. */
object AlbumOptions {
    val GENRES = listOf("Classical", "Salsa", "Rock", "Folk")
    val RECORD_LABELS = listOf("Sony Music", "EMI", "Discos Fuentes", "Elektra", "Fania Records")
}
