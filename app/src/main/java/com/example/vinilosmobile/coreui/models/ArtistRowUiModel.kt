package com.example.vinilosmobile.coreui.models

import com.example.vinilosmobile.domain.models.ArtistKey

data class ArtistRowUiModel(
    val key: ArtistKey,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
)
