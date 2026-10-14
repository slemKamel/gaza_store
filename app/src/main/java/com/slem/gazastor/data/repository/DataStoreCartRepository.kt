package com.slem.gazastor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.slem.gazastor.data.model.CartLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Own DataStore file: two `preferencesDataStore` delegates must never share a
 * file name, so the cart intentionally lives next to (and separate from)
 * [com.slem.gazastor.data.preferences.UserPreferences].
 */
private val Context.cartDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "gaza_stor_cart"
)

private val KEY_CART_LINES = stringPreferencesKey("cart_lines")

/** Persists the cart as a JSON array so it survives process death. */
class DataStoreCartRepository(private val context: Context) : CartRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override val lines: Flow<List<CartLine>> = context.cartDataStore.data.map { preferences ->
        preferences.decodeLines()
    }

    override suspend fun add(line: CartLine) {
        update { current ->
            val index = current.indexOfFirst { it.itemId == line.itemId }
            if (index == -1) {
                current + line
            } else {
                current.toMutableList().also {
                    val existing = it[index]
                    it[index] = existing.copy(quantity = existing.quantity + line.quantity)
                }
            }
        }
    }

    override suspend fun setQuantity(itemId: String, quantity: Int) {
        if (quantity <= 0) {
            remove(itemId)
            return
        }
        update { current ->
            current.map { if (it.itemId == itemId) it.copy(quantity = quantity) else it }
        }
    }

    override suspend fun remove(itemId: String) {
        update { current -> current.filterNot { it.itemId == itemId } }
    }

    override suspend fun clear() {
        update { emptyList() }
    }

    private suspend fun update(transform: (List<CartLine>) -> List<CartLine>) {
        context.cartDataStore.edit { preferences ->
            val updated = transform(preferences.decodeLines())
            preferences[KEY_CART_LINES] = json.encodeToString(updated)
        }
    }

    private fun Preferences.decodeLines(): List<CartLine> {
        val raw = this[KEY_CART_LINES] ?: return emptyList()
        // A corrupt/older payload must not break the cart screen.
        return runCatching { json.decodeFromString<List<CartLine>>(raw) }.getOrDefault(emptyList())
    }
}
