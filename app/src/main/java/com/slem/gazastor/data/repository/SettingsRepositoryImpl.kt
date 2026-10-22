package com.slem.gazastor.data.repository

import com.slem.gazastor.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException

/**
 * Reads the WhatsApp number from [public.app_settings] with in-memory
 * session caching and a hardcoded fallback so ordering never breaks even
 * if the DB is unavailable or misconfigured.
 */
class SettingsRepositoryImpl(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : SettingsRepository {

    /**
     * Cached for the process lifetime so we hit the DB at most once per
     * app start. Updated on first successful read.
     */
    @Volatile
    private var cached: String? = null

    override suspend fun getWhatsAppNumber(): Result<String> {
        if (cached != null) return Result.success(cached!!)

        return safeCall {
            val row = supabase.from("app_settings")
                .select {
                    filter {
                        eq("key", "whatsapp_number")
                    }
                    limit(1)
                }
                .decodeList<AppSettingsRow>()
                .firstOrNull()

            val number = row?.value
                ?.takeIf { it.all { it.isDigit() } }
                ?.takeIf { it.length in 11..15 }

            val result = number ?: FALLBACK_WHATSAPP_NUMBER
            cached = result
            result
        }
    }
}

/** Minimal DTO for the app_settings table. */
private data class AppSettingsRow(
    val key: String,
    val value: String
)

/**
 * Wraps a suspending call in [Result] without swallowing coroutine cancellation.
 */
private suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }