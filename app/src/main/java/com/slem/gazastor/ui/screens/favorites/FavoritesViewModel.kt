package com.slem.gazastor.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.remote.mapper.toFavorite
import com.slem.gazastor.data.remote.mapper.toUi
import com.slem.gazastor.data.repository.FavoritesRepository
import com.slem.gazastor.ui.model.ItemUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val items: List<ItemUi> = emptyList()
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository = AppModule.favoritesRepository
) : ViewModel() {

    /** The saved products, rebuilt as grid items from the local snapshots. */
    val uiState: StateFlow<FavoritesUiState> = favoritesRepository.favorites
        .map { favorites -> FavoritesUiState(isLoading = false, items = favorites.map { it.toUi() }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState())

    /** Removes the product from favorites (the heart on a saved card). */
    fun toggleFavorite(item: ItemUi) {
        viewModelScope.launch { favoritesRepository.toggle(item.toFavorite()) }
    }
}
