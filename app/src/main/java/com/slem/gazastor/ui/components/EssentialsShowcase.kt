package com.slem.gazastor.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.slem.gazastor.R
import kotlinx.coroutines.launch

/**
 * The onboarding "everything in one place" showpiece, transcribed from the
 * Stitch export (`ANIMATION_37`).
 *
 * A luxury shopping bag rests inside astrolabe rings while four category badges
 * orbit it, then descend onto a glowing shelf line as a horizontal row, settle,
 * rise back and repeat. Every coordinate below is written in the export's own
 * 400x400 viewport, and the whole scene is drawn through a single scale
 * transform so the numbers can be read against the source SVG line for line.
 *
 * Faithfulness notes:
 * - CSS applies `animation-timing-function` to *each* keyframe segment, so
 *   [keyframesValue] eases the local segment rather than the whole timeline.
 *   `cubic-bezier(0.42, 0, 0.58, 1)` is CSS `ease-in-out`, which is **not** the
 *   same curve as Compose's `FastOutSlowInEasing`.
 * - The export's `.row-shelf-guide` uses `transform-origin: center`; CSS defaults
 *   SVG transforms to the view-box reference box, so both `scaleX` there and the
 *   sparkle pulse pivot on `(200, 200)`.
 * - Group opacity on the rings is baked into each stroke's alpha. The only
 *   overlap is a 0.75-unit tick crossing a 0.75-unit ring, so the difference is
 *   sub-pixel.
 * - Labels are drawn as real text at a legible size instead of the export's
 *   6.5-unit lettering, and the shelf line is pushed down to clear them.
 */
@Composable
fun EssentialsShowcase(
    visible: Boolean,
    modifier: Modifier = Modifier,
    progressOverride: Float? = null
) {
    val palette = showcasePalette()
    val art = remember { ShowcaseArt() }
    val measurer = rememberTextMeasurer()

    val labels = ShowcaseMark.entries.map { stringResource(it.labelRes) }
    val labelStyle = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp)
    val measuredLabels = remember(measurer, labels, labelStyle) {
        labels.map { measurer.measure(AnnotatedString(it), labelStyle) }
    }

    val orbit = remember { Animatable(0f) }
    val breathe = remember { Animatable(0f) }
    val sparkle = remember { Animatable(0f) }

    // Nothing animates while the page is off-screen, and entering restarts the
    // loop at 0% so the orbit -> row payoff is always seen from the beginning.
    LaunchedEffect(visible, progressOverride) {
        if (progressOverride != null) {
            orbit.snapTo(progressOverride)
            breathe.snapTo(progressOverride)
            sparkle.snapTo(progressOverride)
            return@LaunchedEffect
        }
        if (!visible) {
            orbit.snapTo(0f)
            breathe.snapTo(0f)
            sparkle.snapTo(0f)
            return@LaunchedEffect
        }
        launch { while (true) loop(orbit, ORBIT_MILLIS) }
        launch { while (true) loop(breathe, BREATHE_MILLIS) }
        launch { while (true) loop(sparkle, SPARKLE_MILLIS) }
    }

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val unit = size.minDimension / VIEWPORT
        if (unit <= 0f) return@Canvas

        val orbitProgress = orbit.value
        val bagLift = keyframesValue(breathe.value, PULSE_TIMES, BAG_LIFT, ShowcaseEasing)
        val sparkleAlpha = keyframesValue(sparkle.value, PULSE_TIMES, SPARKLE_ALPHA, ShowcaseEasing)
        val sparkleScale = keyframesValue(sparkle.value, PULSE_TIMES, SPARKLE_SCALE, ShowcaseEasing)
        val shelfAlpha = keyframesValue(orbitProgress, SHELF_TIMES, SHELF_ALPHA, ShowcaseEasing)
        val shelfScale = keyframesValue(orbitProgress, SHELF_TIMES, SHELF_SCALE, ShowcaseEasing)

        withTransform({ scale(unit, unit, pivot = Offset.Zero) }) {
            drawBackdrop(palette)

            // Drawn while the row is formed, so it shares the orbit clock.
            withTransform({ scale(shelfScale, 1f, pivot = Offset(200f, 200f)) }) {
                drawShelf(palette, shelfAlpha)
            }

            // The export bakes `translate(0, 5)` onto the bag group.
            withTransform({ translate(0f, 5f + bagLift) }) {
                drawBag(art, palette)
            }

            ShowcaseMark.entries.forEach { mark ->
                val position = mark.orbit.at(orbitProgress)
                withTransform({ translate(position.x, position.y) }) {
                    drawBadge(art, palette, mark)
                }
            }

            // Sparkles close the scene, so they sit on top of the badges.
            withTransform({ scale(sparkleScale, sparkleScale, pivot = Offset(200f, 200f)) }) {
                drawSparkles(art, palette, sparkleAlpha)
            }
        }

        // Labels sit outside the unit transform so the text is measured and
        // rendered at real sp sizes rather than scaled up from 6.5 units.
        ShowcaseMark.entries.forEachIndexed { index, mark ->
            val layout = measuredLabels[index]
            val position = mark.orbit.at(orbitProgress)
            drawText(
                textLayoutResult = layout,
                color = palette.copper,
                topLeft = Offset(
                    x = position.x * unit - layout.size.width / 2f,
                    y = (position.y + LABEL_OFFSET_Y) * unit
                )
            )
        }
    }
}

