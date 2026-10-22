package com.slem.gazastor.ui.screens.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.pressable
import com.slem.gazastor.ui.model.CardBrand
import com.slem.gazastor.ui.theme.Dimens
import com.slem.gazastor.ui.theme.LatinFontFamily

/** How far a payment-method card shrinks while it is held down. */
private const val PaymentPressScale = 0.98f

/** Gap between the glyph disc and the text of a payment-method card. */
private val MethodIconGap = 12.dp

/** Duration of the expand / collapse of a payment method form. */
private const val MethodFormAnimationMs = 200

/**
 * A payment method as a radio card.
 *
 * The card itself is the radio button: the whole surface is the touch target
 * and it carries the selected border, while the dot on the end marks the
 * state. [expandedContent] is only drawn while the card is selected, so the
 * form of a method appears and disappears with it.
 */
@Composable
internal fun PaymentMethodCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    expandedContent: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pressable(onClick = onSelect, pressScale = PaymentPressScale),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) Dimens.PaymentMethodSelectedBorder else Dimens.Border,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = Dimens.PaymentMethodCardMinHeight)
                .padding(Dimens.PaymentMethodCardPadding)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PaymentMethodIcon(iconRes = iconRes)

                Spacer(modifier = Modifier.width(MethodIconGap))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(MethodIconGap))

                PaymentRadioDot(selected = selected)
            }

            if (expandedContent != null) {
                PaymentMethodExpansion(
                    visible = selected,
                    content = expandedContent
                )
            }
        }
    }
}

/**
 * The body of a payment-method card. It expands and fades in over 200ms with
 * the brand's easing, and collapses again when another method is picked.
 */
@Composable
internal fun PaymentMethodExpansion(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(
                durationMillis = MethodFormAnimationMs,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = MethodFormAnimationMs,
                easing = FastOutSlowInEasing
            )
        ),
        exit = shrinkVertically(
            animationSpec = tween(
                durationMillis = MethodFormAnimationMs,
                easing = FastOutSlowInEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = MethodFormAnimationMs,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        Column {
            Spacer(modifier = Modifier.height(Dimens.PaymentFieldGap))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(Dimens.PaymentFieldGap))
            content()
        }
    }
}

/**
 * The glyph of a payment method, on a beige disc with a hairline ring - the
 * same chip language the home categories use.
 */
@Composable
private fun PaymentMethodIcon(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(Dimens.PaymentMethodIconDiscSize)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .border(Dimens.Border, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.PaymentMethodIconSize)
        )
    }
}

/**
 * The radio dot of a payment-method card: a hairline ring that turns copper,
 * with a filled copper core once the method is selected.
 */
@Composable
private fun PaymentRadioDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(Dimens.PaymentRadioDotSize)
            .clip(CircleShape)
            .border(
                width = Dimens.PaymentRadioDotBorder,
                color = if (selected) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(Dimens.PaymentRadioDotInnerSize)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
            )
        }
    }
}

/**
 * A selectable e-wallet chip. The wallet names are Latin brands, so they are
 * set in Inter while the rest of the form stays in the Arabic face.
 */
@Composable
internal fun WalletChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = Dimens.PaymentChipHeight)
            .pressable(onClick = onClick, pressScale = PaymentPressScale),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) Dimens.PaymentMethodSelectedBorder else Dimens.Border,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Dimens.PaymentChipHPadding,
                vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = LatinFontFamily,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1
            )
            if (selected) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(Dimens.PaymentRadioDotInnerSize)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                )
            }
        }
    }
}

/**
 * The VISA / Mastercard badge shown next to a recognised card number.
 */
@Composable
internal fun CardBrandBadge(
    brand: CardBrand,
    modifier: Modifier = Modifier
) {
    val label = stringResource(
        when (brand) {
            CardBrand.Visa -> R.string.payment_card_brand_visa
            CardBrand.Mastercard -> R.string.payment_card_brand_mastercard
        }
    )
    Surface(
        modifier = modifier.height(Dimens.PaymentBrandBadgeHeight),
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = LatinFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
