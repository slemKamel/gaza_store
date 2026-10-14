package com.slem.gazastor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.slem.gazastor.R

/**
 * IBM Plex Sans Arabic (OFL). Chosen for a formal, premium, understated look.
 * Bundled in res/font so it works offline.
 */
val ArabicFontFamily = FontFamily(
    Font(R.font.ibm_plex_sans_arabic_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.Bold)
)

/**
 * Material 3 type scale. Arabic glyphs run taller than Latin, so line heights
 * are a little more generous than the defaults and letter spacing is removed.
 */
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Bold, fontSize = 52.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 56.sp),
    displaySmall = TextStyle(fontFamily = ArabicFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 48.sp),
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
