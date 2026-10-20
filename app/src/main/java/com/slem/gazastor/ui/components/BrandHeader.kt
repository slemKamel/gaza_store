package com.slem.gazastor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

/**
 * The shared brand hero: the [BrandMark], the bilingual [BrandLockup], the
 * copper rule and an optional title/subtitle. The splash uses the same pieces
 * directly; auth uses this so the brand moment is identical everywhere.
 *
 * The large variant animates in (the mark scales/fades, then the text) to make
 * the auth screen feel like a continuation of the splash. The compact variant
 * is a small static row for tight spaces like the Account tab.
 */
@Composable
fun BrandHeader(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    animated: Boolean = false,
    title: String? = null,
    subtitle: String? = null
) {
    if (compact) {
        CompactBrandHeader(modifier = modifier)
        return
    }

    val markProgress = remember { Animatable(if (animated) 0f else 1f) }
    var textVisible by remember { mutableStateOf(!animated) }

    LaunchedEffect(animated) {
        if (animated) {
            markProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
            )
            textVisible = true
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = BrandMark,
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    alpha = markProgress.value
                    scaleX = lerp(0.85f, 1f, markProgress.value)
                    scaleY = lerp(0.85f, 1f, markProgress.value)
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(tween(durationMillis = 400, easing = FastOutSlowInEasing))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BrandLockup()
                Spacer(modifier = Modifier.height(14.dp))
                Box(Modifier.width(48.dp).height(3.dp).background(MaterialTheme.colorScheme.secondary))
                title?.let {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                subtitle?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Small static brand row for tight spaces (Account tab header). */
@Composable
private fun CompactBrandHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            imageVector = BrandMark,
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        BrandLockup(
            wordmarkStyle = MaterialTheme.typography.titleLarge,
            latinStyle = MaterialTheme.typography.labelMedium
        )
    }
}
