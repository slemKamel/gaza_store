package com.slem.gazastor.ui.screens.detail

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

data class DetailUiState(
    val isLoading: Boolean = false,
    val item: ItemUi? = null,
    val isError: Boolean = false
)

class DetailViewModel(private val repository: ContentRepository = AppModule.contentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var itemId: String = ""

    fun loadItem(itemId: String) {
        this.itemId = itemId
        viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)
            // Fetch just this row by id instead of downloading the whole catalog.
            val result = repository.getItem(itemId)
            val item = result.getOrNull()?.toUi()
            _uiState.value = DetailUiState(
                isLoading = false,
                item = item,
                isError = result.isFailure
            )
        }
    }

    fun retry() {
        loadItem(itemId)
    }
}
