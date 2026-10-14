package com.slem.gazastor.ui.model

data class CategoryUi(
    val id: String,
    val name: String,
    val slug: String,
    val imageUrl: String
)

data class ItemUi(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val price: String,
    /** Raw numeric price, needed for cart totals (null when the item has no price). */
    val priceValue: Double?,
    val imageUrl: String,
    val images: List<String>
)

data class BannerUi(
    val id: String,
    val title: String,
    val imageUrl: String,
    val link: String
)