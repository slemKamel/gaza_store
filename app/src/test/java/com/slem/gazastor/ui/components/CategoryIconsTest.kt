package com.slem.gazastor.ui.components

import com.slem.gazastor.R
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The Home chip resolves `categories.icon_key` through [categoryIconRes], and a
 * key the app cannot draw must still render something, so every unknown shape
 * lands on the generic category glyph.
 */
class CategoryIconsTest {

    @Test
    fun knownKeys_resolveToTheirOwnGlyph() {
        val expected = mapOf(
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

        expected.forEach { (key, glyph) ->
            assertEquals("icon key \"$key\"", glyph, categoryIconRes(key))
        }
        // Guards against two keys being wired to the same drawable by mistake.
        assertEquals("keys must map to distinct glyphs", expected.size, expected.values.toSet().size)
    }

    @Test
    fun keys_ignoreSurroundingSpaceAndCase() {
        assertEquals(R.drawable.ic_category_drinks, categoryIconRes("  Drinks "))
        assertEquals(R.drawable.ic_category_fruits_vegetables, categoryIconRes("FRUITS_VEGETABLES"))
    }

    @Test
    fun unknownKeys_fallBackToTheGenericGlyph() {
        listOf("", "   ", "\t", "watches", "drinks_2", "أقسام").forEach { key ->
            assertEquals("icon key \"$key\"", R.drawable.ic_category_all, categoryIconRes(key))
        }
    }
}
