package com.slem.gazastor.data.model

import kotlinx.serialization.Serializable

/**
 * One favorited product, persisted locally so favorites survive relaunches and
 * work for guests too. Title, price and image are snapshotted the same way
 * `order_items` and [CartLine] snapshot them, so a catalog change never breaks
 * a saved favorite. Only the fields the grid card needs are kept; tapping a
 * favorite loads the fresh full item from the catalog by id.
 */
@Serializable
data class FavoriteItem(
    val itemId: String,
    val title: String,
    val price: String,
    val priceValue: Double?,
    val imageUrl: String,
    /**
     * `categories.icon_key` snapshot, so a saved favorite keeps its image
     * fallback without a catalog round-trip. Added later than the first release,
     * so the default keeps older stored lists decodable.
     */
    val iconKey: String = ""
)
