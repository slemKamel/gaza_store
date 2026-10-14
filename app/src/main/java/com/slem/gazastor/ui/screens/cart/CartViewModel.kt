package com.slem.gazastor.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.config.Constants
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.model.CartLine
import com.slem.gazastor.data.repository.AuthRepository
import com.slem.gazastor.data.repository.CartRepository
import com.slem.gazastor.ui.model.ItemUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartLineUi(
    val itemId: String,
    val title: String,
    val imageUrl: String,
    val unitPrice: Double,
    val quantity: Int,
    val lineTotal: Double
)

data class CartUiState(
    val isLoading: Boolean = true,
    val lines: List<CartLineUi> = emptyList(),
    val itemCount: Int = 0,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val isSignedIn: Boolean = false
) {
    val isEmpty: Boolean get() = lines.isEmpty()
}

/**
 * Merges the persisted cart with the auth session: the cart itself is always
 * local (guests included), while [CartUiState.isSignedIn] decides whether
 * checkout is allowed yet.
 */
class CartViewModel(
    private val cartRepository: CartRepository = AppModule.cartRepository,
    authRepository: AuthRepository = AppModule.authRepository
) : ViewModel() {

    val uiState: StateFlow<CartUiState> =
        combine(cartRepository.lines, authRepository.sessionState) { lines, user ->
            val subtotal = lines.sumOf { it.lineTotal }
            val deliveryFee = if (lines.isEmpty()) 0.0 else Constants.DELIVERY_FEE
            CartUiState(
                isLoading = false,
                lines = lines.map(CartLine::toUi),
                itemCount = lines.sumOf { it.quantity },
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                total = subtotal + deliveryFee,
                isSignedIn = user != null
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CartUiState()
        )

    /** Adds [item] to the cart; items without a numeric price are ignored. */
    fun addItem(item: ItemUi, quantity: Int = 1) {
        val unitPrice = item.priceValue ?: return
        viewModelScope.launch {
            cartRepository.add(
                CartLine(
                    itemId = item.id,
                    title = item.title,
                    unitPrice = unitPrice,
                    imageUrl = item.imageUrl,
                    quantity = quantity
                )
            )
        }
    }

    fun increase(itemId: String) {
        val line = uiState.value.lines.firstOrNull { it.itemId == itemId } ?: return
        viewModelScope.launch { cartRepository.setQuantity(itemId, line.quantity + 1) }
    }

    fun decrease(itemId: String) {
        val line = uiState.value.lines.firstOrNull { it.itemId == itemId } ?: return
        viewModelScope.launch { cartRepository.setQuantity(itemId, line.quantity - 1) }
    }

    fun remove(itemId: String) {
        viewModelScope.launch { cartRepository.remove(itemId) }
    }

    fun clear() {
        viewModelScope.launch { cartRepository.clear() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun CartLine.toUi() = CartLineUi(
    itemId = itemId,
    title = title,
    imageUrl = imageUrl,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)
