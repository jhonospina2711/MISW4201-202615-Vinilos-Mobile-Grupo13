package com.example.vinilosmobile.feature.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateTextFormatterTest {
    @Test fun typedDateIsSentInApiFormat() {
        assertEquals("1984-08-01", DateTextFormatter.inputToIso("01/08/1984"))
        assertEquals("1984-08-01", DateTextFormatter.inputToIso(" 01/08/1984 "))
    }

    @Test fun impossibleOrIncompleteDatesAreRejected() {
        listOf("31/02/1984", "1/8/1984", "1984-08-01", "01/08/84", "").forEach {
            assertNull(it, DateTextFormatter.inputToIso(it))
        }
    }

    @Test fun pickerSelectionRoundTripsWithoutShiftingTheDay() {
        val millis = requireNotNull(DateTextFormatter.utcMillisFromInput("01/08/1984"))

        assertEquals(460_166_400_000L, millis)
        assertEquals("01/08/1984", DateTextFormatter.inputFromUtcMillis(millis))
    }
}
