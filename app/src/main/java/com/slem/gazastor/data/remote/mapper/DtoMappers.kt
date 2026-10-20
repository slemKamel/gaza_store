package com.slem.gazastor.data.remote.mapper

import com.slem.gazastor.data.model.BannerDto
import com.slem.gazastor.data.model.CategoryDto
import com.slem.gazastor.data.model.FavoriteItem
import com.slem.gazastor.data.model.ItemDto
import com.slem.gazastor.ui.model.BannerUi
import com.slem.gazastor.ui.model.CategoryUi
import com.slem.gazastor.ui.model.ItemUi
import com.slem.gazastor.ui.model.formatPriceValue

fun CategoryDto.toUi(): CategoryUi = CategoryUi(
    id = id.orEmpty(),
    name = name.orEmpty(),
    slug = slug.orEmpty(),
    imageUrl = image_url.orEmpty(),
    iconKey = icon_key.orEmpty()
)

fun ItemDto.toUi(): ItemUi = ItemUi(
    id = id.orEmpty(),
    categoryId = category_id.orEmpty(),
    title = title.orEmpty(),
    description = description.orEmpty(),
    price = price?.let(::formatPriceValue).orEmpty(),
    priceValue = price,
    imageUrl = image_url.orEmpty(),
    images = images ?: emptyList(),
    iconKey = categories?.icon_key.orEmpty()
)

fun BannerDto.toUi(): BannerUi = BannerUi(
    id = id.orEmpty(),
    title = title.orEmpty(),
    imageUrl = image_url.orEmpty(),
    link = link.orEmpty(),
    style = style.orEmpty(),
    subtitle = subtitle.orEmpty(),
    badge = badge.orEmpty(),
    iconKey = icon_key.orEmpty()
)

/**
 * A saved favorite only snapshots what the grid card shows, so the rebuild
 * leaves the detail-only fields (description, gallery) empty; tapping the card
 * loads the full fresh item from the catalog by id anyway.
 */
fun FavoriteItem.toUi(): ItemUi = ItemUi(
    id = itemId,
    categoryId = "",
    title = title,
    description = "",
    price = price,
    priceValue = priceValue,
    imageUrl = imageUrl,
    images = listOfNotNull(imageUrl),
    iconKey = iconKey
)

/** Snapshots the fields a favorite needs to render offline. */
fun ItemUi.toFavorite(): FavoriteItem = FavoriteItem(
    itemId = id,
    title = title,
    price = price,
    priceValue = priceValue,
    imageUrl = imageUrl,
    iconKey = iconKey
)
