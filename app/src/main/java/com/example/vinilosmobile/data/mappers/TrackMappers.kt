package com.example.vinilosmobile.data.mappers

import com.example.vinilosmobile.data.network.models.NewTrackDto
import com.example.vinilosmobile.data.network.models.TrackDto
import com.example.vinilosmobile.domain.models.NewTrack
import com.example.vinilosmobile.domain.models.Track

fun NewTrack.toDto(): NewTrackDto = NewTrackDto(name.trim(), duration.trim())

fun TrackDto.toDomain(): Track? {
    val trackId = id ?: return null
    val title = name?.trim().orEmpty()
    if (title.isEmpty()) return null
    return Track(trackId, title, duration?.trim().orEmpty())
}
