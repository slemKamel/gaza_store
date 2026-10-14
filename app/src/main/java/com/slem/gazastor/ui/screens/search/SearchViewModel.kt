package com.slem.gazastor.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.remote.mapper.toUi
import com.slem.gazastor.data.repository.ContentRepository
import com.slem.gazastor.ui.model.ItemUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val results: List<ItemUi> = emptyList(),
    val isError: Boolean = false
) {
    val isEmpty: Boolean
        get() = !isLoading && !isError && hasSearched && results.isEmpty()
}

class SearchViewModel(
    private val repository: ContentRepository = AppModule.contentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
        searchJob?.cancel()

        val term = value.trim()
        if (term.isEmpty()) {
            _uiState.value = SearchUiState(query = value)
            return
        }

        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            search(term)
        }
    }

    fun retry() {
        val term = _uiState.value.query.trim()
        if (term.isEmpty()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch { search(term) }
    }

    private suspend fun search(term: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
        val result = repository.searchItems(term)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            hasSearched = true,
            results = result.getOrDefault(emptyList()).map { it.toUi() },
            isError = result.isFailure
        )
    }

    private companion object {
        const val DEBOUNCE_MS = 300L
    }
}
