package com.slem.gazastor.data.remote.mapper

import com.slem.gazastor.data.model.BannerDto
import com.slem.gazastor.data.model.CategoryDto
import com.slem.gazastor.data.model.ItemDto
import com.slem.gazastor.ui.model.BannerUi
import com.slem.gazastor.ui.model.CategoryUi
import com.slem.gazastor.ui.model.ItemUi
import com.slem.gazastor.ui.model.formatPriceValue

fun CategoryDto.toUi(): CategoryUi = CategoryUi(
    id = id.orEmpty(),
    name = name.orEmpty(),
    slug = slug.orEmpty(),
    imageUrl = image_url.orEmpty()
)

fun ItemDto.toUi(): ItemUi = ItemUi(
    id = id.orEmpty(),
    categoryId = category_id.orEmpty(),
    title = title.orEmpty(),
    description = description.orEmpty(),
    price = price?.let(::formatPriceValue).orEmpty(),
    priceValue = price,
    imageUrl = image_url.orEmpty(),
    images = images ?: emptyList()
)

fun BannerDto.toUi(): BannerUi = BannerUi(
    id = id.orEmpty(),
    title = title.orEmpty(),
    imageUrl = image_url.orEmpty(),
    link = link.orEmpty()
)
