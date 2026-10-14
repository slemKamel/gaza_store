package com.slem.gazastor.data.repository

import com.slem.gazastor.data.model.OrderDto
import com.slem.gazastor.data.model.OrderInsertDto
import com.slem.gazastor.data.model.OrderItemInsertDto
import com.slem.gazastor.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException

class OrderRepositoryImpl(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : OrderRepository {

    override suspend fun placeOrder(order: NewOrder): Result<OrderDto> = safeOrderCall {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("Cannot place an order while signed out")

        val header = OrderInsertDto(
            user_id = userId,
            customer_name = order.customerName,
            phone = order.phone?.takeIf { it.isNotBlank() },
            address = order.address,
            notes = order.notes?.takeIf { it.isNotBlank() },
            subtotal = order.subtotal,
            delivery_fee = order.deliveryFee,
            total = order.total
        )

        // `select()` asks PostgREST to return the inserted row so we get its id.
        val created = supabase.from("orders")
            .insert(header) { select() }
            .decodeSingle<OrderDto>()

        val orderId = created.id
            ?: error("Order insert did not return an id")

        supabase.from("order_items").insert(
            order.lines.map { line ->
                OrderItemInsertDto(
                    order_id = orderId,
                    item_id = line.itemId,
                    title = line.title,
                    unit_price = line.unitPrice,
                    quantity = line.quantity
                )
            }
        )

        created
    }

    override suspend fun getOrders(): Result<List<OrderDto>> = safeOrderCall {
        supabase.from("orders").select(
            columns = Columns.raw("*, order_items(*)")
        ) {
            order("created_at", Order.DESCENDING)
        }.decodeList<OrderDto>()
    }
}

/** Wraps a suspending order call in [Result] without swallowing cancellation. */
private suspend fun <T> safeOrderCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
