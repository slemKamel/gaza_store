package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

/**
 * Insert payload for `public.orders`. Only writable columns are sent: `id`,
 * `status`, `payment_status` and timestamps keep their database defaults, and
 * `user_id` is required by the `orders_insert_own` RLS policy.
 */
@Serializable
data class OrderInsertDto(
    val user_id: String,
    val customer_name: String,
    val phone: String?,
    val address: String,
    val notes: String?,
    val payment_method: String = "cod",
    val subtotal: Double,
    val delivery_fee: Double,
    val total: Double
)

/**
 * Insert payload for `public.order_items`. `line_total` is a generated column
 * and must not be sent.
 */
@Serializable
data class OrderItemInsertDto(
    val order_id: String,
    val item_id: String?,
    val title: String,
    val unit_price: Double,
    val quantity: Int
)
