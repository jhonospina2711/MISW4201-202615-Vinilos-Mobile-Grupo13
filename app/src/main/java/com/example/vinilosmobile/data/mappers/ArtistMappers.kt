package com.example.vinilosmobile.data.mappers

import com.example.vinilosmobile.data.network.models.ArtistAwardDto
import com.example.vinilosmobile.data.network.models.ArtistDto
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistAward
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistMember
import com.example.vinilosmobile.domain.models.ArtistType

fun ArtistDto.toDomain(type: ArtistType, awardsById: Map<Long, ArtistAwardDto> = emptyMap()): Artist? {
    val artistId = id ?: return null
    val title = name?.trim().orEmpty()
    if (title.isEmpty()) return null
    return Artist(
        key = ArtistKey(artistId, type),
        name = title,
        imageUrl = image?.trim()?.takeIf(String::isNotEmpty),
        description = description?.trim()?.takeIf(String::isNotEmpty),
        date = (if (type == ArtistType.MUSICIAN) birthDate else creationDate)?.take(10),
        albums = albums.orEmpty().mapNotNull { album ->
            album.toDomain()?.copy(artistNames = listOf(title))
        },
        members = if (type == ArtistType.BAND) musicians.orEmpty().mapNotNull { musician ->
            val memberId = musician.id ?: return@mapNotNull null
            val memberName = musician.name?.trim().orEmpty()
            if (memberName.isEmpty()) null else ArtistMember(memberId, memberName, musician.image)
        } else emptyList(),
        awards = performerPrizes.orEmpty().mapNotNull { award ->
            val awardId = award.id ?: return@mapNotNull null
            ArtistAward(
                id = awardId,
                name = (award.prize ?: awardsById[awardId]?.prize)?.name?.trim(),
                date = award.premiationDate?.take(10),
            )
        },
    )
}
