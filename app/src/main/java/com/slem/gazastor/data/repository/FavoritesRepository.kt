package com.slem.gazastor.data.repository

import com.slem.gazastor.data.model.FavoriteItem
import kotlinx.coroutines.flow.Flow

/**
 * The favorites list, stored locally on the device (like the cart) so guests
 * can save products without an account.
 */
interface FavoritesRepository {

    /** The currently favorited products, in the order they were first saved. */
    val favorites: Flow<List<FavoriteItem>>

    /**
     * Adds [item] if it is not saved yet, removes it otherwise.
     * Returns true when the item is favorited after the call.
     */
    suspend fun toggle(item: FavoriteItem): Boolean
}
