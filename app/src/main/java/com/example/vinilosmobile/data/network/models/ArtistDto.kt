package com.example.vinilosmobile.data.network.models

data class ArtistDto(
    val id: Long? = null,
    val name: String? = null,
    val image: String? = null,
    val description: String? = null,
    val birthDate: String? = null,
    val creationDate: String? = null,
    val albums: List<AlbumDto>? = null,
    val musicians: List<ArtistDto>? = null,
    val performerPrizes: List<ArtistAwardDto>? = null,
)
