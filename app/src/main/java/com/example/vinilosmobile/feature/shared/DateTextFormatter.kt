package com.example.vinilosmobile.feature.shared

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Conversión de fechas entre el formato de la API (`yyyy-MM-dd`), el del
 * formulario (`dd/MM/yyyy`) y el texto que se muestra en el detalle.
 *
 * Los [SimpleDateFormat] se crean una sola vez por hilo. Antes se instanciaban
 * en cada llamada y esta función se invoca por cada fila que se enlaza en los
 * adaptadores; con el caché se evitan tres asignaciones por fecha mostrada.
 * [SimpleDateFormat] no es seguro entre hilos, por eso el caché es [ThreadLocal].
 */
object DateTextFormatter {
    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")
    private val INPUT_DATE = Regex("""\d{2}/\d{2}/\d{4}""")

    private val ISO_DATE = threadLocalFormat("yyyy-MM-dd", Locale.US)
    private val LONG_DATE_SPANISH = threadLocalFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"))
    private val INPUT_DATE_LOCAL = threadLocalFormat("dd/MM/yyyy", Locale.US)
    private val INPUT_DATE_UTC = threadLocalFormat("dd/MM/yyyy", Locale.US, UTC)
    private val ISO_DATE_UTC = threadLocalFormat("yyyy-MM-dd", Locale.US, UTC)

    fun format(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val value = raw.take(10)
        val date = try {
            ISO_DATE.get().parse(value)
        } catch (_: Exception) {
            null
        }
        return date?.let { LONG_DATE_SPANISH.get().format(it) } ?: value
    }

    /** Turns the dd/MM/yyyy text of a form into the yyyy-MM-dd value the API expects. */
    fun inputToIso(text: String): String? {
        val date = parseInput(text, UTC) ?: return null
        return ISO_DATE_UTC.get().format(date)
    }

    // MaterialDatePicker works with midnight UTC, so both conversions use UTC.
    fun inputFromUtcMillis(millis: Long): String =
        INPUT_DATE_UTC.get().format(Date(millis))

    fun utcMillisFromInput(text: String): Long? = parseInput(text, UTC)?.time

    private fun parseInput(text: String, zone: TimeZone): Date? {
        val value = text.trim()
        if (!INPUT_DATE.matches(value)) return null
        val format = if (zone == UTC) INPUT_DATE_UTC.get() else INPUT_DATE_LOCAL.get()
        return try {
            format.parse(value)
        } catch (_: Exception) {
            null
        }
    }

    private fun threadLocalFormat(
        pattern: String,
        locale: Locale,
        zone: TimeZone? = null,
    ): ThreadLocal<SimpleDateFormat> = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat(pattern, locale).apply {
            isLenient = false
            if (zone != null) timeZone = zone
        }
    }
}
