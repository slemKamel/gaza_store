package com.slem.gazastor.ui.model

data class CategoryUi(
    val id: String,
    val name: String,
    val slug: String,
    val imageUrl: String,
    /**
     * Glyph key from `categories.icon_key`, resolved by `categoryIconRes`.
     * Blank falls back to [imageUrl] (legacy behaviour) and then to the generic
     * category glyph.
     */
    val iconKey: String = ""
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
    val images: List<String>,
    /**
     * `categories.icon_key` of this item's category, resolved by
     * `categoryIconRes` into the glyph the image fallback draws when the product
     * has no photo. Blank when the query did not embed the category.
     */
    val iconKey: String = ""
)

data class BannerUi(
    val id: String,
    val title: String,
    val imageUrl: String,
    val link: String,
    /** Style token of the drawn banner: `green` (default), `beige` or `copper`. */
    val style: String = "",
    /** Optional supporting line under the title, rendered with an ellipsis. */
    val subtitle: String = "",
    /** Optional short pill label drawn above the title. */
    val badge: String = "",
    /**
     * `banners.icon_key`, resolved by `categoryIconRes`. Blank falls back to the
     * generic glyph, so a banner is never icon-less.
     */
    val iconKey: String = ""
)