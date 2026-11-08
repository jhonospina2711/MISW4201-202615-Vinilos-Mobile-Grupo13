package com.example.vinilosmobile.domain.models

/** Data typed by the collector. [releaseDate] uses the yyyy-MM-dd format expected by the API. */
data class NewAlbum(
    val name: String,
    val coverUrl: String,
    val releaseDate: String,
    val genre: String,
    val recordLabel: String,
    val description: String,
)