// --- Clock ------------------------------------------------------------------

private const val ORBIT_MILLIS = 9_500
private const val BREATHE_MILLIS = 6_000
private const val SPARKLE_MILLIS = 4_000

/** Linear clock; the easing belongs to the keyframe tracks, not the tween. */
private suspend fun loop(clock: Animatable<Float, *>, durationMillis: Int) {
    clock.snapTo(0f)
    clock.animateTo(1f, tween(durationMillis = durationMillis, easing = LinearEasing))
}

// --- Keyframes --------------------------------------------------------------

/** SVG viewport of the Stitch export. Every coordinate here is in these units. */
private const val VIEWPORT = 400f

/** CSS `ease-in-out` — `cubic-bezier(0.42, 0, 0.58, 1)`. */
private val ShowcaseEasing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

private val ORBIT_TIMES = floatArrayOf(0f, 0.14f, 0.28f, 0.40f, 0.50f, 0.60f, 0.71f, 0.82f, 0.91f, 1f)
private val SHELF_TIMES = floatArrayOf(0f, 0.45f, 0.60f, 0.82f, 0.92f, 1f)
private val PULSE_TIMES = floatArrayOf(0f, 0.50f, 1f)

private val SHELF_ALPHA = floatArrayOf(0f, 0f, 0.65f, 0.65f, 0f, 0f)
private val SHELF_SCALE = floatArrayOf(0.7f, 0.7f, 1f, 1f, 0.7f, 0.7f)

private val SPARKLE_ALPHA = floatArrayOf(0.7f, 1f, 0.7f)
private val SPARKLE_SCALE = floatArrayOf(1f, 1.06f, 1f)
private val BAG_LIFT = floatArrayOf(0f, -3f, 0f)

/** Baseline shelf line. Pushed from the export's 378 to clear the Arabic labels. */
private const val SHELF_Y = 394f

/** Label top edge, relative to a badge centre. The badge radius is 25. */
private const val LABEL_OFFSET_Y = 27f

/** Badge radius, in export units. */
private const val BADGE_RADIUS = 25f

/**
 * Samples a CSS keyframe track at [progress] (0..1).
 *
 * A CSS `animation-timing-function` applies to *each* keyframe segment rather
 * than once across the whole timeline, so [easing] is applied to the local
 * segment progress. Getting this wrong is what makes a Compose port feel subtly
 * out of step with the original.
 */
internal fun keyframesValue(
    progress: Float,
    times: FloatArray,
    values: FloatArray,
    easing: Easing
): Float {
    require(times.size == values.size) { "times and values must line up" }
    if (values.isEmpty()) return 0f

    val at = progress.coerceIn(0f, 1f)
    if (at <= times.first()) return values.first()
    if (at >= times.last()) return values.last()

    var index = 0
    while (index < times.size - 2 && at >= times[index + 1]) index++

    val start = times[index]
    val span = times[index + 1] - start
    val local = if (span <= 0f) 1f else (at - start) / span
    return values[index] + (values[index + 1] - values[index]) * easing.transform(local)
}

