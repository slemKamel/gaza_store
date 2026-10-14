package com.slem.gazastor.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.model.OrderDto
import com.slem.gazastor.data.repository.AuthRepository
import com.slem.gazastor.data.repository.OrderRepository
import com.slem.gazastor.ui.model.formatOrderDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderLineUi(
    val title: String,
    val quantity: Int,
    val lineTotal: Double
)

data class OrderUi(
    val id: String,
    val shortId: String,
    /** Backend status key, mapped to Arabic in the UI. */
    val status: String,
    val total: Double,
    val date: String,
    val lines: List<OrderLineUi>
)

data class OrdersUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isSignedIn: Boolean = false,
    val orders: List<OrderUi> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && !isError && orders.isEmpty()
}

/**
 * Loads the signed-in customer's order history. Guests get an empty list and a
 * sign-in prompt; RLS enforces the same rule on the server.
 */
class OrdersViewModel(
    private val orderRepository: OrderRepository = AppModule.orderRepository,
    authRepository: AuthRepository = AppModule.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionState.collect { user ->
                _uiState.update { it.copy(isSignedIn = user != null) }
                if (user != null) {
                    load()
                } else {
                    _uiState.value = OrdersUiState(isSignedIn = false)
                }
            }
        }
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }
            orderRepository.getOrders().fold(
                onSuccess = { orders ->
                    _uiState.update {
                        it.copy(isLoading = false, isError = false, orders = orders.map(OrderDto::toUi))
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, isError = true) }
                }
            )
        }
    }
}

private fun OrderDto.toUi(): OrderUi {
    val id = id.orEmpty()
    return OrderUi(
        id = id,
        shortId = id.takeLast(ID_SUFFIX_LENGTH).uppercase(),
        status = status.orEmpty(),
        total = total ?: 0.0,
        date = formatOrderDate(created_at),
        lines = order_items.orEmpty().map { item ->
            OrderLineUi(
                title = item.title.orEmpty(),
                quantity = item.quantity ?: 0,
                lineTotal = item.line_total ?: 0.0
            )
        }
    )
}

private const val ID_SUFFIX_LENGTH = 8
