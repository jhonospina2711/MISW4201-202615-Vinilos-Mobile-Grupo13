package com.example.vinilosmobile.data.mappers

import com.example.vinilosmobile.data.network.models.CollectorAlbumDto
import com.example.vinilosmobile.data.network.models.CollectorDto
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.domain.models.CollectorAlbum
import com.example.vinilosmobile.domain.models.CollectorComment
import com.example.vinilosmobile.domain.models.CollectorDetail
import com.example.vinilosmobile.domain.models.CollectorFavorite
import com.example.vinilosmobile.domain.models.CollectorSummary

fun CollectorDto.toSummary(): CollectorSummary? {
    val collectorId = id ?: return null
    val title = name?.trim().orEmpty()
    if (title.isEmpty()) return null
    return CollectorSummary(collectorId, title, collectorAlbums.orEmpty().size, favoritePerformers.orEmpty().size)
}

fun CollectorDto.toDetail(albums: List<CollectorAlbumDto>?): CollectorDetail? {
    val summary = toSummary() ?: return null
    return CollectorDetail(
        id = summary.id,
        name = summary.name,
        telephone = telephone?.trim()?.takeIf(String::isNotEmpty),
        email = email?.trim()?.takeIf(String::isNotEmpty),
        favorites = favoritePerformers.orEmpty().mapNotNull { performer ->
            val performerId = performer.id ?: return@mapNotNull null
            val performerName = performer.name?.trim()?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
            val type = if (performer.type.equals("Band", ignoreCase = true) || performer.creationDate != null)
                ArtistType.BAND else ArtistType.MUSICIAN
            CollectorFavorite(ArtistKey(performerId, type), performerName, performer.image)
        },
        albums = albums?.mapNotNull { entry ->
            val album = entry.album?.toDomain() ?: return@mapNotNull null
            CollectorAlbum(album, entry.price, entry.status)
        },
        comments = comments.orEmpty().mapNotNull { comment ->
            val commentId = comment.id ?: return@mapNotNull null
            val text = comment.description?.trim()?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
            CollectorComment(commentId, text, comment.rating)
        },
    )
}
