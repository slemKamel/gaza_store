package com.slem.gazastor.data.repository

import com.slem.gazastor.data.model.CartLine
import kotlinx.coroutines.flow.Flow

/**
 * The shopping cart, stored locally so guests can fill it without an account.
 * Quantities are merged per item, and a line is removed when its quantity
 * reaches zero.
 */
interface CartRepository {

    /** The current lines, ordered as they were first added. */
    val lines: Flow<List<CartLine>>

    /** Adds [line] (or increases the quantity of an existing line). */
    suspend fun add(line: CartLine)

    /** Sets the absolute quantity of a line; removes it when [quantity] <= 0. */
    suspend fun setQuantity(itemId: String, quantity: Int)

    /**
     * Atomically adds [delta] to an existing line and removes it when the result
     * is <= 0. Unknown ids are ignored. Atomic so rapid taps from the compact
     * stepper cannot lose an increment.
     */
    suspend fun increment(itemId: String, delta: Int)

    suspend fun remove(itemId: String)

    suspend fun clear()
}
