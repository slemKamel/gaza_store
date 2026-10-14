package com.slem.gazastor.ui.screens.orders

import com.slem.gazastor.data.model.AuthUser
import com.slem.gazastor.data.model.OrderDto
import com.slem.gazastor.data.model.OrderItemDto
import com.slem.gazastor.testing.FakeAuthRepository
import com.slem.gazastor.testing.FakeOrderRepository
import com.slem.gazastor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun signedInUser() = FakeAuthRepository(AuthUser(id = "user-1", email = "a@b.com"))

    @Test
    fun guest_isNotSignedInAndHasNoOrders() = runTest {
        val viewModel = OrdersViewModel(FakeOrderRepository(), FakeAuthRepository())
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSignedIn)
        assertTrue(viewModel.uiState.value.orders.isEmpty())
    }

    @Test
    fun signedIn_mapsOrdersWithLinesAndDate() = runTest {
        val repository = FakeOrderRepository().apply {
            ordersResult = Result.success(
                listOf(
                    OrderDto(
                        id = "11111111-2222-3333-4444-555555555555",
                        status = "shipped",
                        total = 150.0,
                        created_at = "2026-10-14T09:30:00+00:00",
                        order_items = listOf(
                            OrderItemDto(title = "منتج", quantity = 2, line_total = 150.0)
                        )
                    )
                )
            )
        }
        val viewModel = OrdersViewModel(repository, signedInUser())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSignedIn)
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals(1, state.orders.size)

        val order = state.orders.first()
        assertEquals("shipped", order.status)
        assertEquals("55555555", order.shortId)
        assertEquals("2026/10/14", order.date)
        assertEquals(150.0, order.total, 0.0)
        assertEquals(1, order.lines.size)
        assertEquals("منتج", order.lines.first().title)
        assertEquals(2, order.lines.first().quantity)
    }

    @Test
    fun signedIn_whenLoadingFails_setsErrorState() = runTest {
        val repository = FakeOrderRepository().apply {
            ordersResult = Result.failure(IllegalStateException("boom"))
        }
        val viewModel = OrdersViewModel(repository, signedInUser())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isError)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
