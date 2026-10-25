package com.example.vinilosmobile.domain.models

data class Album(
    val id: Long,
    val name: String,
    val artistNames: List<String>,
    val genre: String?,
    val releaseDate: String? = null,
    val releaseYear: String?,
    val coverUrl: String?,
    val recordLabel: String?,
    val description: String?,
    /** Null means the related data was unavailable, for example in an older Room cache entry. */
    val tracks: List<Track>? = null,
    val comments: List<AlbumComment>? = null,
)
