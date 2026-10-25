package com.example.vinilosmobile.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Long,
    val position: Int,
    val inCatalog: Boolean,
    val name: String,
    val artistNamesJson: String,
    val genre: String?,
    val releaseYear: String?,
    val coverUrl: String?,
    val recordLabel: String?,
    val description: String?,
)
