package com.slem.gazastor.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.slem.gazastor.data.repository.FALLBACK_WHATSAPP_NUMBER

private const val MIN_WHATSAPP_DIGITS = 11
private const val MAX_WHATSAPP_DIGITS = 15

/**
 * Resolves the WhatsApp number to dial: the configured value when it is a
 * plausible international number, otherwise the bundled fallback. Both values
 * are validated so a malformed setting can never produce a broken `wa.me` URL.
 */
internal fun resolveWhatsAppNumber(configured: String?): String? =
    configured?.takeIf(::isValidWhatsAppNumber)
        ?: FALLBACK_WHATSAPP_NUMBER.takeIf(::isValidWhatsAppNumber)

/**
 * A number is usable when it is 11-15 ASCII digits: no `+`, spaces or dashes,
 * because `wa.me` expects the international number without punctuation.
 */
internal fun isValidWhatsAppNumber(value: String): Boolean =
    value.length in MIN_WHATSAPP_DIGITS..MAX_WHATSAPP_DIGITS &&
        value.all { it in '0'..'9' }

/**
 * Opens WhatsApp with [message] pre-filled. Uses the same `https://wa.me/...`
 * ACTION_VIEW hand-over as the payment flow, so any app registered for the link
 * (WhatsApp, WhatsApp Business, or a browser) can take it.
 *
 * Returns false when the number is unusable or no app accepts the intent; the
 * caller decides what to show. No message content is ever persisted or logged.
 */
internal fun openWhatsApp(context: Context, number: String, message: String): Boolean {
    if (!isValidWhatsAppNumber(number)) return false

    val uri = "https://wa.me/$number?text=${Uri.encode(message)}".toUri()

    return try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
