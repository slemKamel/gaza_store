package com.slem.gazastor.ui.components

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable

/**
 * Adds a cell that spans every column of a [LazyGridScope].
 *
 * Used for full-width sections such as headers, banners and state messages that
 * live inside an otherwise two-column product grid.
 */
fun LazyGridScope.fullWidthItem(
    key: Any? = null,
    content: @Composable () -> Unit
) {
    item(
        key = key,
        span = { GridItemSpan(maxLineSpan) }
    ) {
        content()
    }
}
