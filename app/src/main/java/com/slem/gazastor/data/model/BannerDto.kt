package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BannerDto(
    val id: String? = null,
    val title: String? = null,
    val image_url: String? = null,
    val link: String? = null,
    val created_at: String? = null
)