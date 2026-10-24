package com.example.vinilosmobile.domain.models

data class Album(
    val id: Long,
    val name: String,
    val artistNames: List<String>,
    val genre: String?,
    val releaseYear: String?,
    val coverUrl: String?,
    val recordLabel: String?,
    val description: String?,
)
