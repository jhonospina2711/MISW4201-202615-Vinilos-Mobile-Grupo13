package com.example.vinilosmobile.feature.shared

import java.text.SimpleDateFormat
import java.util.Locale

object DateTextFormatter {
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
}
