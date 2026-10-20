package com.slem.gazastor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.slem.gazastor.data.model.FavoriteItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Own DataStore file: two `preferencesDataStore` delegates must never share a
 * file name, so the favorites intentionally live next to (and separate from)
 * the cart ([DataStoreCartRepository]) and
 * [com.slem.gazastor.data.preferences.UserPreferences].
 */
private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gaza_stor_favorites"
)

private val KEY_FAVORITES = stringPreferencesKey("favorite_items")

/** Persists favorites as a JSON array so they survive process death. */
class DataStoreFavoritesRepository(private val context: Context) : FavoritesRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override val favorites: Flow<List<FavoriteItem>> =
        context.favoritesDataStore.data
            .map { prefs -> decode(prefs[KEY_FAVORITES]) }

    override suspend fun toggle(item: FavoriteItem): Boolean {
        val current = decode(context.favoritesDataStore.data.first()[KEY_FAVORITES])
        val isFavorite = current.any { it.itemId == item.itemId }
        val next = if (isFavorite) {
            current.filterNot { it.itemId == item.itemId }
        } else {
            current + item
        }
        context.favoritesDataStore.edit { prefs -> prefs[KEY_FAVORITES] = json.encodeToString(next) }
        return !isFavorite
    }

    private fun decode(raw: String?): List<FavoriteItem> =
        if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            runCatching { json.decodeFromString<List<FavoriteItem>>(raw) }.getOrDefault(emptyList())
        }
}
