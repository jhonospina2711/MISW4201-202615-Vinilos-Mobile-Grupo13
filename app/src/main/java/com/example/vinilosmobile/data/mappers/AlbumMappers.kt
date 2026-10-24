package com.example.vinilosmobile.data.mappers

import com.example.vinilosmobile.data.local.models.AlbumEntity
import com.example.vinilosmobile.data.remote.models.AlbumDto
import com.example.vinilosmobile.domain.models.Album
import com.google.gson.Gson

fun AlbumDto.toDomain(): Album? {
    val albumId = id ?: return null
    val title = name?.trim().orEmpty()
    if (title.isEmpty()) return null
    return Album(
        id = albumId,
        name = title,
        artistNames = performers.orEmpty().mapNotNull { it.name?.trim()?.takeIf(String::isNotEmpty) },
        genre = genre?.trim()?.takeIf(String::isNotEmpty),
        releaseYear = releaseDate?.take(4)?.takeIf { it.length == 4 && it.all(Char::isDigit) },
        coverUrl = cover?.trim()?.takeIf(String::isNotEmpty),
        recordLabel = recordLabel?.trim()?.takeIf(String::isNotEmpty),
        description = description?.trim()?.takeIf(String::isNotEmpty),
    )
}

fun Album.toEntity(position: Int, inCatalog: Boolean, gson: Gson): AlbumEntity = AlbumEntity(
    id = id,
    position = position,
    inCatalog = inCatalog,
    name = name,
    artistNamesJson = gson.toJson(artistNames),
    genre = genre,
    releaseYear = releaseYear,
    coverUrl = coverUrl,
    recordLabel = recordLabel,
    description = description,
)

fun AlbumEntity.toDomain(gson: Gson): Album = Album(
    id = id,
    name = name,
    artistNames = gson.fromJson(artistNamesJson, Array<String>::class.java).toList(),
    genre = genre,
    releaseYear = releaseYear,
    coverUrl = coverUrl,
    recordLabel = recordLabel,
    description = description,
)
