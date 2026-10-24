package com.example.vinilosmobile.coreui.models

import android.content.Context
import com.example.vinilosmobile.R
import com.example.vinilosmobile.domain.models.Album

fun Album.toCardUiModel(context: Context): AlbumCardUiModel = AlbumCardUiModel(
    id = id,
    title = name,
    artist = artistNames.joinToString().ifBlank { context.getString(R.string.album_no_artist) },
    metadata = listOf(
        genre ?: context.getString(R.string.album_no_genre),
        releaseYear ?: context.getString(R.string.album_no_year),
    ).joinToString(" · "),
    coverUrl = coverUrl,
)
