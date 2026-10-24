package com.example.vinilosmobile.data.network.models

data class AlbumDto(
    val id: Long? = null,
    val name: String? = null,
    val cover: String? = null,
    val releaseDate: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val recordLabel: String? = null,
    val performers: List<PerformerDto>? = null,
    val tracks: List<TrackDto>? = null,
    val comments: List<AlbumCommentDto>? = null,
)
