package com.slem.gazastor.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.BrandLockup
import com.slem.gazastor.ui.components.BrandMarkOutlined
import com.slem.gazastor.ui.components.EmptyState
import com.slem.gazastor.ui.components.ErrorState
import com.slem.gazastor.ui.components.ItemGridCard
import com.slem.gazastor.ui.components.ProductImage
import com.slem.gazastor.ui.components.QuickCartActions
import com.slem.gazastor.ui.components.SectionTitle
import com.slem.gazastor.ui.components.SkeletonCard
import com.slem.gazastor.ui.components.SkeletonSection
import com.slem.gazastor.ui.components.categoryIconRes
import com.slem.gazastor.ui.components.fullWidthItem
import com.slem.gazastor.ui.components.pressable
import com.slem.gazastor.ui.model.BannerUi
import com.slem.gazastor.ui.model.CategoryUi
import com.slem.gazastor.ui.model.ItemUi
import com.slem.gazastor.ui.screens.cart.CartViewModel
import com.slem.gazastor.ui.screens.cart.quickCartActions
import com.slem.gazastor.ui.theme.ArabicBrandFontFamily
import com.slem.gazastor.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Pause between automatic banner page advances. */
private const val BannerAutoAdvanceMs = 4000L

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    onCategoryClick: (String) -> Unit,
    onItemClick: (String) -> Unit,
    onSearchClick: () -> Unit = {},
    gridState: LazyGridState = rememberLazyGridState()
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val cartState by cartViewModel.uiState.collectAsState()

    // The staggered entrance belongs to the screen's first appearance. The tab
    // returns from its saved back-stack entry, so `rememberSaveable` still
    // reports the reveal that already happened and a restored tab shows its
    // content instantly. The value is captured for the whole appearance with
    // `remember`, so cells that scroll into view during that first visit still
    // stagger in.
    var hasRevealed by rememberSaveable { mutableStateOf(false) }
    val animateEntrance = remember { !hasRevealed }

    LaunchedEffect(uiState.hasContent) {
        if (uiState.hasContent) hasRevealed = true
    }

    HomeContent(
        uiState = uiState,
        favoriteIds = favoriteIds,
        onCategoryClick = onCategoryClick,
        onItemClick = onItemClick,
        onFavoriteClick = viewModel::toggleFavorite,
        onRetry = viewModel::loadData,
        onSearchClick = onSearchClick,
        quickCart = cartViewModel.quickCartActions(cartState),
        animateEntrance = animateEntrance,
        gridState = gridState
    )
}

