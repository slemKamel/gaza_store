package com.slem.gazastor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.slem.gazastor.R

/**
 * Font roles. Every family is bundled in `res/font` (OFL, no network) and the
 * app is Arabic-first, so each role is applied explicitly per script — Compose
 * has no `:lang(ar)` selector and does not guarantee glyph fallback across a
 * [FontFamily] list.
 *
 * - [ArabicFontFamily] — IBM Plex Sans Arabic: every Arabic UI string (body,
 *   labels, buttons, tab bar, section headings).
 * - [ArabicBrandFontFamily] — Amiri: the Arabic wordmark and large decorative
 *   Arabic headlines (the `display*` styles).
 * - [LatinBrandFontFamily] — Bodoni Moda: the Latin wordmark lockup. Latin only,
 *   so letter spacing is safe to use on it.
 * - [LatinFontFamily] — Inter: Latin text and figures (prices, totals, counts).
 *
 * Letter spacing stays at the Material default for Arabic; it is only widened on
 * the Latin lockup.
 */
val ArabicFontFamily = FontFamily(
    Font(R.font.ibm_plex_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.Bold)
)

val ArabicBrandFontFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold)
)

val LatinBrandFontFamily = FontFamily(
    Font(R.font.bodoni_moda_regular, FontWeight.Normal),
    Font(R.font.bodoni_moda_semibold, FontWeight.SemiBold)
)

val LatinFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

/**
 * Material 3 type scale. Arabic glyphs run taller than Latin, so line heights
 * are a little more generous than the defaults and letter spacing is removed.
 * The `display*` tier is the decorative one, so it uses Amiri.
 */
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = ArabicBrandFontFamily, fontWeight = FontWeight.Bold, fontSize = 52.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = ArabicBrandFontFamily, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 56.sp),
    displaySmall = TextStyle(fontFamily = ArabicBrandFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 48.sp),
    headlineLarge = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 44.sp),
    headlineMedium = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 40.sp),
    headlineSmall = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 26.sp),
    titleSmall = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),
    labelSmall = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
)
