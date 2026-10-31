package com.slem.gazastor.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slem.gazastor.ui.theme.Dimens

/**
 * Loading skeleton for the home screen: instead of a lone spinner, the real
 * layout (banner card, indicator dots, copper-ruled section headings, circular
 * category chips, product cards) is drawn as pulsing beige blocks, so content
 * appears to "settle" into place.
 *
 * The blocks breathe via a gentle alpha pulse rather than a sweeping gradient
 * shimmer, keeping with the no-gradient brand rule. Pass `pulse = false` to
 * freeze them: the same beige scaffolding then stands behind an error card
 * instead of collapsing into blank space.
 */

/** Shared breathing clock: 0.45 -> 1.0 alpha, reversed, ~1.8s round trip. */
@Composable
private fun rememberSkeletonAlpha(): State<Float> {
    val transition = rememberInfiniteTransition(label = "skeleton")
    return transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
}

/** A single placeholder block, pulsing unless [pulse] is false. */
@Composable
private fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    pulse: Boolean = true
) {
    val pulsing = rememberSkeletonAlpha().value
    Spacer(
        modifier = modifier
            .background(color, shape)
            .graphicsLayer { alpha = if (pulse) pulsing else 1f }
    )
}

/**
 * A single free-standing placeholder line, for small blocks that are not part
 * of a full skeleton layout (the account profile card's loading name).
 */
@Composable
fun SkeletonLine(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    pulse: Boolean = true
) {
    SkeletonBlock(
        modifier = modifier
            .width(width)
            .height(height),
        shape = MaterialTheme.shapes.extraSmall,
        pulse = pulse
    )
}

/** Stand-in for a [SectionTitle]: the copper accent rule over a beige bar. */
@Composable
private fun SkeletonTitle(pulse: Boolean) {
    Column {
        SkeletonBlock(
            modifier = Modifier
                .width(Dimens.SectionRuleWidth)
                .height(Dimens.SectionRuleHeight),
            color = MaterialTheme.colorScheme.secondary,
            pulse = pulse
        )
        Spacer(modifier = Modifier.height(6.dp))
        SkeletonBlock(
            modifier = Modifier
                .width(Dimens.SkeletonTitleWidth)
                .height(Dimens.SkeletonTitleHeight),
            pulse = pulse
        )
    }
}

/**
 * Full-width skeleton section mirroring the banner, the pager dots, the two
 * section headings and the category row. Must be emitted as a single root child
 * of a grid item, like the real sections.
 */
@Composable
fun SkeletonSection(modifier: Modifier = Modifier, pulse: Boolean = true) {
    Column(modifier = modifier) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant)
        ) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.BannerHeight),
                shape = MaterialTheme.shapes.large,
                pulse = pulse
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonDots(pulse = pulse)
        Spacer(modifier = Modifier.height(Dimens.SectionSpacing))
        SkeletonTitle(pulse = pulse)
        Spacer(modifier = Modifier.height(Dimens.SectionSpacing))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)) {
            items(6) { SkeletonCategory(pulse = pulse) }
        }
        Spacer(modifier = Modifier.height(Dimens.SectionSpacing))
        SkeletonTitle(pulse = pulse)
    }
}

/** The three pager dots (the real row only shows them when there are several). */
@Composable
private fun SkeletonDots(pulse: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) {
            SkeletonBlock(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(6.dp),
                shape = CircleShape,
                pulse = pulse
            )
        }
    }
}

/** Ringed circular placeholder mirroring a category chip. */
@Composable
private fun SkeletonCategory(pulse: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SkeletonBlock(
            modifier = Modifier
                .size(Dimens.CategoryImageSize)
                .border(Dimens.Border, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            shape = CircleShape,
            pulse = pulse
        )
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBlock(
            modifier = Modifier
                .width(Dimens.SkeletonCategoryLabelWidth)
                .height(Dimens.SkeletonCategoryLabelHeight),
            shape = MaterialTheme.shapes.extraSmall,
            pulse = pulse
        )
    }
}

/** Product tile placeholder mirroring [ItemGridCard]'s chrome and text block. */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier, pulse: Boolean = true) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RectangleShape,
                pulse = pulse
            )
            Column(
                modifier = Modifier
                    .heightIn(min = Dimens.GridCardTextMinHeight)
                    .padding(Dimens.GridCardTextPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(Dimens.SkeletonTextLineHeight),
                    shape = MaterialTheme.shapes.extraSmall,
                    pulse = pulse
                )
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(Dimens.SkeletonTextLineHeight),
                    shape = MaterialTheme.shapes.extraSmall,
                    pulse = pulse
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.QuickAddTouchSize),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBlock(
                        modifier = Modifier
                            .width(Dimens.SkeletonPriceBarWidth)
                            .height(Dimens.SkeletonPriceBarHeight),
                        shape = MaterialTheme.shapes.extraSmall,
                        pulse = pulse
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    SkeletonBlock(
                        modifier = Modifier.size(Dimens.QuickAddSize),
                        shape = CircleShape,
                        pulse = pulse
                    )
                }
            }
        }
    }
}