/**
 * Stateless home screen: a fixed compact brand bar and pinned search pill sit
 * above the scrollable grid (banner pager, circular category chips, recent
 * products) with a layout-mirroring skeleton while loading. Nothing in the
 * header scrolls, and it never overlaps the content beneath it.
 *
 * Every full-width section must resolve to a single root child of the grid —
 * emitting SectionTitle/Spacer/rows as separate grid children collapsed and
 * clipped them on device.
 *
 * [animateEntrance] is turned off in previews: the reveal animations start
 * from a hidden state that a static preview frame would never leave.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onCategoryClick: (String) -> Unit,
    onItemClick: (String) -> Unit,
    onRetry: () -> Unit,
    onSearchClick: () -> Unit = {},
    favoriteIds: Set<String> = emptySet(),
    onFavoriteClick: (ItemUi) -> Unit = {},
    quickCart: QuickCartActions = QuickCartActions(),
    animateEntrance: Boolean = true,
    // Owned by the navigation graph so the Home tab can scroll it back to the
    // top when the user taps the already-selected tab.
    gridState: LazyGridState = rememberLazyGridState()
) {

    // Once content scrolls under the pinned area, a hairline separator fades
    // in (200ms) to divide it from the moving grid.
    val scrolled by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
        }
    }
    val pinnedHairlineAlpha = animateFloatAsState(
        targetValue = if (scrolled) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "pinnedHairline"
    )
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Status bar only — the parent's bottom navigation bar owns the
        // bottom edge (and its system inset), so content must not pad it again.
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        // paddingValues.top clears the status bar (Home has no TopAppBar to
        // carry it); its bottom is 0, so the grid ends at the bottom bar.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CompactBrandBar()
            Spacer(modifier = Modifier.height(Dimens.PinnedGap))
            SearchPill(onSearchClick = onSearchClick)
            Spacer(modifier = Modifier.height(Dimens.PinnedGap))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRetry,
                    state = pullToRefreshState,
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            state = pullToRefreshState,
                            isRefreshing = uiState.isRefreshing,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 0.dp,
                            bottom = Dimens.ScreenPadding,
                            start = Dimens.ScreenPadding,
                            end = Dimens.ScreenPadding
                        ),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
                        verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
                    ) {
                        // The placeholder scaffolding stands in while the first
                        // load runs, and stays (frozen, no pulse) behind the
                        // error card so a failed load never leaves blank space.
                        val showPlaceholders = !uiState.hasContent &&
                            (uiState.isLoading || uiState.isError)
                        val placeholderPulse = !uiState.isError

                        if (uiState.isError) {
                            fullWidthItem(key = "error") {
                                RevealSection(animateEntrance = animateEntrance) {
                                    ErrorState(onRetry = onRetry)
                                }
                            }
                        }

                        if (showPlaceholders) {
                            fullWidthItem(key = "skeleton") {
                                SkeletonSection(pulse = placeholderPulse)
                            }
                            repeat(4) { index ->
                                item(key = "skeleton-$index") {
                                    SkeletonCard(pulse = placeholderPulse)
                                }
                            }
                        }

                        if (uiState.isEmpty) {
                            fullWidthItem(key = "empty") {
                                RevealSection(animateEntrance = animateEntrance) {
                                    EmptyState()
                                }
                            }
                        }

                        if (uiState.banners.isNotEmpty()) {
                            fullWidthItem(key = "banners") {
                                RevealSection(delayMillis = 60, animateEntrance = animateEntrance) {
                                    BannerPager(banners = uiState.banners)
                                }
                            }
                        }

                        if (uiState.categories.isNotEmpty()) {
                            fullWidthItem(key = "categories") {
                                RevealSection(delayMillis = 120, animateEntrance = animateEntrance) {
                                    SectionTitle(stringResource(R.string.home_categories))
                                    Spacer(modifier = Modifier.height(Dimens.SectionSpacing))
                                    CategoryRow(categories = uiState.categories, onClick = onCategoryClick)
                                }
                            }
                        }

                        if (uiState.recentItems.isNotEmpty()) {
                            fullWidthItem(key = "recent-title") {
                                RevealSection(delayMillis = 180, animateEntrance = animateEntrance) {
                                    SectionTitle(stringResource(R.string.home_recent_items))
                                }
                            }
                            itemsIndexed(uiState.recentItems, key = { _, item -> item.id }) { index, item ->
                                StaggeredGridItem(index = index, animateEntrance = animateEntrance) {
                                    ItemGridCard(
                                        item = item,
                                        onClick = { onItemClick(item.id) },
                                        isFavorite = item.id in favoriteIds,
                                        onFavoriteClick = { onFavoriteClick(item) },
                                        quantity = quickCart.quantities[item.id] ?: 0,
                                        onAdd = { quickCart.onAdd(item) },
                                        onIncrease = { quickCart.onIncrease(item.id) },
                                        onDecrease = { quickCart.onDecrease(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Scroll boundary: the layer block reads the animated alpha so
                // the grid never recomposes while the line fades in.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(Dimens.Border)
                        .graphicsLayer { alpha = pinnedHairlineAlpha.value }
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}

/**
 * Permanent header: the outlined brand mark + bilingual lockup over a solid
 * beige backdrop. Always opaque and never animated — the scroll-faded
 * separator below the search pill is the only divider in the pinned area.
 */
@Composable
private fun CompactBrandBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.CompactBarHeight)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.ScreenPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            imageVector = BrandMarkOutlined,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            modifier = Modifier.size(Dimens.CompactBarMarkSize)
        )
        BrandLockup(
            wordmarkStyle = MaterialTheme.typography.titleLarge.copy(
                fontFamily = ArabicBrandFontFamily,
                fontWeight = FontWeight.Bold,
                // 28sp still clears Amiri's tallest ink at 22sp; the leading
                // trim is what lets the bar drop to Dimens.CompactBarHeight.
                lineHeight = 28.sp
            ),
            latinStyle = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp),
            lineGap = Dimens.CompactBrandLineGap
        )
    }
}