/** A badge's orbit: X and Y share the timings but not the values. */
private class OrbitPath(xs: FloatArray, ys: FloatArray) {
    private val xs = xs.copyOf()
    private val ys = ys.copyOf()

    fun at(progress: Float): Offset = Offset(
        x = keyframesValue(progress, ORBIT_TIMES, xs, ShowcaseEasing),
        y = keyframesValue(progress, ORBIT_TIMES, ys, ShowcaseEasing)
    )
}

private enum class ShowcaseMark(
    @param:StringRes val labelRes: Int,
    private val xs: FloatArray,
    private val ys: FloatArray
) {
    /** Slot 1 (far left): أزياء */
    Dress(
        R.string.onboarding_showcase_fashion,
        floatArrayOf(75f, 185f, 325f, 315f, 180f, 55f, 55f, 55f, 50f, 75f),
        floatArrayOf(120f, 92f, 125f, 205f, 265f, 340f, 337f, 340f, 220f, 120f)
    ),

    /** Slot 2: منزل */
    Lamp(
        R.string.onboarding_showcase_home,
        floatArrayOf(325f, 330f, 195f, 75f, 95f, 150f, 150f, 150f, 220f, 325f),
        floatArrayOf(120f, 205f, 92f, 125f, 255f, 340f, 337f, 340f, 220f, 120f)
    ),

    /** Slot 3: جمال */
    Perfume(
        R.string.onboarding_showcase_beauty,
        floatArrayOf(65f, 80f, 210f, 325f, 280f, 248f, 248f, 248f, 150f, 65f),
        floatArrayOf(210f, 125f, 92f, 145f, 260f, 340f, 337f, 340f, 270f, 210f)
    ),

    /** Slot 4 (far right): تقنية */
    Audio(
        R.string.onboarding_showcase_tech,
        floatArrayOf(330f, 265f, 95f, 65f, 190f, 342f, 342f, 342f, 340f, 330f),
        floatArrayOf(215f, 110f, 125f, 205f, 275f, 340f, 337f, 340f, 265f, 215f)
    );

    val orbit = OrbitPath(xs, ys)
}

// --- Palette ----------------------------------------------------------------

private class ShowcasePalette(
    val forest: Color,
    val copper: Color,
    val white: Color,
    val cream: Color,
    val variant: Color,
    val shade: Color,
    val aura: Brush,
    val trim: Brush,
    val handle: Brush
)

private val COPPER_STOPS = listOf(
    Color(0xFFC5936E),
    Color(0xFFA9744F),
    Color(0xFF8E5E3B)
)

/**
 * Brand palette pulled from the Material theme. The export's off-whites map onto
 * the existing beige ramp rather than being hardcoded here: `#FFFFFF` -> the pure
 * white container, `#FAF8F5`/`#FBF9F6` -> surface, `#EDE7DD` -> surfaceVariant,
 * `#E4DCD0` -> the hairline line.
 */
@Composable
private fun showcasePalette(): ShowcasePalette {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) {
        val copper = scheme.secondary
        ShowcasePalette(
            forest = scheme.primary,
            copper = copper,
            white = scheme.surfaceContainerLowest,
            cream = scheme.surface,
            variant = scheme.surfaceVariant,
            shade = scheme.outlineVariant,
            aura = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to copper.copy(alpha = 0.16f),
                    0.6f to copper.copy(alpha = 0.05f),
                    1f to copper.copy(alpha = 0f)
                ),
                center = Offset(200f, 201.4f),
                radius = 153.6f
            ),
            // `subtleCopper` is authored in objectBoundingBox units, so each
            // element resolves it against its own box.
            trim = Brush.linearGradient(
                colors = COPPER_STOPS,
                start = Offset(134f, 190f),
                end = Offset(266f, 190f)
            ),
            handle = Brush.linearGradient(
                colors = COPPER_STOPS,
                start = Offset(166f, 128f),
                end = Offset(234f, 190f)
            )
        )
    }
}

// --- Paths ------------------------------------------------------------------

private fun path(data: String): Path = PathParser().parsePathString(data).toPath()

