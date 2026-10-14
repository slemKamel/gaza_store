package com.slem.gazastor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * "Quiet Luxury" light-only scheme: soft beige surfaces, deep forest green
 * structure, muted copper accent. Dynamic color is intentionally disabled so
 * the brand stays consistent across devices.
 */
private val GazaStorColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenDark,
    secondary = Copper,
    onSecondary = Color.White,
    secondaryContainer = CopperContainer,
    onSecondaryContainer = CopperOnContainer,
    tertiary = Copper,
    onTertiary = Color.White,
    background = BeigeBackground,
    onBackground = InkBlack,
    surface = BeigeSurface,
    onSurface = InkBlack,
    surfaceVariant = BeigeVariant,
    onSurfaceVariant = MutedText,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = BeigeSurface,
    surfaceContainer = BeigeBackground,
    surfaceContainerHigh = BeigeVariant,
    surfaceContainerHighest = BeigeVariant,
    outline = Hairline,
    outlineVariant = Hairline,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFF6DFDB),
    onErrorContainer = Color(0xFF3F0F0A)
)

private val GazaStorShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun GazaStorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GazaStorColorScheme,
        typography = Typography,
        shapes = GazaStorShapes,
        content = content
    )
}
