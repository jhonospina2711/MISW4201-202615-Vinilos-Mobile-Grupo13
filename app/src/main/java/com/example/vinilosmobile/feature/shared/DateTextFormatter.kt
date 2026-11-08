package com.example.vinilosmobile.feature.shared

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTextFormatter {
    private val INPUT_DATE = Regex("""\d{2}/\d{2}/\d{4}""")

    fun format(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val value = raw.take(10)
        val date = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value)
        } catch (_: Exception) {
            null
        }
        return date?.let {
            SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO")).format(it)
        } ?: value
    }

    /** Turns the dd/MM/yyyy text of a form into the yyyy-MM-dd value the API expects. */
    fun inputToIso(text: String): String? {
        val date = parseInput(text, TimeZone.getDefault()) ?: return null
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    }

    // MaterialDatePicker works with midnight UTC, so both conversions use UTC.
    fun inputFromUtcMillis(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { timeZone = UTC }.format(Date(millis))

    fun utcMillisFromInput(text: String): Long? = parseInput(text, UTC)?.time

    private fun parseInput(text: String, zone: TimeZone): Date? {
        val value = text.trim()
        if (!INPUT_DATE.matches(value)) return null
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
                isLenient = false
                timeZone = zone
            }.parse(value)
        } catch (_: Exception) {
            null
        }
    }

    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")
}
