package com.slem.gazastor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slem.gazastor.R
import com.slem.gazastor.ui.theme.Dimens
import com.slem.gazastor.ui.theme.LatinBrandFontFamily

/**
 * The bilingual brand lockup: "متجر غزة" in Amiri over a letter-spaced
 * "GAZA STORE" in Bodoni Moda.
 *
 * Both scripts are set explicitly because Compose offers no `:lang(ar)` switch
 * and glyph fallback across a [androidx.compose.ui.text.font.FontFamily] list is
 * not guaranteed. Letter spacing is only applied to the Latin line — it breaks
 * Arabic letter joining. [lineGap] defaults to the shared [Dimens.BrandLineGap];
 * the compact Home bar passes a tighter gap so its two lines read as one unit.
 */
@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    wordmarkStyle: TextStyle = MaterialTheme.typography.displaySmall,
    latinStyle: TextStyle = MaterialTheme.typography.labelLarge,
    wordmarkColor: Color = MaterialTheme.colorScheme.primary,
    latinColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    lineGap: Dp = Dimens.BrandLineGap
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.brand_wordmark),
            style = wordmarkStyle,
            color = wordmarkColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lineGap))

        Text(
            text = stringResource(R.string.brand_wordmark_latin),
            style = latinStyle.copy(fontFamily = LatinBrandFontFamily, letterSpacing = 4.sp),
            color = latinColor,
            textAlign = TextAlign.Center
        )
    }
}
