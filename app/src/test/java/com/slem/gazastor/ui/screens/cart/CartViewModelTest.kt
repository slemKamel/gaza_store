package com.slem.gazastor.ui.screens.cart

import com.slem.gazastor.core.config.Constants
import com.slem.gazastor.data.model.AuthUser
import com.slem.gazastor.data.model.CartLine
import com.slem.gazastor.testing.FakeAuthRepository
import com.slem.gazastor.testing.FakeCartRepository
import com.slem.gazastor.testing.MainDispatcherRule
import com.slem.gazastor.ui.model.ItemUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val line = CartLine(
        itemId = "item-1",
        title = "منتج",
        unitPrice = 100.0,
        imageUrl = "",
        quantity = 2
    )

    @Test
    fun emptyCart_hasNoTotals() = runTest {
        val viewModel = CartViewModel(FakeCartRepository(), FakeAuthRepository())
        val state = observe(viewModel)

        assertTrue(state.isEmpty)
        assertEquals(0.0, state.subtotal, 0.0)
        assertEquals(0.0, state.deliveryFee, 0.0)
        assertEquals(0.0, state.total, 0.0)
    }

    @Test
    fun totals_includeFlatDeliveryFee() = runTest {
        val viewModel = CartViewModel(FakeCartRepository(listOf(line)), FakeAuthRepository())
        val state = observe(viewModel)

        assertEquals(200.0, state.subtotal, 0.0)
        assertEquals(Constants.DELIVERY_FEE, state.deliveryFee, 0.0)
        assertEquals(200.0 + Constants.DELIVERY_FEE, state.total, 0.0)
        assertEquals(2, state.itemCount)
        assertFalse(state.isEmpty)
    }

    @Test
    fun signedIn_reflectsSession() = runTest {
        val guest = CartViewModel(FakeCartRepository(), FakeAuthRepository())
        assertFalse(observe(guest).isSignedIn)

        val signedIn = CartViewModel(
            FakeCartRepository(),
            FakeAuthRepository(AuthUser(id = "user-1", email = "a@b.com"))
        )
        assertTrue(observe(signedIn).isSignedIn)
    }

    @Test
    fun addItem_mergesQuantityForTheSameItem() = runTest {
        val viewModel = CartViewModel(FakeCartRepository(), FakeAuthRepository())
        observe(viewModel)

        viewModel.addItem(itemUi(), quantity = 2)
        advanceUntilIdle()
        viewModel.addItem(itemUi(), quantity = 1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.lines.size)
        assertEquals(3, viewModel.uiState.value.lines.first().quantity)
    }

    @Test
    fun addItem_withoutNumericPrice_isIgnored() = runTest {
        val viewModel = CartViewModel(FakeCartRepository(), FakeAuthRepository())
        observe(viewModel)

        viewModel.addItem(itemUi(price = null))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun decrease_removesTheLineWhenItReachesZero() = runTest {
        val viewModel = CartViewModel(FakeCartRepository(listOf(line)), FakeAuthRepository())
        observe(viewModel)

        viewModel.decrease("item-1")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.lines.first().quantity)

        viewModel.decrease("item-1")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun clear_emptiesTheCart() = runTest {
        val viewModel = CartViewModel(FakeCartRepository(listOf(line)), FakeAuthRepository())
        observe(viewModel)

        viewModel.clear()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    /** `stateIn(WhileSubscribed)` needs an active collector before `value` is populated. */
    private fun TestScope.observe(viewModel: CartViewModel): CartUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        return viewModel.uiState.value
    }
}

private fun itemUi(price: Double? = 100.0) = ItemUi(
    id = "item-1",
    categoryId = "cat-1",
    title = "منتج",
    description = "",
    price = price?.toString().orEmpty(),
    priceValue = price,
    imageUrl = "",
    images = emptyList()
)
