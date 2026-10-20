package com.slem.gazastor.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * The Gaza Store brand mark as a Compose [ImageVector]: a copper gradient disc
 * holding a cream rounded square with a shopping-bag glyph.
 *
 * It is defined in Kotlin so the splash renders in Android Studio's Compose
 * preview without depending on a generated `R.drawable` field; the adaptive
 * launcher icon uses its own `res/drawable/ic_launcher_foreground.xml`.
 */
val BrandMark: ImageVector = ImageVector.Builder(
    name = "BrandMark",
    defaultWidth = 108.dp,
    defaultHeight = 108.dp,
    viewportWidth = 500f,
    viewportHeight = 500f
).apply {
    // Copper disc.
    addPath(
        pathData = pathNodes("M35,250 A215,215 0 1 1 465,250 A215,215 0 1 1 35,250 Z"),
        fill = copperGradient(Offset(35f, 35f), Offset(465f, 465f))
    )
    // Cream rounded square.
    addPath(
        pathData = pathNodes(
            "M199,135 L301,135 A64,64 0 0 1 365,199 L365,301 A64,64 0 0 1 301,365 " +
                "L199,365 A64,64 0 0 1 135,301 L135,199 A64,64 0 0 1 199,135 Z"
        ),
        fill = Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFF7F2E8)),
            start = Offset(135f, 135f),
            end = Offset(365f, 365f)
        )
    )
    // Bag handle.
    addPath(
        pathData = pathNodes(
            "M223,244 C223,214 277,214 277,244 C277,250 273,254 267,254 C261,254 259,250 " +
                "259,244 C259,228 241,228 241,244 C241,250 239,254 233,254 C227,254 223,250 223,244 Z"
        ),
        fill = SolidColor(Color(0xFFB07146))
    )
    // Bag body.
    addPath(
        pathData = pathNodes(
            "M206,244 L294,244 C297,244 299,247 298,250 L293,298 C292,306 285,312 277,312 " +
                "L223,312 C215,312 208,306 207,298 L202,250 C201,247 203,244 206,244 Z"
        ),
        fill = copperGradient(Offset(202f, 244f), Offset(299f, 312f))
    )
    // Handle eyelets.
    addPath(
        pathData = pathNodes("M224,259 a5,5 0 1,0 10,0 a5,5 0 1,0 -10,0"),
        fill = SolidColor(Color(0xFFF7F2E8))
    )
    addPath(
        pathData = pathNodes("M266,259 a5,5 0 1,0 10,0 a5,5 0 1,0 -10,0"),
        fill = SolidColor(Color(0xFFF7F2E8))
    )
}.build()

/**
 * Lighter sibling of [BrandMark] for small placements such as the compact
 * brand bar: a thin ring around the same bag glyph, in one flat color. It
 * carries no meaningful color of its own — tint it at the call site (e.g. with
 * `MaterialTheme.colorScheme.secondary`) so the copper comes from the theme.
 */
val BrandMarkOutlined: ImageVector = ImageVector.Builder(
    name = "BrandMarkOutlined",
    defaultWidth = 108.dp,
    defaultHeight = 108.dp,
    viewportWidth = 500f,
    viewportHeight = 500f
).apply {
    // Thin outer ring (stroke only — keeps the mark lightweight at small sizes).
    addPath(
        pathData = pathNodes("M25,250 A225,225 0 1 1 475,250 A225,225 0 1 1 25,250 Z"),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 26f,
        strokeLineCap = StrokeCap.Round
    )
    // The bag glyph is drawn at ~19% of the canvas in the full logo; inside the
    // ring it must fill the interior instead, or it reads as a dot at bar size.
    addGroup(scaleX = 2.05f, scaleY = 2.05f, pivotX = 250.5f, pivotY = 263f)
    // Bag handle.
    addPath(
        pathData = pathNodes(
            "M223,244 C223,214 277,214 277,244 C277,250 273,254 267,254 C261,254 259,250 " +
                "259,244 C259,228 241,228 241,244 C241,250 239,254 233,254 C227,254 223,250 223,244 Z"
        ),
        fill = SolidColor(Color.Black)
    )
    // Bag body.
    addPath(
        pathData = pathNodes(
            "M206,244 L294,244 C297,244 299,247 298,250 L293,298 C292,306 285,312 277,312 " +
                "L223,312 C215,312 208,306 207,298 L202,250 C201,247 203,244 206,244 Z"
        ),
        fill = SolidColor(Color.Black)
    )
    clearGroup()
}.build()

/** Copper gradient shared by the disc and the bag body. */
private fun copperGradient(start: Offset, end: Offset): Brush = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0xFFC28459),
        0.5f to Color(0xFFB07146),
        1f to Color(0xFF9C5E35)
    ),
    start = start,
    end = end
)

private fun pathNodes(pathData: String) = PathParser().parsePathString(pathData).toNodes()
