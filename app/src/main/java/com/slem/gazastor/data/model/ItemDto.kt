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
    val updated_at: String? = null
)