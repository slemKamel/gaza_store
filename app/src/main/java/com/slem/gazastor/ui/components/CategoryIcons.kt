package com.slem.gazastor.ui.components

import androidx.annotation.DrawableRes
import com.slem.gazastor.R

/**
 * Icon-key resolver for `categories.icon_key` and `banners.icon_key`. Keep the
 * keys in sync with `supabase/schema.sql`.
 */
private val CategoryIcons: Map<String, Int> = mapOf(
    "drinks" to R.drawable.ic_category_drinks,
    "food" to R.drawable.ic_category_food,
    "cleaning" to R.drawable.ic_category_cleaning,
    "fruits_vegetables" to R.drawable.ic_category_fruits_vegetables,
    "dairy" to R.drawable.ic_category_dairy,
    "bakery" to R.drawable.ic_category_bakery,
    "snacks" to R.drawable.ic_category_snacks,
    "frozen" to R.drawable.ic_category_frozen,
    "personal_care" to R.drawable.ic_category_personal_care,
    "home" to R.drawable.ic_category_home,
    "baby" to R.drawable.ic_category_baby,
    "pets" to R.drawable.ic_category_pets,
    // Banner-only keys (see the "Banners" note in AGENTS.md).
    "delivery" to R.drawable.ic_banner_delivery,
    "offer" to R.drawable.ic_banner_offer,
    "fresh" to R.drawable.ic_banner_fresh
)

/**
 * Resolves an icon key (trimmed, case-insensitive) to its outlined glyph. An
 * unknown or blank key falls back to the generic glyph, so a bad value can never
 * leave a chip or a banner empty.
 */
@DrawableRes
fun categoryIconRes(iconKey: String): Int =
    CategoryIcons[iconKey.trim().lowercase()] ?: R.drawable.ic_category_all
