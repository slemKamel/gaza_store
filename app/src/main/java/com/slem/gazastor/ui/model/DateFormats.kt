package com.slem.gazastor.ui.model

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.US)

/**
 * Formats a PostgREST timestamp (ISO-8601 with offset) as `yyyy/MM/dd`.
 * Falls back to the raw date part if the value cannot be parsed.
 */
fun formatOrderDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return runCatching {
        OffsetDateTime.parse(raw).format(ORDER_DATE_FORMAT)
    }.getOrElse { raw.take(10) }
}
