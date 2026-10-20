package com.slem.gazastor.ui.screens.favorites

import com.slem.gazastor.data.model.FavoriteItem
import com.slem.gazastor.testing.FakeFavoritesRepository
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
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val saved = FavoriteItem(
        itemId = "item-1",
        title = "منتج",
        price = "100 ₪",
        priceValue = 100.0,
        imageUrl = "https://example.com/a.jpg"
    )

    @Test
    fun emptyStore_isEmpty() = runTest {
        val viewModel = FavoritesViewModel(FakeFavoritesRepository())
        val state = observe(viewModel)

        assertTrue(state.isEmpty)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun savedItems_areShownAsGridItems() = runTest {
        val viewModel = FavoritesViewModel(FakeFavoritesRepository(listOf(saved)))
        val state = observe(viewModel)

        assertFalse(state.isEmpty)
        assertEquals(listOf("item-1"), state.items.map { it.id })
        assertEquals("منتج", state.items.first().title)
        assertEquals(100.0, state.items.first().priceValue!!, 0.0)
        assertEquals("https://example.com/a.jpg", state.items.first().imageUrl)
    }

    @Test
    fun toggle_removesASavedItem() = runTest {
        val repository = FakeFavoritesRepository(listOf(saved))
        val viewModel = FavoritesViewModel(repository)
        observe(viewModel)

        viewModel.toggleFavorite(itemUi())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
        assertTrue(repository.current.isEmpty())
    }

    @Test
    fun toggle_addsThenRemoves_anUnsavedItem() = runTest {
        val repository = FakeFavoritesRepository()
        val viewModel = FavoritesViewModel(repository)
        observe(viewModel)

        viewModel.toggleFavorite(itemUi())
        advanceUntilIdle()
        assertEquals(listOf("item-1"), viewModel.uiState.value.items.map { it.id })

        viewModel.toggleFavorite(itemUi())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    /** `stateIn(WhileSubscribed)` needs an active collector before `value` is populated. */
    private fun TestScope.observe(viewModel: FavoritesViewModel): FavoritesUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()
        return viewModel.uiState.value
    }
}

private fun itemUi() = ItemUi(
    id = "item-1",
    categoryId = "cat-1",
    title = "منتج",
    description = "",
    price = "100 ₪",
    priceValue = 100.0,
    imageUrl = "https://example.com/a.jpg",
    images = emptyList()
)
