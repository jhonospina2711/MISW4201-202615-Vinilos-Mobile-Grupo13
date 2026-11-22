package com.example.vinilosmobile.data.network.models

data class CollectorDto(
    val id: Long? = null,
    val name: String? = null,
    val telephone: String? = null,
    val email: String? = null,
    val favoritePerformers: List<ArtistDto>? = null,
    val collectorAlbums: List<CollectorAlbumDto>? = null,
    val comments: List<AlbumCommentDto>? = null,
)
