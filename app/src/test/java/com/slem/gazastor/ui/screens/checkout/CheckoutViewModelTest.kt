package com.slem.gazastor.ui.screens.checkout

import com.slem.gazastor.core.config.Constants
import com.slem.gazastor.data.model.AuthUser
import com.slem.gazastor.data.model.CartLine
import com.slem.gazastor.data.model.ProfileDto
import com.slem.gazastor.testing.FakeAuthRepository
import com.slem.gazastor.testing.FakeCartRepository
import com.slem.gazastor.testing.FakeOrderRepository
import com.slem.gazastor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val line = CartLine(
        itemId = "item-1",
        title = "منتج",
        unitPrice = 100.0,
        imageUrl = "",
        quantity = 2
    )

    private fun signedInUser() = FakeAuthRepository(AuthUser(id = "user-1", email = "a@b.com"))

    @Test
    fun submit_withoutName_showsNameErrorAndDoesNotOrder() = runTest {
        val orders = FakeOrderRepository()
        val viewModel = CheckoutViewModel(orders, FakeCartRepository(listOf(line)), signedInUser())
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(CheckoutError.NameRequired, viewModel.uiState.value.error)
        assertTrue(orders.placedOrders.isEmpty())
    }

    @Test
    fun submit_whenGuest_asksForSignIn() = runTest {
        val orders = FakeOrderRepository()
        val viewModel = CheckoutViewModel(orders, FakeCartRepository(listOf(line)), FakeAuthRepository())
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(CheckoutError.NotSignedIn, viewModel.uiState.value.error)
        assertTrue(orders.placedOrders.isEmpty())
    }

    @Test
    fun submit_withCompleteDetails_placesOrderClearsCartAndEmitsEvent() = runTest {
        val cart = FakeCartRepository(listOf(line))
        val orders = FakeOrderRepository()
        val viewModel = CheckoutViewModel(orders, cart, signedInUser())
        val events = mutableListOf<CheckoutEvent>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }
        advanceUntilIdle()

        viewModel.onCustomerNameChange("أحمد")
        viewModel.onPhoneChange("0599123456")
        viewModel.onAddressChange("غزة - الرمال")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, orders.placedOrders.size)
        val placed = orders.placedOrders.first()
        assertEquals("أحمد", placed.customerName)
        assertEquals(200.0, placed.subtotal, 0.0)
        assertEquals(Constants.DELIVERY_FEE, placed.deliveryFee, 0.0)
        assertEquals(200.0 + Constants.DELIVERY_FEE, placed.total, 0.0)
        assertEquals(1, placed.lines.size)
        assertEquals(listOf(CheckoutEvent.OrderPlaced), events)
        assertTrue(cart.lines.first().isEmpty())
        collector.cancel()
    }

    @Test
    fun submit_whenRepositoryFails_keepsCartAndShowsError() = runTest {
        val cart = FakeCartRepository(listOf(line))
        val orders = FakeOrderRepository().apply {
            placeOrderResult = Result.failure(IllegalStateException("boom"))
        }
        val viewModel = CheckoutViewModel(orders, cart, signedInUser())
        advanceUntilIdle()

        viewModel.onCustomerNameChange("أحمد")
        viewModel.onPhoneChange("0599123456")
        viewModel.onAddressChange("غزة")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(CheckoutError.SubmitFailed, viewModel.uiState.value.error)
        assertEquals(1, cart.lines.first().size)
    }

    @Test
    fun prefill_usesTheSignedInProfile() = runTest {
        val auth = signedInUser().apply {
            profileResult = Result.success(ProfileDto(id = "user-1", full_name = "سارة", phone = "0599000000"))
        }
        val viewModel = CheckoutViewModel(FakeOrderRepository(), FakeCartRepository(listOf(line)), auth)
        advanceUntilIdle()

        assertEquals("سارة", viewModel.uiState.value.customerName)
        assertEquals("0599000000", viewModel.uiState.value.phone)
    }
}
