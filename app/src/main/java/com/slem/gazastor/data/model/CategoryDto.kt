package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val image_url: String? = null,
    val created_at: String? = null
)