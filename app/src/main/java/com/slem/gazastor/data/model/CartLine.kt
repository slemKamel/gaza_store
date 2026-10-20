package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

/**
 * One cart line, persisted locally so guests keep their cart across launches.
 * Title, price and image are snapshotted the same way `order_items` snapshots
 * them, so a catalog change never rewrites an in-progress cart.
 */
@Serializable
data class CartLine(
    val itemId: String,
    val title: String,
    val unitPrice: Double,
    val imageUrl: String,
    val quantity: Int,
    /**
     * `categories.icon_key` snapshot, so the cart row keeps its image fallback
     * without a catalog round-trip. Added later than the first release, so the
     * default keeps carts stored by older builds decodable.
     */
    val iconKey: String = ""
) {
    val lineTotal: Double get() = unitPrice * quantity
}
