package com.slem.gazastor.testing

import com.slem.gazastor.data.model.OrderDto
import com.slem.gazastor.data.repository.NewOrder
import com.slem.gazastor.data.repository.OrderRepository

/** Hand-written [OrderRepository] fake recording what was submitted. */
class FakeOrderRepository : OrderRepository {

    var placeOrderResult: Result<OrderDto> = Result.success(OrderDto(id = "order-1"))
    var ordersResult: Result<List<OrderDto>> = Result.success(emptyList())

    val placedOrders = mutableListOf<NewOrder>()

    var getOrdersCalls = 0
        private set

    override suspend fun placeOrder(order: NewOrder): Result<OrderDto> {
        placedOrders += order
        return placeOrderResult
    }

    override suspend fun getOrders(): Result<List<OrderDto>> {
        getOrdersCalls++
        return ordersResult
    }
}
