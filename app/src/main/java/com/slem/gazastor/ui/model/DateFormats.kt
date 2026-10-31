package com.slem.gazastor.ui.model

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.US)
private val MEMBER_SINCE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM", Locale.US)

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

/**
 * Formats the account creation timestamp as `yyyy/MM` (Western digits, matching
 * the order dates). Returns an empty string for a missing or unparseable value
 * so the account screen can hide the "عضو منذ" line instead of showing garbage.
 */
fun formatMemberSince(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return runCatching {
        OffsetDateTime.parse(raw).format(MEMBER_SINCE_FORMAT)
    }.getOrDefault("")
}
