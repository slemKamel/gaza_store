package com.slem.gazastor.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.remote.mapper.toFavorite
import com.slem.gazastor.data.remote.mapper.toUi
import com.slem.gazastor.data.repository.ContentRepository
import com.slem.gazastor.data.repository.FavoritesRepository
import com.slem.gazastor.ui.model.ItemUi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ListUiState(
    val isLoading: Boolean = false,
    val categoryName: String = "",
    val items: List<ItemUi> = emptyList(),
    val isError: Boolean = false
) {
    val isEmpty: Boolean
        get() = !isLoading && !isError && items.isEmpty()
}

class ListViewModel(
    private val repository: ContentRepository = AppModule.contentRepository,
    private val favoritesRepository: FavoritesRepository = AppModule.favoritesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    /** Ids of saved products, kept in sync with the shared favorites store. */
    val favoriteIds: StateFlow<Set<String>> = favoritesRepository.favorites
        .map { favorites -> favorites.map { it.itemId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavorite(item: ItemUi) {
        viewModelScope.launch { favoritesRepository.toggle(item.toFavorite()) }
    }

    private var categoryId: String = ""

    fun loadItems(categoryId: String) {
        this.categoryId = categoryId
        viewModelScope.launch {
            _uiState.value = ListUiState(isLoading = true)
            // Items and title load together instead of waiting in a queue.
            val itemsDeferred = async { repository.getItemsByCategory(categoryId) }
            val nameDeferred = async { repository.getCategory(categoryId) }
            val items = itemsDeferred.await()
            val name = nameDeferred.await().getOrNull()?.name.orEmpty()
            _uiState.value = ListUiState(
                isLoading = false,
                categoryName = name,
                items = items.getOrDefault(emptyList()).map { it.toUi() },
                isError = items.isFailure
            )
        }
    }

    fun retry() {
        loadItems(categoryId)
    }
}