/** Every curve from the export, parsed once and reused across frames. */
private class ShowcaseArt {
    // Bag
    val bagBack = path("M 144,172 L 256,172 L 274,270 L 126,270 Z")
    val bagShade = path("M 152,178 L 248,178 L 258,262 L 142,262 Z")
    val tissue = path("M 160,176 C 168,156 188,154 196,170 C 205,152 228,156 238,174 Z")
    val bagBody =
        path("M 134,190 L 266,190 L 278,278 C 278,282 274,285 270,285 L 130,285 C 126,285 122,282 122,278 Z")
    val monogram = path(
        "M 5,-7 C 3,-10 -1,-11 -5,-11 C -11,-11 -15,-6 -15,0 C -15,6 -10,11 -4,11 C 2,11 7,8 8,4 " +
            "C 9,2 9,0 9,-1 L 0,-1 L 0,1 L 6,1 C 5,4 2,8 -4,8 C -8,8 -12,4 -12,0 C -12,-5 -8,-8 -4,-8 " +
            "C -1,-8 2,-7 4,-5 Z"
    )
    val handleGhost = path("M 172,172 C 172,120 228,120 228,172")
    val handleMain = path("M 166,190 C 166,128 234,128 234,190")
    val handleInner = path("M 170,190 C 170,134 230,134 230,190")

    // Sparkles
    val sparkleTop = path("M 124,106 Q 124,112 118,112 Q 124,112 124,118 Q 124,112 130,112 Q 124,112 124,106 Z")
    val sparkleSide = path("M 264,136 Q 264,141 259,141 Q 264,141 264,146 Q 264,141 269,141 Q 264,141 264,136 Z")

    // Dress
    val dressStrap = path("M 0,-13 C -2,-13 -3,-10 -1,-8 L 0,-6")
    val dressNeck = path("M -5,-5 Q 0,-2 5,-5")
    val dressBody = path("M -6,-5 L -4,0 Q -6,5 -3,7 L -11,14 C -11,15 11,15 11,14 L 3,7 Q 6,5 4,0 L 6,-5")

    // Lamp
    val lampShade = path("M -9,-5 L 9,-5 L 6,-14 L -6,-14 Z")
    val lampBase = path("M -7,10 L 7,10")

    // Headphones
    val headband = path("M -11,4 C -11,-10 11,-10 11,4")
}

// --- Drawing ----------------------------------------------------------------

private fun dashedStroke(width: Float, vararg intervals: Float): Stroke =
    Stroke(width = width, pathEffect = PathEffect.dashPathEffect(intervals))

private fun outline(
    width: Float,
    cap: StrokeCap = StrokeCap.Butt,
    join: StrokeJoin = StrokeJoin.Miter
): Stroke = Stroke(width = width, cap = cap, join = join)

private fun DrawScope.drawBackdrop(palette: ShowcasePalette) {
    drawCircle(brush = palette.aura, radius = 153.6f, center = Offset(200f, 201.4f))

    // `<g opacity="0.45">` — folded into the stroke colours.
    val ring = palette.forest.copy(alpha = 0.45f)
    val tick = palette.copper.copy(alpha = 0.45f)
    drawCircle(
        color = ring,
        radius = 145f,
        center = Offset(200f, 195f),
        style = dashedStroke(0.75f, 3f, 5f)
    )
    drawCircle(color = tick, radius = 112f, center = Offset(200f, 195f), style = Stroke(0.5f))
    drawLine(tick, Offset(200f, 42f), Offset(200f, 60f), strokeWidth = 0.8f)
    drawLine(tick, Offset(45f, 195f), Offset(63f, 195f), strokeWidth = 0.8f)
    drawLine(tick, Offset(337f, 195f), Offset(355f, 195f), strokeWidth = 0.8f)
    drawCircle(color = tick, radius = 2.5f, center = Offset(200f, 51f))
}

private fun DrawScope.drawShelf(palette: ShowcasePalette, alpha: Float) {
    val color = palette.copper.copy(alpha = alpha)
    drawLine(
        color = color,
        start = Offset(35f, SHELF_Y),
        end = Offset(365f, SHELF_Y),
        strokeWidth = 0.75f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 3f))
    )
    drawCircle(color = color, radius = 1.5f, center = Offset(35f, SHELF_Y))
    drawCircle(color = color, radius = 2f, center = Offset(200f, SHELF_Y))
    drawCircle(color = color, radius = 1.5f, center = Offset(365f, SHELF_Y))
}

