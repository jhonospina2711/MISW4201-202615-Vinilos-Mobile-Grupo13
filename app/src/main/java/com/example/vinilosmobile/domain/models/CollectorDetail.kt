package com.example.vinilosmobile.domain.models

data class CollectorDetail(
    val id: Long,
    val name: String,
    val telephone: String?,
    val email: String?,
    val favorites: List<CollectorFavorite>,
    /** Null means the separate album request failed; an empty list means no albums. */
    val albums: List<CollectorAlbum>?,
    val comments: List<CollectorComment>,
)
