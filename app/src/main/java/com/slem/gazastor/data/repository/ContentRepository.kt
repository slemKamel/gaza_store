package com.slem.gazastor.data.repository

import com.slem.gazastor.data.model.BannerDto
import com.slem.gazastor.data.model.CategoryDto
import com.slem.gazastor.data.model.ItemDto
import com.slem.gazastor.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException

/**
 * Catalog access. Every call returns a [Result] instead of swallowing failures,
 * so the UI can tell "the store is empty" apart from "the network failed".
 */
interface ContentRepository {
    suspend fun getCategories(): Result<List<CategoryDto>>
    suspend fun getCategory(categoryId: String): Result<CategoryDto?>
    suspend fun getItems(): Result<List<ItemDto>>
    suspend fun getBanners(): Result<List<BannerDto>>
    suspend fun getItemsByCategory(categoryId: String): Result<List<ItemDto>>
    suspend fun searchItems(query: String): Result<List<ItemDto>>
}

class ContentRepositoryImpl(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : ContentRepository {

    override suspend fun getCategories(): Result<List<CategoryDto>> =
        safeCall { supabase.from("categories").select().decodeList<CategoryDto>() }

    override suspend fun getCategory(categoryId: String): Result<CategoryDto?> = safeCall {
        supabase.from("categories").select {
            filter {
                eq("id", categoryId)
            }
        }.decodeList<CategoryDto>().firstOrNull()
    }

    override suspend fun getItems(): Result<List<ItemDto>> =
        safeCall { supabase.from("items").select().decodeList<ItemDto>() }

    override suspend fun getBanners(): Result<List<BannerDto>> =
        safeCall { supabase.from("banners").select().decodeList<BannerDto>() }

    override suspend fun getItemsByCategory(categoryId: String): Result<List<ItemDto>> =
        safeCall {
            supabase.from("items").select {
                filter {
                    eq("category_id", categoryId)
                }
            }.decodeList<ItemDto>()
        }

    override suspend fun searchItems(query: String): Result<List<ItemDto>> {
        val term = query.trim()
        if (term.isEmpty()) return Result.success(emptyList())
        return safeCall {
            val pattern = "%${escapeLikeWildcards(term)}%"
            supabase.from("items").select {
                filter {
                    or {
                        ilike("title", pattern)
                        ilike("description", pattern)
                    }
                }
                limit(50)
            }.decodeList<ItemDto>()
        }
    }
}

/**
 * Escapes `%`, `_` and `\` so user input can't inject wildcards into an
 * `ilike` pattern. PostgREST treats `\` as the escape character by default.
 */
private fun escapeLikeWildcards(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")

/** Wraps a suspending call in [Result] without swallowing coroutine cancellation. */
private suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
