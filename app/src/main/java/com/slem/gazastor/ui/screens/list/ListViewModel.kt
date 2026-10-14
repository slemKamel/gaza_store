package com.slem.gazastor.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.remote.mapper.toUi
import com.slem.gazastor.data.repository.ContentRepository
import com.slem.gazastor.ui.model.ItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class ListViewModel(private val repository: ContentRepository = AppModule.contentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private var categoryId: String = ""

    fun loadItems(categoryId: String) {
        this.categoryId = categoryId
        viewModelScope.launch {
            _uiState.value = ListUiState(isLoading = true)
            val items = repository.getItemsByCategory(categoryId)
            val name = repository.getCategory(categoryId).getOrNull()?.name.orEmpty()
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
