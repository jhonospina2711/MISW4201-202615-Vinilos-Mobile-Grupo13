package com.example.vinilosmobile.domain.validation

import com.example.vinilosmobile.domain.models.NewTrack
import com.example.vinilosmobile.domain.models.TrackField
import com.example.vinilosmobile.domain.models.TrackFieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackValidatorTest {
    private val validator = TrackValidator()

    @Test fun blankFieldsAreRequired() {
        assertEquals(mapOf(
            TrackField.NAME to TrackFieldError.REQUIRED,
            TrackField.DURATION to TrackFieldError.REQUIRED,
        ), validator.validate(NewTrack("  ", " ")))
    }

    @Test fun durationRequiresMinutesAndTwoSecondsBelowSixty() {
        listOf("7:60", "7:2", "7:021", "abc", "-1:20").forEach { invalid ->
            assertEquals(TrackFieldError.INVALID_DURATION, validator.validate(NewTrack("Canción", invalid))[TrackField.DURATION])
        }
        listOf("0:00", "7:21", "123:59").forEach { valid ->
            assertTrue(validator.validate(NewTrack("Canción", valid)).isEmpty())
        }
    }
}
