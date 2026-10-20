package com.slem.gazastor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.slem.gazastor.R
import com.slem.gazastor.ui.theme.Dimens

/** Fade duration for remote images so they appear softly instead of popping in. */
private const val ImageCrossfadeMs = 200

/** Category glyph size as a fraction of the image box's shorter side. */
private const val FallbackGlyphRatio = 0.4f

/** Opacity of the fallback glyph under a missing or broken photo. */
private const val FallbackGlyphAlpha = 0.35f

/**
 * Product image with a branded fallback. A beige [MaterialTheme.colorScheme.surfaceVariant]
 * panel shows while the remote image loads and stays if it never does, so cards
 * never collapse into blank boxes; the image then crossfades in over it.
 *
 * When [fallbackIconKey] is set (a product's `categories.icon_key`) the panel
 * shows that category's outlined glyph, muted and centered. Product photos ship
 * on a uniform beige background, so [contentScale] defaults to [ContentScale.Fit];
 * banners and category artwork pass [ContentScale.Crop].
 *
 * @param contentDescription accessible label, e.g. the product name.
 * @param fallbackIconKey category glyph for the fallback panel; blank falls back
 * to the generic image glyph (banners and categories have no category key).
 */
@Composable
fun ProductImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    fallbackIconKey: String = ""
) {
    // The request is keyed on the image, so a cell only builds it once instead of
    // on every recomposition while scrolling a grid.
    val context = LocalContext.current
    val request = remember(model) {
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(ImageCrossfadeMs)
            // Coil keeps both caches on by default; spelled out so product images are
            // reused across Home/List/Search/Cart instead of being refetched.
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    BoxWithConstraints(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (fallbackIconKey.isBlank()) {
            Icon(
                painter = painterResource(R.drawable.ic_image_placeholder),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = FallbackGlyphAlpha),
                modifier = Modifier.size(Dimens.ImagePlaceholderIcon)
            )
        } else {
            Icon(
                painter = painterResource(categoryIconRes(fallbackIconKey)),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = FallbackGlyphAlpha),
                modifier = Modifier.size(minOf(maxWidth, maxHeight) * FallbackGlyphRatio)
            )
        }
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        )
    }
}
