package com.example.vinilosmobile.domain.models

data class Artist(
    val key: ArtistKey,
    val name: String,
    val imageUrl: String?,
    val description: String?,
    val date: String?,
    val albums: List<Album>,
    val members: List<ArtistMember>,
    val awards: List<ArtistAward>,
)
