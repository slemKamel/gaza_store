package com.slem.gazastor.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.remote.mapper.toUi
import com.slem.gazastor.data.repository.ContentRepository
import com.slem.gazastor.ui.model.BannerUi
import com.slem.gazastor.ui.model.CategoryUi
import com.slem.gazastor.ui.model.ItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryUi> = emptyList(),
    val banners: List<BannerUi> = emptyList(),
    val recentItems: List<ItemUi> = emptyList(),
    val isError: Boolean = false
) {
    val isEmpty: Boolean
        get() = !isLoading && !isError && categories.isEmpty() && banners.isEmpty() && recentItems.isEmpty()
}

class HomeViewModel(private val repository: ContentRepository = AppModule.contentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            val categories = repository.getCategories()
            val banners = repository.getBanners()
            val items = repository.getItems()
            _uiState.value = HomeUiState(
                isLoading = false,
                categories = categories.getOrDefault(emptyList()).map { it.toUi() },
                banners = banners.getOrDefault(emptyList()).map { it.toUi() },
                recentItems = items.getOrDefault(emptyList()).map { it.toUi() }.take(10),
                isError = categories.isFailure || banners.isFailure || items.isFailure
            )
        }
    }
}
