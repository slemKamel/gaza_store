package com.slem.gazastor.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Shared layout dimensions, so screens stop hardcoding the same numbers
 * (button height, hairline borders, screen padding, grid gaps).
 */
object Dimens {
    /** Primary call-to-action height used across onboarding, auth, detail, cart, checkout. */
    val ButtonHeight = 52.dp

    /** Hairline border width for the flat "Quiet Luxury" surfaces. */
    val Border = 1.dp

    /** Standard horizontal/vertical padding inside scrollable screens. */
    val ScreenPadding = 16.dp

    /** Gap between a section title and its content. */
    val SectionSpacing = 12.dp

    /** Gap between grid cells and stacked cards. */
    val GridSpacing = 12.dp

    /** Size of the fallback icon drawn while a product image loads. */
    val ImagePlaceholderIcon = 30.dp
}
