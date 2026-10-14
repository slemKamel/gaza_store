package com.slem.gazastor.data.repository

import com.slem.gazastor.data.model.CartLine
import com.slem.gazastor.data.model.OrderDto

/** Everything needed to place one cash-on-delivery order. */
data class NewOrder(
    val customerName: String,
    val phone: String?,
    val address: String,
    val notes: String?,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val lines: List<CartLine>
)

/**
 * Order history and checkout. Returns [Result] like the other repositories, and
 * relies on RLS so a customer can only read/insert their own orders.
 */
interface OrderRepository {

    /** Inserts the order header plus its lines and returns the created order. */
    suspend fun placeOrder(order: NewOrder): Result<OrderDto>

    /** The signed-in customer's orders, newest first, with their lines embedded. */
    suspend fun getOrders(): Result<List<OrderDto>>
}