/**
 * Pinned search pill leading to the Search tab: fully rounded, flat surface
 * fill, hairline border and a copper icon — fixed above the scrolling grid.
 */
@Composable
private fun SearchPill(onSearchClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding)
            .height(Dimens.SearchBarHeight)
            .pressable(onClick = onSearchClick, pressScale = 0.98f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.search_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Auto-advancing banner pager: flat panels drawn in Compose from each banner's
 * fields (no photo, no gradient, per brand rules) inside a hairline card, with
 * copper indicator dots below. The loop pauses while the user drags and stops
 * when the section scrolls off screen (the grid disposes it).
 */
@Composable
private fun BannerPager(banners: List<BannerUi>) {
    val pageCount = banners.size
    val pagerState = rememberPagerState(pageCount = { pageCount })

    LaunchedEffect(pagerState, pageCount) {
        if (pageCount <= 1) return@LaunchedEffect
        while (isActive) {
            delay(BannerAutoAdvanceMs)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % pageCount)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.BannerHeight),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                BannerPage(banner = banners[page])
            }
        }

        if (pageCount > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { page ->
                    val active = pagerState.currentPage == page
                    val color by animateColorAsState(
                        targetValue = if (active) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        animationSpec = tween(durationMillis = 200),
                        label = "bannerDot"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(6.dp)
                            .background(color, CircleShape)
                    )
                }
            }
        }
    }
}

/**
 * A single banner page, drawn entirely in Compose from the banner's fields
 * (title / subtitle / badge / style / icon_key) — no photo, no gradient and no
 * click handling: [BannerUi.link] stays unused. A legacy photo-only banner
 * (image and no title) still renders its image the old way.
 */
