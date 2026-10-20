package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BannerDto(
    val id: String? = null,
    val title: String? = null,
    val image_url: String? = null,
    val link: String? = null,
    val created_at: String? = null,
    /** Drawing style of the Compose banner: 'green' | 'beige' | 'copper' (unknown = green). */
    val style: String? = null,
    /** Icon key for `categoryIconRes`; blank/unknown = the generic glyph. */
    val icon_key: String? = null,
    /** Optional short pill label above the title. */
    val badge: String? = null,
    /** Optional supporting line under the title. */
    val subtitle: String? = null
)