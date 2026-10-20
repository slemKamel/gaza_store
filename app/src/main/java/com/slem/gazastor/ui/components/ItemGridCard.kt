package com.slem.gazastor.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.slem.gazastor.R
import com.slem.gazastor.ui.model.ItemUi
import com.slem.gazastor.ui.theme.Dimens
import com.slem.gazastor.ui.theme.LatinFontFamily

/** Opacity of the beige disc behind the favorite heart. */
private const val FavoriteDiscAlpha = 0.9f

/** Press scale of the quick-add "+" while held. */
private const val QuickAddPressScale = 0.92f

/** Duration of the "+" <-> stepper morph. */
private const val CartControlMorphMs = 200

/**
 * Flat product tile for the 2-column grids: square image, hairline border,
 * copper price. Shared by the home, category, search and favorites screens.
 *
 * The image fill is exposed as [imageContentScale] so screens can opt into a
 * different crop without forking the tile. The text block reserves two title
 * lines and a minimum height, so prices line up across a row even when names
 * differ in length. The heart is only drawn when [onFavoriteClick] is provided.
 *
 * When [onAdd] is supplied the price row ends with the quick-cart control: a
 * "+" that adds straight to the cart (no detail screen), morphing into a compact
 * stepper once [quantity] > 0.
 */
@Composable
fun ItemGridCard(
    item: ItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    imageContentScale: ContentScale = ContentScale.Fit,
    quantity: Int = 0,
    onAdd: (() -> Unit)? = null,
    onIncrease: (() -> Unit)? = null,
    onDecrease: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .pressable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                ProductImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = imageContentScale,
                    fallbackIconKey = item.iconKey
                )
                if (onFavoriteClick != null) {
                    FavoriteHeart(
                        isFavorite = isFavorite,
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .heightIn(min = Dimens.GridCardTextMinHeight)
                    .padding(Dimens.GridCardTextPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PriceLine(item = item)
                    }
                    if (onAdd != null) {
                        QuickCartControl(
                            quantity = quantity,
                            onAdd = onAdd,
                            onIncrease = onIncrease ?: {},
                            onDecrease = onDecrease ?: {}
                        )
                    }
                }
            }
        }
    }
}

/** The bare amount (or fallback string) in copper semibold, kept to one line. */
@Composable
private fun PriceLine(item: ItemUi) {
    item.priceValue?.let { price ->
        AmountText(
            value = price,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    } ?: run {
        if (item.price.isNotEmpty()) {
            Text(
                text = stringResource(R.string.currency_format, item.price),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Quick-cart control: a green "+" on a beige-free flat circle that morphs into
 * a compact stepper. The width animates both ways; height stays put because the
 * quick-add touch target (48dp) is the tallest state.
 */
@Composable
private fun QuickCartControl(
    quantity: Int,
    onAdd: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier.animateContentSize(
            animationSpec = tween(durationMillis = CartControlMorphMs, easing = FastOutSlowInEasing)
        ),
        contentAlignment = Alignment.Center
    ) {
        if (quantity > 0) {
            MiniStepper(
                quantity = quantity,
                onIncrease = onIncrease,
                onDecrease = onDecrease
            )
        } else {
            Box(
                modifier = Modifier
                    .size(Dimens.QuickAddTouchSize)
                    .clip(CircleShape)
                    .pressable(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAdd()
                        },
                        pressScale = QuickAddPressScale
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.QuickAddSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.cart_add_to_cart),
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(Dimens.QuickAddIconSize)
                    )
                }
            }
        }
    }
}

/** Compact − / count / + pill shown on cards whose item is already in the cart. */
@Composable
private fun MiniStepper(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.height(Dimens.MiniStepperHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniStepperButton(
                onClick = onDecrease,
                label = stringResource(R.string.cart_decrease)
            ) {
                Text(
                    text = "\u2212",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontFamily = LatinFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(Dimens.MiniStepperCountWidth)
            )
            MiniStepperButton(
                onClick = onIncrease,
                label = stringResource(R.string.cart_increase)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.MiniStepperIconSize)
                )
            }
        }
    }
}

@Composable
private fun MiniStepperButton(
    onClick: () -> Unit,
    label: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = Dimens.MiniStepperButtonWidth, height = Dimens.MiniStepperHeight)
            .pressable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

/**
 * The save/unsave heart: a flat beige disc (the brand background at 90%) so it
 * stays legible on any photo. Unsaved the heart is an outline in the muted text
 * tone; saved it fills with copper. The icon cross-fades and gives a small pop.
 */
@Composable
private fun FavoriteHeart(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heartScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.12f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "favoriteHeartScale"
    )
    val tint by animateColorAsState(
        targetValue = if (isFavorite) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 180),
        label = "favoriteHeartTint"
    )
    IconButton(
        onClick = onClick,
        modifier = modifier.size(Dimens.FavoriteButtonSize)
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.FavoriteButtonSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background.copy(alpha = FavoriteDiscAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = isFavorite,
                animationSpec = tween(durationMillis = 180),
                label = "favoriteHeartIcon"
            ) { saved ->
                Icon(
                    painter = painterResource(
                        if (saved) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                    ),
                    contentDescription = stringResource(
                        if (saved) R.string.action_remove_favorite
                        else R.string.action_add_favorite
                    ),
                    tint = tint,
                    modifier = Modifier
                        .size(Dimens.FavoriteIconSize)
                        .scale(heartScale)
                )
            }
        }
    }
}
