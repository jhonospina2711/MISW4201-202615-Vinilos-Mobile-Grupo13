package com.example.vinilosmobile.feature.albumcreate.models

import com.example.vinilosmobile.domain.models.NewAlbum
import com.example.vinilosmobile.feature.shared.DateTextFormatter

/** Values exactly as they appear on screen; the date keeps the dd/MM/yyyy format. */
data class AlbumForm(
    val name: String,
    val coverUrl: String,
    val releaseDate: String,
    val genre: String,
    val recordLabel: String,
    val description: String,
)

// An unreadable date is passed through as typed so the validator reports it.
fun AlbumForm.toNewAlbum(): NewAlbum = NewAlbum(
    name = name,
    coverUrl = coverUrl,
    releaseDate = DateTextFormatter.inputToIso(releaseDate) ?: releaseDate,
    genre = genre,
    recordLabel = recordLabel,
    description = description,
)
