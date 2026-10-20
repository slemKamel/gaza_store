package com.slem.gazastor.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.remote.mapper.toFavorite
import com.slem.gazastor.data.remote.mapper.toUi
import com.slem.gazastor.data.repository.ContentRepository
import com.slem.gazastor.data.repository.FavoritesRepository
import com.slem.gazastor.ui.model.BannerUi
import com.slem.gazastor.ui.model.CategoryUi
import com.slem.gazastor.ui.model.ItemUi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    /** True while a pull-to-refresh is in-flight; content may still be visible. */
    val isRefreshing: Boolean = false,
    val categories: List<CategoryUi> = emptyList(),
    val banners: List<BannerUi> = emptyList(),
    val recentItems: List<ItemUi> = emptyList(),
    val isError: Boolean = false
) {
    val hasContent: Boolean
        get() = categories.isNotEmpty() || banners.isNotEmpty() || recentItems.isNotEmpty()

    val isEmpty: Boolean
        get() = !isLoading && !isRefreshing && !isError && !hasContent
}

class HomeViewModel(
    private val repository: ContentRepository = AppModule.contentRepository,
    private val favoritesRepository: FavoritesRepository = AppModule.favoritesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Ids of saved products, kept in sync with the shared favorites store. */
    val favoriteIds: StateFlow<Set<String>> = favoritesRepository.favorites
        .map { favorites -> favorites.map { it.itemId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavorite(item: ItemUi) {
        viewModelScope.launch { favoritesRepository.toggle(item.toFavorite()) }
    }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val current = _uiState.value
            val isFirstLoad = !current.hasContent && !current.isError

            if (isFirstLoad) {
                _uiState.value = current.copy(isLoading = true, isRefreshing = false, isError = false)
            } else {
                _uiState.value = current.copy(isLoading = false, isRefreshing = true, isError = false)
            }

            // Fire all three queries at once
            val categoriesDeferred = async { repository.getCategories() }
            val bannersDeferred = async { repository.getBanners() }
            val itemsDeferred = async { repository.getItems() }
            val categories = categoriesDeferred.await()
            val banners = bannersDeferred.await()
            val items = itemsDeferred.await()
            _uiState.value = HomeUiState(
                isLoading = false,
                isRefreshing = false,
                categories = categories.getOrDefault(emptyList()).map { it.toUi() },
                banners = banners.getOrDefault(emptyList()).map { it.toUi() },
                recentItems = items.getOrDefault(emptyList()).map { it.toUi() }.take(10),
                isError = categories.isFailure || banners.isFailure || items.isFailure
            )
        }
    }
}
