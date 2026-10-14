package com.slem.gazastor.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.config.Constants
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.repository.AuthRepository
import com.slem.gazastor.data.repository.CartRepository
import com.slem.gazastor.data.repository.NewOrder
import com.slem.gazastor.data.repository.OrderRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Validation/intent failures the checkout form can show. */
enum class CheckoutError {
    NameRequired,
    PhoneRequired,
    AddressRequired,
    NotSignedIn,
    CartEmpty,
    SubmitFailed
}

/** One-shot signal consumed by the screen after a successful order. */
sealed interface CheckoutEvent {
    data object OrderPlaced : CheckoutEvent
}

data class CheckoutUiState(
    val customerName: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val isSubmitting: Boolean = false,
    val isCartEmpty: Boolean = true,
    val isSignedIn: Boolean = false,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val itemCount: Int = 0,
    val error: CheckoutError? = null
)

/**
 * Collects delivery details, prefills them from the signed-in profile, and
 * writes the order through [OrderRepository]. The cart is only cleared once the
 * order header and its lines were accepted by the backend.
 */
class CheckoutViewModel(
    private val orderRepository: OrderRepository = AppModule.orderRepository,
    private val cartRepository: CartRepository = AppModule.cartRepository,
    private val authRepository: AuthRepository = AppModule.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _events = Channel<CheckoutEvent>(Channel.BUFFERED)
    val events: Flow<CheckoutEvent> = _events.receiveAsFlow()

    init {
        observeSession()
        observeCart()
        prefillFromProfile()
    }

    fun onCustomerNameChange(value: String) = updateField { it.copy(customerName = value) }

    fun onPhoneChange(value: String) = updateField { it.copy(phone = value) }

    fun onAddressChange(value: String) = updateField { it.copy(address = value) }

    fun onNotesChange(value: String) = updateField { it.copy(notes = value) }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val lines = cartRepository.lines.first()
            val subtotal = lines.sumOf { it.lineTotal }
            val deliveryFee = Constants.DELIVERY_FEE
            val result = orderRepository.placeOrder(
                NewOrder(
                    customerName = state.customerName.trim(),
                    phone = state.phone.trim(),
                    address = state.address.trim(),
                    notes = state.notes.trim(),
                    subtotal = subtotal,
                    deliveryFee = deliveryFee,
                    total = subtotal + deliveryFee,
                    lines = lines
                )
            )
            result.fold(
                onSuccess = {
                    cartRepository.clear()
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(CheckoutEvent.OrderPlaced)
                },
                onFailure = {
                    _uiState.update { it.copy(isSubmitting = false, error = CheckoutError.SubmitFailed) }
                }
            )
        }
    }

    private fun validate(state: CheckoutUiState): CheckoutError? = when {
        !state.isSignedIn -> CheckoutError.NotSignedIn
        state.isCartEmpty -> CheckoutError.CartEmpty
        state.customerName.isBlank() -> CheckoutError.NameRequired
        state.phone.isBlank() -> CheckoutError.PhoneRequired
        state.address.isBlank() -> CheckoutError.AddressRequired
        else -> null
    }

    private fun updateField(transform: (CheckoutUiState) -> CheckoutUiState) {
        _uiState.update { transform(it).copy(error = null) }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionState.collect { user ->
                _uiState.update { it.copy(isSignedIn = user != null) }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.lines.collect { lines ->
                val subtotal = lines.sumOf { it.lineTotal }
                val deliveryFee = if (lines.isEmpty()) 0.0 else Constants.DELIVERY_FEE
                _uiState.update {
                    it.copy(
                        isCartEmpty = lines.isEmpty(),
                        itemCount = lines.sumOf { line -> line.quantity },
                        subtotal = subtotal,
                        deliveryFee = deliveryFee,
                        total = subtotal + deliveryFee
                    )
                }
            }
        }
    }

    private fun prefillFromProfile() {
        viewModelScope.launch {
            authRepository.profile().onSuccess { profile ->
                if (profile == null) return@onSuccess
                _uiState.update {
                    it.copy(
                        customerName = it.customerName.ifBlank { profile.full_name.orEmpty() },
                        phone = it.phone.ifBlank { profile.phone.orEmpty() }
                    )
                }
            }
        }
    }
}
