package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

/**
 * Row of `public.orders`. `order_items` is embedded by PostgREST when the query
 * selects the relation, and stays null when it is not requested.
 */
@Serializable
data class OrderDto(
    val id: String? = null,
    val user_id: String? = null,
    val status: String? = null,
    val payment_method: String? = null,
    val payment_status: String? = null,
    val subtotal: Double? = null,
    val delivery_fee: Double? = null,
    val total: Double? = null,
    val customer_name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val created_at: String? = null,
    val order_items: List<OrderItemDto>? = null
)
