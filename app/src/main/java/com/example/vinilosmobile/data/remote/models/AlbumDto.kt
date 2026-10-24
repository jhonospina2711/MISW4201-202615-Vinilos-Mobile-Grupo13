package com.example.vinilosmobile.data.remote.models

/** Nullable fields reflect responses that have not yet been verified on the deployed API. */
data class AlbumDto(
    val id: Long? = null,
    val name: String? = null,
    val cover: String? = null,
    val releaseDate: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val recordLabel: String? = null,
    val performers: List<PerformerDto>? = null,
)

data class PerformerDto(
    val id: Long? = null,
    val name: String? = null,
)
