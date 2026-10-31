package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

/** Row of `public.profiles` (1:1 with an `auth.users` row). */
@Serializable
data class ProfileDto(
    val id: String? = null,
    val full_name: String? = null,
    val phone: String? = null,
    val role: String? = null,
    /** Row creation timestamp, shown as "عضو منذ ..." on the account screen. */
    val created_at: String? = null
)
