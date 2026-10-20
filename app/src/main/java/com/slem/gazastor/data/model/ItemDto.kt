package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: String? = null,
    val category_id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val image_url: String? = null,
    val images: List<String>? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    /**
     * The embedded category. PostgREST returns a single object here (not a list)
     * because `items.category_id` points at `categories.id`, and only the
     * `icon_key` column is selected - enough for the image fallback glyph.
     * Null when the query did not ask for the relation.
     */
    val categories: CategoryDto? = null
)