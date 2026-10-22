package com.slem.gazastor.data.repository

/**
 * Last-resort WhatsApp number, used only when the setting is missing, invalid,
 * or the request fails. Keep in sync with the seed in `supabase/schema.sql`.
 * It is never logged.
 */
internal const val FALLBACK_WHATSAPP_NUMBER = "972567982362"

/**
 * Runtime configuration stored in the database. Currently only the WhatsApp
 * number is supported, but the table is generic so other settings can be added
 * later without schema changes.
 */
interface SettingsRepository {
    /** Reads the WhatsApp number configured for the store. */
    suspend fun getWhatsAppNumber(): Result<String>
}