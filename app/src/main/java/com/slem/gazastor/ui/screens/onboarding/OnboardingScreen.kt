package com.slem.gazastor.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.BrandLockup
import com.slem.gazastor.ui.components.BrandMark
import com.slem.gazastor.ui.components.EssentialsShowcase
import com.slem.gazastor.ui.components.QuickOrdersShowcase
import com.slem.gazastor.ui.theme.Dimens
import kotlinx.coroutines.launch

/** What a page shows above the shared headline. */
private sealed interface PageVisual {
    /** The brand lockup, used by the first page. */
    data object Brand : PageVisual

    /** The animated category showcase, used by the second page. */
    data object Showcase : PageVisual

    /** The animated quick-orders scene (ANIMATION_62), used by the third page. */
    data object Orders : PageVisual
}

private data class OnboardingPage(
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    val visual: PageVisual
)

private val OnboardingPages = listOf(
    OnboardingPage(
        titleRes = 0,
        subtitleRes = 0,
        visual = PageVisual.Brand
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_title_essentials,
        subtitleRes = 0,
        visual = PageVisual.Showcase
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_title_orders,
        subtitleRes = R.string.onboarding_subtitle_orders,
        visual = PageVisual.Orders
    )
)

/**
 * Three quiet-luxury intro pages: the brand lockup, the animated category
 * showcase, then the animated quick-orders scene. Skippable from the top
 * corner (start in RTL = right).
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    OnboardingContent(onComplete = { viewModel.complete(onFinish) })
}

/** Stateless intro pager; [onComplete] persists the flag and leaves onboarding. */
@Composable
internal fun OnboardingContent(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == OnboardingPages.lastIndex

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(onClick = onComplete) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                PageContent(
                    page = OnboardingPages[pageIndex],
                    isVisible = pagerState.currentPage == pageIndex
                )
            }

            PageIndicator(
                pageCount = OnboardingPages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    .height(Dimens.ButtonHeight)
            ) {
                Text(
                    text = stringResource(
                        if (isLastPage) R.string.onboarding_start else R.string.onboarding_next
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun PageContent(
    page: OnboardingPage,
    isVisible: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val visual = page.visual) {
            PageVisual.Brand -> BrandHero(isVisible = isVisible)
            PageVisual.Showcase -> ShowcaseHero(titleRes = page.titleRes, isVisible = isVisible)
            PageVisual.Orders -> OrdersHero(
                titleRes = page.titleRes,
                subtitleRes = page.subtitleRes,
                isVisible = isVisible
            )
        }
    }
}

/** The animated showcase, capped so the headline still fits on small screens. */
@Composable
private fun ShowcaseHero(@StringRes titleRes: Int, isVisible: Boolean) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val side = if (maxHeight.value.isFinite()) min(maxWidth, maxHeight * 0.72f) else maxWidth
        EssentialsShowcase(
            visible = isVisible,
            modifier = Modifier.size(side)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    PageHeadline(titleRes = titleRes, subtitleRes = 0)
}

/** The animated quick-orders scene, capped so the headline still fits. */
@Composable
private fun OrdersHero(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    isVisible: Boolean
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val side = if (maxHeight.value.isFinite()) min(maxWidth, maxHeight * 0.72f) else maxWidth
        QuickOrdersShowcase(
            visible = isVisible,
            modifier = Modifier.width(side)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    PageHeadline(titleRes = titleRes, subtitleRes = subtitleRes)
}

/** Green headline plus the optional muted body copy. */
@Composable
private fun PageHeadline(@StringRes titleRes: Int, @StringRes subtitleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )

    if (subtitleRes != 0) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(subtitleRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BrandHero(isVisible: Boolean) {
    val enterProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "brandHeroProgress"
    )
    val alpha = enterProgress
    val scale = 0.92f + (0.08f * enterProgress)

    Column(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = BrandMark,
            contentDescription = null,
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .width(48.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
        )

        Spacer(modifier = Modifier.height(20.dp))

        BrandLockup(wordmarkStyle = MaterialTheme.typography.displayMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (selected) 24.dp else 8.dp,
                label = "onboardingIndicatorWidth"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(width)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
            )
        }
    }
}
