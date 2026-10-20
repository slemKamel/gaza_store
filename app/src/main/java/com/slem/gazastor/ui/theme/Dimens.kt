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

    /** Inner padding of a grid card's text block. */
    val GridCardTextPadding = 12.dp

    /** Minimum height of a grid card's text block (title lines + cart control row). */
    val GridCardTextMinHeight = 120.dp

    /** Diameter of the circular favorite button on a grid card. */
    val FavoriteButtonSize = 36.dp

    /** Size of the heart glyph inside the favorite button. */
    val FavoriteIconSize = 18.dp

    /** Visible diameter of the quick-add "+" on a grid card. */
    val QuickAddSize = 36.dp

    /** Touch target (and row height) of the quick-add control. */
    val QuickAddTouchSize = 48.dp

    /** Size of the "+" glyph inside the quick-add button. */
    val QuickAddIconSize = 18.dp

    /** Height of the compact stepper that replaces the "+" once an item is in the cart. */
    val MiniStepperHeight = 32.dp

    /** Width of each − / + hit cell inside the compact stepper. */
    val MiniStepperButtonWidth = 28.dp

    /** Width of the quantity cell inside the compact stepper. */
    val MiniStepperCountWidth = 20.dp

    /** Size of the − / + glyphs inside the compact stepper. */
    val MiniStepperIconSize = 14.dp

    /** Size of the fallback icon drawn while a product image loads. */
    val ImagePlaceholderIcon = 30.dp

    // --- Home ----------------------------------------------------------------

    /** Height of the fixed compact brand bar at the top of Home. */
    val CompactBarHeight = 48.dp

    /** Size of the outlined brand mark inside the compact brand bar. */
    val CompactBarMarkSize = 32.dp

    /** Vertical gap between the Arabic and the Latin line of the brand lockup. */
    val BrandLineGap = 6.dp

    /**
     * Tighter lockup gap for the compact brand bar, where the two lines must
     * read as one unit inside [CompactBarHeight].
     */
    val CompactBrandLineGap = 2.dp

    /** Vertical gap between the pinned bar, the search pill and the grid. */
    val PinnedGap = 6.dp

    /** Height of the tappable search pill on Home. */
    val SearchBarHeight = 46.dp

    /** Height of the auto-advancing banner pager card. */
    val BannerHeight = 150.dp

    /** Padding inside a drawn banner; caps the text block against [BannerHeight]. */
    val BannerPadding = 16.dp

    /** Gap between the badge, title and subtitle inside a drawn banner. */
    val BannerStackGap = 4.dp

    /** Size of the large outlined glyph at the end corner of a drawn banner. */
    val BannerIconSize = 96.dp

    /** Diameter of the thin copper ring drawn around the glyph on green banners. */
    val BannerIconRingSize = 112.dp

    /**
     * How far the glyph assembly is pushed past the banner's end edge; the card
     * clips it, so the glyph reads as partly cropped by the corner.
     */
    val BannerIconBleed = 26.dp

    /** Horizontal padding inside the badge pill. */
    val BannerBadgeHPadding = 10.dp

    /** Vertical padding inside the badge pill. */
    val BannerBadgeVPadding = 4.dp

    /** Diameter of the circular category chip (icon disc, image fallback or skeleton). */
    val CategoryImageSize = 72.dp

    /** Size of the outlined category glyph drawn inside a [CategoryImageSize] disc. */
    val CategoryIconSize = 32.dp

    // --- Detail ----------------------------------------------------------------

    /**
     * Height of the detail screen's image block: the photo pager, or the single
     * fallback panel when the product has no photo.
     */
    val DetailImageHeight = 260.dp

    // --- Section title --------------------------------------------------------

    /** Copper accent rule drawn above a [com.slem.gazastor.ui.components.SectionTitle]. */
    val SectionRuleWidth = 24.dp

    /** Thickness of the copper accent rule above a section title. */
    val SectionRuleHeight = 3.dp

    // --- Skeleton placeholders -------------------------------------------------

    /** Width of the text bar standing in for a section title. */
    val SkeletonTitleWidth = 110.dp

    /** Height of the title / sub-title bars in the skeleton. */
    val SkeletonTitleHeight = 18.dp

    /** Height of a card's title line in the skeleton. */
    val SkeletonTextLineHeight = 12.dp

    /** Width of the price bar in a skeleton card. */
    val SkeletonPriceBarWidth = 56.dp

    /** Height of the price bar in a skeleton card. */
    val SkeletonPriceBarHeight = 14.dp

    /** Width of the name bar under a skeleton category circle. */
    val SkeletonCategoryLabelWidth = 48.dp

    /** Height of the name bar under a skeleton category circle. */
    val SkeletonCategoryLabelHeight = 10.dp
}
