package com.example.vinilosmobile.domain.validation

import com.example.vinilosmobile.domain.models.NewTrack
import com.example.vinilosmobile.domain.models.TrackField
import com.example.vinilosmobile.domain.models.TrackFieldError
import javax.inject.Inject

class TrackValidator @Inject constructor() {
    fun validate(track: NewTrack): Map<TrackField, TrackFieldError> = buildMap {
        if (track.name.isBlank()) put(TrackField.NAME, TrackFieldError.REQUIRED)
        val duration = track.duration.trim()
        if (duration.isEmpty()) put(TrackField.DURATION, TrackFieldError.REQUIRED)
        else if (!DURATION.matches(duration)) put(TrackField.DURATION, TrackFieldError.INVALID_DURATION)
    }

    private companion object {
        val DURATION = Regex("[0-9]+:[0-5][0-9]")
    }
}
