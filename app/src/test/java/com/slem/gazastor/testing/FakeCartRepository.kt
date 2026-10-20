package com.slem.gazastor.testing

import com.slem.gazastor.data.model.CartLine
import com.slem.gazastor.data.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [CartRepository] mirroring the merge/remove semantics of the real one. */
class FakeCartRepository(initial: List<CartLine> = emptyList()) : CartRepository {

    private val state = MutableStateFlow(initial)

    override val lines: Flow<List<CartLine>> = state

    override suspend fun add(line: CartLine) {
        val current = state.value
        val index = current.indexOfFirst { it.itemId == line.itemId }
        state.value = if (index == -1) {
            current + line
        } else {
            current.toMutableList().also {
                it[index] = it[index].copy(quantity = it[index].quantity + line.quantity)
            }
        }
    }

    override suspend fun setQuantity(itemId: String, quantity: Int) {
        if (quantity <= 0) {
            remove(itemId)
            return
        }
        state.value = state.value.map {
            if (it.itemId == itemId) it.copy(quantity = quantity) else it
        }
    }

    override suspend fun increment(itemId: String, delta: Int) {
        state.value = state.value.mapNotNull { line ->
            when {
                line.itemId != itemId -> line
                line.quantity + delta <= 0 -> null
                else -> line.copy(quantity = line.quantity + delta)
            }
        }
    }

    override suspend fun remove(itemId: String) {
        state.value = state.value.filterNot { it.itemId == itemId }
    }

    override suspend fun clear() {
        state.value = emptyList()
    }
}
