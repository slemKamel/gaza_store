package com.slem.gazastor.testing

import com.slem.gazastor.data.model.FavoriteItem
import com.slem.gazastor.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [FavoritesRepository] mirroring the add/remove semantics of the real one. */
class FakeFavoritesRepository(initial: List<FavoriteItem> = emptyList()) : FavoritesRepository {

    private val state = MutableStateFlow(initial)

    override val favorites: Flow<List<FavoriteItem>> = state

    /** The current snapshot, for easy assertions. */
    val current: List<FavoriteItem> get() = state.value

    override suspend fun toggle(item: FavoriteItem): Boolean {
        val isFavorite = state.value.any { it.itemId == item.itemId }
        state.value = if (isFavorite) {
            state.value.filterNot { it.itemId == item.itemId }
        } else {
            state.value + item
        }
        return !isFavorite
    }
}