@Composable
private fun BannerPage(banner: BannerUi) {
    // Legacy photo-only banner: keep the old behaviour (cropped image, no bar).
    if (banner.title.isBlank() && banner.imageUrl.isNotBlank()) {
        ProductImage(
            model = banner.imageUrl,
            contentDescription = banner.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        return
    }

    val scheme = MaterialTheme.colorScheme
    // Unknown or blank style falls back to green, as documented in schema.sql.
    val style = banner.style.trim().lowercase()
    val container = when (style) {
        "beige" -> scheme.surfaceVariant
        "copper" -> scheme.secondary
        else -> scheme.primary
    }
    val textColor = when (style) {
        "beige" -> scheme.primary
        "copper" -> scheme.onSecondary
        else -> scheme.onPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.large)
            .background(container)
    ) {
        // Drawn first, so the low-alpha glyph reads as a watermark behind the copy.
        BannerGlyph(
            banner = banner,
            style = style,
            modifier = Modifier.align(Alignment.BottomEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.BannerPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.BannerStackGap, Alignment.CenterVertically)
        ) {
            if (banner.badge.isNotBlank()) {
                BannerBadge(text = banner.badge, style = style)
            }
            Text(
                text = banner.title,
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (banner.subtitle.isNotBlank()) {
                Text(
                    text = banner.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * The short label above a drawn banner's title: a filled copper pill on green,
 * plain copper text on beige and an outlined pill on copper.
 */
@Composable
private fun BannerBadge(text: String, style: String) {
    val scheme = MaterialTheme.colorScheme
    when (style) {
        "beige" -> Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = scheme.secondary
        )
        "copper" -> Box(
            modifier = Modifier
                .border(Dimens.Border, scheme.onSecondary, CircleShape)
                .padding(horizontal = Dimens.BannerBadgeHPadding, vertical = Dimens.BannerBadgeVPadding)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSecondary
            )
        }
        else -> Box(
            modifier = Modifier
                .background(scheme.secondary, CircleShape)
                .padding(horizontal = Dimens.BannerBadgeHPadding, vertical = Dimens.BannerBadgeVPadding)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSecondary
            )
        }
    }
}

/**
 * The oversized outlined glyph at the banner's end corner: a low-alpha watermark
 * that the card edge crops, wrapped in a thin copper ring on green pages. The
 * bleed flips in RTL ([androidx.compose.foundation.layout.offset] is physical),
 * so the glyph always leaves through the layout's end edge.
 */
@Composable
private fun BannerGlyph(
    banner: BannerUi,
    style: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val tint = when (style) {
        "beige" -> scheme.primary
        "copper" -> scheme.onSecondary
        else -> scheme.onPrimary
    }
    val alpha = if (style == "beige") 0.12f else 0.15f
    val hasRing = style != "beige" && style != "copper"
    val bleed = if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
        -Dimens.BannerIconBleed
    } else {
        Dimens.BannerIconBleed
    }

    Box(
        modifier = modifier
            .offset(x = bleed)
            .size(if (hasRing) Dimens.BannerIconRingSize else Dimens.BannerIconSize)
            .then(
                if (hasRing) {
                    Modifier.border(Dimens.Border, scheme.secondary, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = categoryIconRes(banner.iconKey)),
            contentDescription = null,
            tint = tint.copy(alpha = alpha),
            modifier = Modifier.size(Dimens.BannerIconSize)
        )
    }
}

/** Circular category chips with a scroll-driven edge fade (alpha, not a gradient). */
@Composable
private fun CategoryRow(
    categories: List<CategoryUi>,
    onClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
    ) {
        itemsIndexed(categories, key = { _, item -> item.id }) { index, category ->
            CategoryChip(
                category = category,
                onClick = { onClick(category.id) },
                // Read in the layer block: only the layer invalidates per
                // scroll frame, the chip itself never recomposes.
                modifier = Modifier.graphicsLayer { alpha = listState.edgeFadeAlpha(index) }
            )
        }
    }
}

/**
 * Alpha for [index] in [listState]: chips mostly scrolled out of the row fade
 * toward 0.35, so the row dissolves at both edges without a gradient mask.
 */
private fun LazyListState.edgeFadeAlpha(index: Int): Float {
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return 1f
    val visible = (
        minOf(item.offset + item.size, info.viewportEndOffset) -
            maxOf(item.offset, info.viewportStartOffset)
        ).toFloat()
    return (visible / item.size).coerceIn(0.35f, 1f)
}

/** Circular category tile: outlined glyph (or legacy image) over an ellipsized name. */
@Composable
private fun CategoryChip(
    category: CategoryUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(Dimens.CategoryImageSize)
            .pressable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.CategoryImageSize)
                .clip(CircleShape)
                // Same beige as the loading placeholder, so a chip does not pop
                // when the skeleton swaps out.
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(Dimens.Border, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // A glyph key wins over the image; the image stays as the legacy
            // fallback, and the generic glyph covers categories with neither.
            if (category.iconKey.isNotBlank() || category.imageUrl.isBlank()) {
                Icon(
                    painter = painterResource(categoryIconRes(category.iconKey)),
                    contentDescription = category.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.CategoryIconSize)
                )
            } else {
                ProductImage(
                    model = category.imageUrl,
                    contentDescription = category.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Fades + slides a full-width section in shortly after the skeleton swaps out
 * for real content, staggered by [delayMillis].
 */
@Composable
private fun RevealSection(
    delayMillis: Int = 0,
    animateEntrance: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var visible by remember { mutableStateOf(!animateEntrance) }
    if (animateEntrance) {
        LaunchedEffect(Unit) { visible = true }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            tween(durationMillis = 400, delayMillis = delayMillis, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            animationSpec = tween(durationMillis = 400, delayMillis = delayMillis, easing = FastOutSlowInEasing)
        ) { it / 4 }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

/** Grid cell entrance: same fade/slide, staggered by grid position. */
@Composable
private fun StaggeredGridItem(
    index: Int,
    animateEntrance: Boolean = true,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(!animateEntrance) }
    if (animateEntrance) {
        LaunchedEffect(Unit) { visible = true }
    }
    val delay = minOf(index, 6) * 50
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            tween(durationMillis = 400, delayMillis = delay, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            animationSpec = tween(durationMillis = 400, delayMillis = delay, easing = FastOutSlowInEasing)
        ) { it / 4 }
    ) {
        content()
    }
}
