package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

/** Row of `public.order_items` (line snapshot of a placed order). */
@Serializable
data class OrderItemDto(
    val id: String? = null,
    val order_id: String? = null,
    val item_id: String? = null,
    val title: String? = null,
    val unit_price: Double? = null,
    val quantity: Int? = null,
    val line_total: Double? = null
)