private fun DrawScope.drawBag(art: ShowcaseArt, palette: ShowcasePalette) {
    // Base shadow.
    drawOval(
        color = palette.forest.copy(alpha = 0.08f),
        topLeft = Offset(128f, 279f),
        size = Size(144f, 14f)
    )

    // Back wall, then the interior shading, then the silk lining.
    drawPath(art.bagBack, palette.variant)
    drawPath(art.bagBack, palette.forest, style = outline(1.2f, join = StrokeJoin.Round))
    drawPath(art.bagShade, palette.shade.copy(alpha = 0.6f))
    drawPath(art.tissue, palette.cream)
    drawPath(art.tissue, palette.copper.copy(alpha = 0.6f), style = Stroke(1f))

    // Front body with its perspective flare.
    drawPath(art.bagBody, palette.white)
    drawPath(art.bagBody, palette.forest, style = outline(1.6f, join = StrokeJoin.Round))

    // Gusset creases.
    val crease = palette.forest.copy(alpha = 0.3f)
    drawLine(
        color = crease,
        start = Offset(152f, 190f),
        end = Offset(144f, 282f),
        strokeWidth = 0.75f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))
    )
    drawLine(
        color = crease,
        start = Offset(248f, 190f),
        end = Offset(256f, 282f),
        strokeWidth = 0.75f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))
    )

    // Burnished copper trim across the opening.
    drawLine(
        brush = palette.trim,
        start = Offset(134f, 190f),
        end = Offset(266f, 190f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    // Monogram, authored around (200, 240).
    withTransform({ translate(200f, 240f) }) {
        drawCircle(
            color = palette.copper.copy(alpha = 0.65f),
            radius = 22f,
            center = Offset.Zero,
            style = dashedStroke(0.7f, 1.5f, 2f)
        )
        drawCircle(color = palette.cream.copy(alpha = 0.9f), radius = 18f, center = Offset.Zero)
        drawCircle(
            color = palette.forest,
            radius = 18f,
            center = Offset.Zero,
            style = Stroke(0.6f)
        )
        drawPath(art.monogram, palette.forest)
        drawCircle(color = palette.copper, radius = 1.5f, center = Offset(0f, -14f))
    }

    // Handles over the front wall, then the rivets.
    drawPath(art.handleGhost, palette.forest.copy(alpha = 0.35f), style = Stroke(1f))
    drawPath(art.handleMain, brush = palette.handle, style = Stroke(2.6f, cap = StrokeCap.Round))
    drawPath(art.handleInner, palette.cream.copy(alpha = 0.6f), style = Stroke(0.8f))

    listOf(166f, 234f).forEach { x ->
        drawCircle(color = palette.forest, radius = 3.2f, center = Offset(x, 195f))
        drawCircle(color = palette.copper, radius = 3.2f, center = Offset(x, 195f), style = Stroke(1.2f))
    }
}

private fun DrawScope.drawSparkles(art: ShowcaseArt, palette: ShowcasePalette, alpha: Float) {
    drawPath(art.sparkleTop, palette.copper.copy(alpha = 0.75f * alpha))
    drawPath(art.sparkleSide, palette.copper.copy(alpha = 0.75f * alpha))
    drawCircle(color = palette.copper.copy(alpha = 0.45f * alpha), radius = 2f, center = Offset(104f, 188f))
    drawCircle(color = palette.copper.copy(alpha = 0.45f * alpha), radius = 1.8f, center = Offset(288f, 172f))
    drawCircle(color = palette.copper.copy(alpha = 0.6f * alpha), radius = 1.5f, center = Offset(190f, 132f))
}

private fun DrawScope.drawBadge(art: ShowcaseArt, palette: ShowcasePalette, mark: ShowcaseMark) {
    // Badge discs. The local origin is the badge centre, so every circle has to
    // pass it explicitly — the DrawScope default would be the canvas centre.
    drawCircle(color = palette.white, radius = BADGE_RADIUS, center = Offset.Zero)
    drawCircle(
        color = palette.forest.copy(alpha = 0.18f),
        radius = BADGE_RADIUS,
        center = Offset.Zero,
        style = Stroke(0.8f)
    )
    drawCircle(color = palette.cream.copy(alpha = 0.5f), radius = 22f, center = Offset.Zero)
    drawCircle(
        color = palette.copper.copy(alpha = 0.5f),
        radius = 22f,
        center = Offset.Zero,
        style = dashedStroke(0.5f, 1.5f, 2f)
    )

    when (mark) {
        ShowcaseMark.Dress -> {
            drawPath(art.dressStrap, palette.copper, style = Stroke(1f, cap = StrokeCap.Round))
            drawPath(art.dressNeck, palette.forest, style = Stroke(1.2f, cap = StrokeCap.Round))
            drawPath(art.dressBody, palette.forest, style = outline(1.3f, join = StrokeJoin.Round))
            drawLine(palette.copper, Offset(-4f, 2f), Offset(4f, 2f), strokeWidth = 1.4f, cap = StrokeCap.Round)
            drawCircle(color = palette.copper, radius = 1.2f, center = Offset(0f, 2f))
        }

        ShowcaseMark.Lamp -> {
            drawPath(art.lampShade, palette.forest, style = outline(1.3f, join = StrokeJoin.Round))
            drawLine(palette.copper, Offset(-9f, -5f), Offset(9f, -5f), strokeWidth = 1.4f)
            drawLine(palette.forest, Offset(0f, -5f), Offset(0f, 10f), strokeWidth = 1.2f)
            drawCircle(color = palette.copper, radius = 1.8f, center = Offset(0f, -1f))
            drawPath(art.lampBase, palette.forest, style = Stroke(1.8f, cap = StrokeCap.Round))
        }

        ShowcaseMark.Perfume -> {
            // Stopper: fill then hairline stroke.
            drawRoundRect(
                color = palette.copper,
                topLeft = Offset(-4f, -13f),
                size = Size(8f, 5.5f),
                cornerRadius = CornerRadius(1f, 1f)
            )
            drawRoundRect(
                color = palette.copper,
                topLeft = Offset(-4f, -13f),
                size = Size(8f, 5.5f),
                cornerRadius = CornerRadius(1f, 1f),
                style = Stroke(0.5f)
            )
            drawRect(color = palette.forest, topLeft = Offset(-1.5f, -7.5f), size = Size(3f, 2f))
            drawRoundRect(
                color = palette.forest,
                topLeft = Offset(-9f, -5.5f),
                size = Size(18f, 19f),
                cornerRadius = CornerRadius(2.5f, 2.5f),
                style = Stroke(1.3f)
            )
            drawRoundRect(
                color = palette.cream,
                topLeft = Offset(-5.5f, -1.5f),
                size = Size(11f, 10f),
                cornerRadius = CornerRadius(1f, 1f)
            )
            drawRoundRect(
                color = palette.copper,
                topLeft = Offset(-5.5f, -1.5f),
                size = Size(11f, 10f),
                cornerRadius = CornerRadius(1f, 1f),
                style = dashedStroke(0.7f, 1f, 1f)
            )
            drawCircle(color = palette.copper, radius = 1.2f, center = Offset(0f, 3.5f))
        }

        ShowcaseMark.Audio -> {
            drawPath(art.headband, palette.forest, style = Stroke(1.4f, cap = StrokeCap.Round))
            drawCircle(color = palette.copper, radius = 1.8f, center = Offset(-11f, 4f))
            drawCircle(color = palette.copper, radius = 1.8f, center = Offset(11f, 4f))
            listOf(-14f, 9f).forEach { x ->
                drawRoundRect(
                    color = palette.white,
                    topLeft = Offset(x, 2f),
                    size = Size(5f, 12f),
                    cornerRadius = CornerRadius(2.5f, 2.5f)
                )
                drawRoundRect(
                    color = palette.forest,
                    topLeft = Offset(x, 2f),
                    size = Size(5f, 12f),
                    cornerRadius = CornerRadius(2.5f, 2.5f),
                    style = Stroke(1.2f)
                )
            }
            drawLine(palette.copper, Offset(-11.5f, 5f), Offset(-11.5f, 11f), strokeWidth = 1f)
            drawLine(palette.copper, Offset(11.5f, 5f), Offset(11.5f, 11f), strokeWidth = 1f)
        }
    }
}
