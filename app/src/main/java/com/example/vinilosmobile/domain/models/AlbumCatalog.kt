package com.example.vinilosmobile.domain.models

enum class AlbumSource { REMOTE, CACHE }

data class AlbumCatalog(
    val albums: List<Album>,
    val source: AlbumSource,
)
