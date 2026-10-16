package com.slem.gazastor.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.slem.gazastor.R
import com.slem.gazastor.ui.theme.ArabicFontFamily
import com.slem.gazastor.ui.theme.LatinFontFamily
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * The onboarding "quick orders" showpiece, transcribed from the Stitch export
 * (`ANIMATION_62`, `image/code.html`).
 *
 * A pristine "Gaza Store — Curated Box" card floats on the left; a pointer taps
 * its copper + button and releases a ripple; four category badges (أزياء، منزل،
 * جمال، تقنية) launch along dashed golden arcs and plunge into the luxury
 * shopping bag, which cushions the impact while a copper +4 badge overshoots
 * from its corner. An emerald status pill then rises with
 * «اكتملت الإضافة • جاهز للطلب» and everything dissolves back to rest for the
 * next loop.
 *
 * Every coordinate below is written in the export's own 420x380 viewport, and
 * the scene is drawn through a single scale transform (one [unitLayer] per
 * z-tier) so the numbers can be read against the source SVG line for line.
 *
 * Faithfulness notes:
 * - The 10s timeline is the export's CSS keyframe table; [keyframesValue]
 *   eases each segment locally because CSS applies `animation-timing-function`
 *   to *every* segment, not once across the animation (same trap and same
 *   helper as [EssentialsShowcase]).
 * - The card breathes on its own 6s clock and the sparkles pulse on a 3.5s
 *   clock, exactly as the export runs them on separate animations.
 * - `.sparkle` has no explicit `transform`, so it picks up the CSS default
 *   transform-origin: the view-box centre — pivot (210, 190) here, mirroring
 *   the showcase's reading of the same default.
 * - The export's `feDropShadow` filters are dropped: quiet-luxury surfaces are
 *   flat, so the card keeps only its stroke-opacity 0.12 hairline and the bag
 *   keeps its flat contact ellipse.
 * - The HTML page's black background is not part of the SVG; the `luxAura`
 *   gradient already fades to transparent, so the beige page shows through.
 * - The export's tiny in-badge label pills (5.5-unit lettering) are replaced by
 *   real-sp Arabic text below each badge, drawn unclipped and rotated with its
 *   badge — the same treatment [EssentialsShowcase] gives the same four
 *   categories. The GAZA STORE tag, the «صندوق مختار» caption (the export's
 *   empty "Card Description" slot), the pill copy and the +4 digit are likewise
 *   measured at real sp and drawn in a pixel pass anchored to their export
 *   positions, between the z-tiers they belong to.
 * - The cursor and ripple anchor at (138, 196), 8 units above the card's +
 *   centre (138, 204) because the card breathes independently; coordinates are
 *   kept as exported rather than "fixed".
 */
@Composable
fun QuickOrdersShowcase(
    visible: Boolean,
    modifier: Modifier = Modifier,
    progressOverride: Float? = null
) {
    val palette = ordersPalette()
    val art = remember { OrdersArt() }
    val measurer = rememberTextMeasurer()

    val markLabels = OrdersMark.entries.map { stringResource(it.labelRes) }
    val tagText = stringResource(R.string.brand_wordmark_latin)
    val boxText = stringResource(R.string.onboarding_orders_box)
    val pillText = stringResource(R.string.onboarding_orders_pill)

    // Labels are rendered at real sp sizes instead of the export's unit-sized
    // lettering, so each style is sized to the shape it sits in or under.
    val markStyle = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp)
    val tagStyle = TextStyle(
        fontFamily = LatinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 6.5.sp,
        letterSpacing = 0.4.sp
    )
    val boxStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    )
    val pillStyle = TextStyle(
        fontFamily = ArabicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp
    )
    val countStyle = TextStyle(
        fontFamily = LatinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp
    )

    val markLayouts = remember(measurer, markLabels, markStyle) {
        markLabels.map { measurer.measure(AnnotatedString(it), markStyle) }
    }
    val tagLayout = remember(measurer, tagText, tagStyle) {
        measurer.measure(AnnotatedString(tagText), tagStyle)
    }
    val boxLayout = remember(measurer, boxText, boxStyle) {
        measurer.measure(AnnotatedString(boxText), boxStyle)
    }
    val pillLayout = remember(measurer, pillText, pillStyle) {
        measurer.measure(AnnotatedString(pillText), pillStyle)
    }
    val countLayout = remember(measurer, countStyle) {
        measurer.measure(AnnotatedString("4"), countStyle)
    }

    // Three linear clocks: the 10s master timeline, the card's 6s breath and
    // the 3.5s sparkle shimmer. Easing belongs to the keyframe tracks.
    val timeline = remember { Animatable(0f) }
    val breath = remember { Animatable(0f) }
    val shimmer = remember { Animatable(0f) }

    LaunchedEffect(visible, progressOverride) {
        if (progressOverride != null) {
            timeline.snapTo(progressOverride)
            breath.snapTo(progressOverride)
            shimmer.snapTo(progressOverride)
            return@LaunchedEffect
        }
        // Nothing animates while the page is off-screen, and entering restarts
        // the loop at 0% so the tap -> flight -> bag payoff is always seen from
        // the beginning.
        if (!visible) {
            timeline.snapTo(0f)
            breath.snapTo(0f)
            shimmer.snapTo(0f)
            return@LaunchedEffect
        }
        launch { while (true) loop(timeline, TIMELINE_MILLIS) }
        launch { while (true) loop(breath, BREATH_MILLIS) }
        launch { while (true) loop(shimmer, SHIMMER_MILLIS) }
    }

    Canvas(modifier = modifier.aspectRatio(VIEWPORT_W / VIEWPORT_H)) {
        val unit = minOf(size.width / VIEWPORT_W, size.height / VIEWPORT_H)
        if (unit <= 0f) return@Canvas

        val progress = timeline.value
        val breathDy = cardFloatDy(breath.value)
        val spark = sparkleFrame(shimmer.value)
        val tap = tapFrame(progress)
        val ripple = rippleFrame(progress)
        val arc = arcFrame(progress)
        val marks = OrdersMark.entries.map { it.at(progress) }
        val cushion = bagFrame(progress)
        val pop = popFrame(progress)
        val pill = pillFrame(progress)

        // 1 — Ambient stage: warm aura, astrolabe rings, trajectory arcs and
        // the sparkles dotted along the flight path.
        unitLayer(unit) {
            drawAura(palette)
            drawRings(palette)
            drawTrajectory(palette, art, arc.alpha, arc.phase)
            withTransform({ scale(spark.scale, spark.scale, pivot = SPARKLE_PIVOT) }) {
                drawPathSparkles(palette, spark.alpha)
            }
        }

        // 2 — The floating showcase card (its own breath clock).
        unitLayer(unit) {
            withTransform({ translate(CARD_X, CARD_Y + breathDy) }) {
                drawCard(palette, art)
            }
        }

        // Card copy, drawn over the card shapes but under the badges that
        // emerge from the crest — matching the export's document order.
        drawCentered(tagLayout, Offset(TAG_CENTER_X * unit, (TAG_CENTER_Y + breathDy) * unit), palette.copper)
        drawCentered(boxLayout, Offset(BOX_CENTER_X * unit, (BOX_CENTER_Y + breathDy) * unit), palette.forest)

        // 3 — Haptic tap: copper shockwave ripple + pointer.
        unitLayer(unit) {
            if (ripple.alpha > 0f) {
                drawCircle(
                    color = palette.copper.copy(alpha = ripple.alpha),
                    radius = RIPPLE_RADIUS * ripple.scale,
                    center = RIPPLE_CENTER,
                    style = Stroke(RIPPLE_STROKE * ripple.scale)
                )
            }
            withTransform({
                translate(tap.x, tap.y)
                scale(tap.scale, tap.scale, pivot = Offset.Zero)
            }) {
                drawPointer(palette, tap.alpha)
            }
        }

        // 4 — The four category badges in flight (staggered parabolic arcs).
        unitLayer(unit) {
            OrdersMark.entries.forEachIndexed { index, mark ->
                val frame = marks[index]
                if (frame.alpha <= 0f) return@forEachIndexed
                withTransform({
                    translate(frame.x, frame.y)
                    scale(frame.scale, frame.scale, pivot = Offset.Zero)
                }) {
                    rotate(frame.rotation, pivot = Offset.Zero) {
                        drawMark(palette, art, mark)
                    }
                }
            }
        }

        // Their labels ride along at real sp, rotated with the badge.
        drawMarkLabels(markLayouts, marks, unit, palette.copper)

        // 5 — The luxury bag absorbs the impact (cushion clock).
        unitLayer(unit) {
            withTransform({
                translate(BAG_X, BAG_Y)
                scale(cushion.scale, cushion.scale, pivot = Offset.Zero)
                translate(0f, cushion.dy)
            }) {
                drawBag(palette, art)
            }
        }

        // 6 — The +4 cart badge overshoots from the bag's top-right corner.
        unitLayer(unit) {
            if (pop.alpha > 0f && pop.scale > 0f) {
                withTransform({
                    translate(BADGE_X, BADGE_Y)
                    scale(pop.scale, pop.scale, pivot = Offset.Zero)
                }) {
                    drawCircle(
                        brush = Brush.linearGradient(
                            COPPER_STOPS.map { it.copy(alpha = pop.alpha) },
                            start = Offset(-12f, -12f),
                            end = Offset(12f, 12f)
                        ),
                        radius = 12f,
                        center = Offset.Zero
                    )
                    drawCircle(
                        color = palette.white.copy(alpha = pop.alpha),
                        radius = 12f,
                        center = Offset.Zero,
                        style = Stroke(1.2f)
                    )
                }
            }
        }
        if (pop.alpha > 0f) {
            drawCentered(
                countLayout,
                Offset(BADGE_X * unit, (BADGE_Y + 0.5f) * unit),
                palette.white,
                pop.alpha
            )
        }

        // 7 — The emerald status pill rises, holds, then dissolves.
        unitLayer(unit) {
            if (pill.alpha > 0f) {
                withTransform({
                    translate(PILL_X, PILL_Y + pill.dy)
                    scale(pill.scale, pill.scale, pivot = Offset.Zero)
                }) {
                    drawStatusPill(palette, art, pill.alpha)
                }
            }
        }
        if (pill.alpha > 0f) {
            drawCentered(
                pillLayout,
                Offset(PILL_X * unit, (PILL_Y + pill.dy) * unit),
                palette.cream,
                pill.alpha
            )
        }

        // Corner sparkles close the scene, so they sit on top of everything.
        unitLayer(unit) {
            withTransform({ scale(spark.scale, spark.scale, pivot = SPARKLE_PIVOT) }) {
                drawCornerSparkles(palette, art, spark.alpha)
            }
        }
    }
}

// --- Clock ------------------------------------------------------------------

private const val TIMELINE_MILLIS = 10_000
private const val BREATH_MILLIS = 6_000
private const val SHIMMER_MILLIS = 3_500

/** Linear clock; the easing belongs to the keyframe tracks, not the tween. */
private suspend fun loop(clock: Animatable<Float, *>, durationMillis: Int) {
    clock.snapTo(0f)
    clock.animateTo(1f, tween(durationMillis = durationMillis, easing = LinearEasing))
}

// --- Viewport & easings -----------------------------------------------------

/** SVG viewport of the Stitch export. Every coordinate here is in these units. */
private const val VIEWPORT_W = 420f
private const val VIEWPORT_H = 380f

/** CSS `ease-in-out` — `cubic-bezier(0.42, 0, 0.58, 1)`. */
private val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

/** The four badge flights — `cubic-bezier(0.3, 0.05, 0.2, 1)`. */
private val FlyEasing = CubicBezierEasing(0.3f, 0.05f, 0.2f, 1f)

/** Pointer press/release — `cubic-bezier(0.2, 0.8, 0.2, 1)`. */
private val TapEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/** CSS `ease-out` — `cubic-bezier(0, 0, 0.58, 1)`, the ripple's shockwave. */
private val EaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)

/** The +4 badge's spring — `cubic-bezier(0.34, 1.56, 0.64, 1)`. */
private val BackOut = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/** The status pill's editorial rise — `cubic-bezier(0.16, 1, 0.3, 1)`. */
private val ExpoOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

// --- Keyframe tracks --------------------------------------------------------

/** Card breath: `translateY` on the 6s `productCardMotion` cycle. */
private val CARD_TIMES = floatArrayOf(0f, 0.5f, 1f)
private val CARD_DY = floatArrayOf(0f, -5f, 0f)

/** Pointer: glide in, press at 1.8s, leave by 2.8s. */
private val TAP_TIMES = floatArrayOf(0f, 0.10f, 0.15f, 0.18f, 0.22f, 0.28f, 1f)
private val TAP_ALPHA = floatArrayOf(0f, 0f, 1f, 1f, 1f, 0f, 0f)
private val TAP_X = floatArrayOf(145f, 145f, 138f, 138f, 138f, 138f, 138f)
private val TAP_Y = floatArrayOf(205f, 205f, 196f, 196f, 196f, 186f, 186f)
private val TAP_SCALE = floatArrayOf(0.6f, 0.6f, 1f, 0.82f, 1f, 0.8f, 0.8f)

/** Ripple: radiates from scale 0.3 to 1.6 while fading out. */
private val RIPPLE_TIMES = floatArrayOf(0f, 0.17f, 0.18f, 0.26f, 1f)
private val RIPPLE_ALPHA = floatArrayOf(0f, 0f, 0.85f, 0f, 0f)
private val RIPPLE_SCALE = floatArrayOf(0.3f, 0.3f, 0.6f, 1.6f, 1f)

/** Cart badge: pops at 5.3s with a back-out overshoot, exits at 9.8s. */
private val POP_TIMES = floatArrayOf(0f, 0.48f, 0.53f, 0.58f, 0.92f, 0.98f, 1f)
private val POP_ALPHA = floatArrayOf(0f, 0f, 1f, 1f, 1f, 0f, 0f)
private val POP_SCALE = floatArrayOf(0f, 0f, 1.3f, 1f, 1f, 0.5f, 0.5f)

/** Bag cushion: expand, dampen, settle between 4.8s and 6.6s. */
private val BAG_TIMES = floatArrayOf(0f, 0.45f, 0.5f, 0.55f, 0.6f, 0.66f, 0.72f, 1f)
private val BAG_SCALE = floatArrayOf(1f, 1f, 1.03f, 1.08f, 0.96f, 1.02f, 1f, 1f)
private val BAG_DY = floatArrayOf(0f, 0f, -2f, -5f, 2f, 0f, 0f, 0f)

/** Status pill: rise at 6.2s, hold, dissolve upward from 9.8s. */
private val PILL_TIMES = floatArrayOf(0f, 0.54f, 0.62f, 0.68f, 0.92f, 0.98f, 1f)
private val PILL_ALPHA = floatArrayOf(0f, 0f, 1f, 1f, 1f, 0f, 0f)
private val PILL_DY = floatArrayOf(12f, 12f, 0f, 0f, 0f, -6f, -6f)
private val PILL_SCALE = floatArrayOf(0.94f, 0.94f, 1.02f, 1f, 1f, 0.96f, 0.96f)

/** Trajectory arcs: dash offset travels while the glow peaks mid-flight. */
private val ARC_TIMES = floatArrayOf(0f, 0.15f, 0.4f, 0.6f, 1f)
private val ARC_ALPHA = floatArrayOf(0.15f, 0.15f, 0.75f, 0.15f, 0.15f)
private val ARC_PHASE = floatArrayOf(300f, 300f, 0f, -300f, -300f)

/** Sparkles: 3.5s ambient shimmer. */
private val SPARK_TIMES = floatArrayOf(0f, 0.5f, 1f)
private val SPARK_ALPHA = floatArrayOf(0.35f, 1f, 0.35f)
private val SPARK_SCALE = floatArrayOf(0.85f, 1.15f, 0.85f)

// --- Frames (sampled by the canvas, asserted by the tests) ------------------

internal data class TapFrame(val x: Float, val y: Float, val scale: Float, val alpha: Float)
internal data class RippleFrame(val scale: Float, val alpha: Float)
internal data class ArcFrame(val phase: Float, val alpha: Float)
internal data class BagFrame(val scale: Float, val dy: Float)
internal data class PopFrame(val scale: Float, val alpha: Float)
internal data class PillFrame(val dy: Float, val scale: Float, val alpha: Float)
internal data class SparkleFrame(val alpha: Float, val scale: Float)

internal fun cardFloatDy(progress: Float): Float =
    keyframesValue(progress, CARD_TIMES, CARD_DY, EaseInOut)

internal fun tapFrame(progress: Float) = TapFrame(
    x = keyframesValue(progress, TAP_TIMES, TAP_X, TapEasing),
    y = keyframesValue(progress, TAP_TIMES, TAP_Y, TapEasing),
    scale = keyframesValue(progress, TAP_TIMES, TAP_SCALE, TapEasing),
    alpha = keyframesValue(progress, TAP_TIMES, TAP_ALPHA, TapEasing)
)

internal fun rippleFrame(progress: Float) = RippleFrame(
    scale = keyframesValue(progress, RIPPLE_TIMES, RIPPLE_SCALE, EaseOut),
    alpha = keyframesValue(progress, RIPPLE_TIMES, RIPPLE_ALPHA, EaseOut)
)

internal fun arcFrame(progress: Float) = ArcFrame(
    phase = keyframesValue(progress, ARC_TIMES, ARC_PHASE, LinearEasing),
    alpha = keyframesValue(progress, ARC_TIMES, ARC_ALPHA, LinearEasing)
)

internal fun bagFrame(progress: Float) = BagFrame(
    scale = keyframesValue(progress, BAG_TIMES, BAG_SCALE, EaseInOut),
    dy = keyframesValue(progress, BAG_TIMES, BAG_DY, EaseInOut)
)

internal fun popFrame(progress: Float) = PopFrame(
    scale = keyframesValue(progress, POP_TIMES, POP_SCALE, BackOut),
    alpha = keyframesValue(progress, POP_TIMES, POP_ALPHA, BackOut)
)

internal fun pillFrame(progress: Float) = PillFrame(
    dy = keyframesValue(progress, PILL_TIMES, PILL_DY, ExpoOut),
    scale = keyframesValue(progress, PILL_TIMES, PILL_SCALE, ExpoOut),
    alpha = keyframesValue(progress, PILL_TIMES, PILL_ALPHA, ExpoOut)
)

internal fun sparkleFrame(progress: Float) = SparkleFrame(
    alpha = keyframesValue(progress, SPARK_TIMES, SPARK_ALPHA, EaseInOut),
    scale = keyframesValue(progress, SPARK_TIMES, SPARK_SCALE, EaseInOut)
)

/** One badge's sampled flight pose, in export units. */
internal data class FlightFrame(
    val x: Float,
    val y: Float,
    val scale: Float,
    val rotation: Float,
    val alpha: Float
)

/** A CSS keyframe track: x, y, scale, rotation (degrees) and opacity. */
private class FlightPath(
    times: FloatArray,
    xs: FloatArray,
    ys: FloatArray,
    scales: FloatArray,
    rotations: FloatArray,
    alphas: FloatArray
) {
    private val times = times.copyOf()
    private val xs = xs.copyOf()
    private val ys = ys.copyOf()
    private val scales = scales.copyOf()
    private val rotations = rotations.copyOf()
    private val alphas = alphas.copyOf()

    fun at(progress: Float) = FlightFrame(
        x = keyframesValue(progress, times, xs, FlyEasing),
        y = keyframesValue(progress, times, ys, FlyEasing),
        scale = keyframesValue(progress, times, scales, FlyEasing),
        rotation = keyframesValue(progress, times, rotations, FlyEasing),
        alpha = keyframesValue(progress, times, alphas, FlyEasing)
    )
}

/**
 * The four flying categories, in launch order. The tracks are the export's
 * `flyDress` / `flyLamp` / `flyPerfume` / `flyTech` keyframes verbatim; the
 * labels reuse the page-2 showcase's Arabic strings (the export's English
 * pills are traded for legible Arabic, per the showcase precedent).
 */
internal enum class OrdersMark(
    @param:StringRes val labelRes: Int,
    times: FloatArray,
    xs: FloatArray,
    ys: FloatArray,
    scales: FloatArray,
    rotations: FloatArray,
    alphas: FloatArray
) {
    /** أزياء — uppermost arc, forward tilt. */
    Dress(
        R.string.onboarding_showcase_fashion,
        times = floatArrayOf(0f, 0.12f, 0.18f, 0.22f, 0.32f, 0.42f, 0.48f, 0.52f, 1f),
        xs = floatArrayOf(95f, 90f, 90f, 105f, 175f, 255f, 288f, 288f, 288f),
        ys = floatArrayOf(120f, 95f, 90f, 75f, 60f, 95f, 155f, 175f, 175f),
        scales = floatArrayOf(0.3f, 0.75f, 0.85f, 0.85f, 0.8f, 0.7f, 0.4f, 0.2f, 0.2f),
        rotations = floatArrayOf(0f, 0f, 0f, 0f, 10f, 4f, 0f, 0f, 0f),
        alphas = floatArrayOf(0f, 0.95f, 1f, 1f, 1f, 1f, 1f, 0f, 0f)
    ),

    /** منزل — mid-high trajectory, counter-rotation. */
    Lamp(
        R.string.onboarding_showcase_home,
        times = floatArrayOf(0f, 0.14f, 0.18f, 0.24f, 0.35f, 0.45f, 0.51f, 0.55f, 1f),
        xs = floatArrayOf(95f, 120f, 125f, 145f, 205f, 270f, 295f, 295f, 295f),
        ys = floatArrayOf(120f, 105f, 100f, 85f, 75f, 115f, 160f, 178f, 178f),
        scales = floatArrayOf(0.3f, 0.75f, 0.85f, 0.85f, 0.8f, 0.68f, 0.38f, 0.2f, 0.2f),
        rotations = floatArrayOf(0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f),
        alphas = floatArrayOf(0f, 0.95f, 1f, 1f, 1f, 1f, 1f, 0f, 0f)
    ),

    /** جمال — central path, refined poise. */
    Perfume(
        R.string.onboarding_showcase_beauty,
        times = floatArrayOf(0f, 0.13f, 0.18f, 0.26f, 0.37f, 0.46f, 0.52f, 0.56f, 1f),
        xs = floatArrayOf(95f, 85f, 80f, 120f, 195f, 265f, 284f, 284f, 284f),
        ys = floatArrayOf(120f, 140f, 145f, 125f, 105f, 135f, 165f, 180f, 180f),
        scales = floatArrayOf(0.3f, 0.75f, 0.85f, 0.85f, 0.78f, 0.65f, 0.35f, 0.2f, 0.2f),
        rotations = floatArrayOf(0f, 0f, 0f, 0f, 12f, 0f, 0f, 0f, 0f),
        alphas = floatArrayOf(0f, 0.95f, 1f, 1f, 1f, 1f, 1f, 0f, 0f)
    ),

    /** تقنية — lower arc, anchored attitude. */
    Audio(
        R.string.onboarding_showcase_tech,
        times = floatArrayOf(0f, 0.15f, 0.18f, 0.28f, 0.39f, 0.48f, 0.54f, 0.58f, 1f),
        xs = floatArrayOf(95f, 115f, 120f, 140f, 215f, 278f, 292f, 292f, 292f),
        ys = floatArrayOf(120f, 150f, 155f, 140f, 120f, 145f, 168f, 182f, 182f),
        scales = floatArrayOf(0.3f, 0.75f, 0.85f, 0.85f, 0.76f, 0.65f, 0.35f, 0.2f, 0.2f),
        rotations = floatArrayOf(0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f),
        alphas = floatArrayOf(0f, 0.95f, 1f, 1f, 1f, 1f, 1f, 0f, 0f)
    );

    private val path = FlightPath(times, xs, ys, scales, rotations, alphas)

    fun at(progress: Float): FlightFrame = path.at(progress)
}

// --- Anchors (export coordinates) -------------------------------------------

private const val CARD_X = 32f
private const val CARD_Y = 80f
private const val CARD_W = 132f
private const val CARD_H = 155f
private const val CARD_R = 14f

/** Card tag centre: card origin (32, 80) + tag rect centre (44, 20). */
private const val TAG_CENTER_X = 76f
private const val TAG_CENTER_Y = 100f

/** Caption centre in the export's empty "Card Description" slot: (32+60, 80+114). */
private const val BOX_CENTER_X = 92f
private const val BOX_CENTER_Y = 194f

private const val RIPPLE_RADIUS = 14f
private const val RIPPLE_STROKE = 2f
private val RIPPLE_CENTER = Offset(138f, 196f)

private const val BAG_X = 290f
private const val BAG_Y = 195f
private const val BADGE_X = 328f
private const val BADGE_Y = 148f
private const val PILL_X = 210f
private const val PILL_Y = 320f

/**
 * Both `.sparkle` groups scale without an explicit `transform`, so CSS resolves
 * transform-origin against the view-box: the centre of the 420x380 viewport.
 */
private val SPARKLE_PIVOT = Offset(210f, 190f)

/** Where a badge's label sits: the export's pill centre, 24.5 units below it. */
private const val MARK_LABEL_OFFSET = 24.5f

// --- Palette ----------------------------------------------------------------

private val COPPER_STOPS = listOf(
    Color(0xFFC5936E),
    Color(0xFFA9744F),
    Color(0xFF8E5E3B)
)

private val EMERALD_STOPS = listOf(
    Color(0xFF2D4D43),
    Color(0xFF142621)
)

/**
 * Brand palette pulled from the Material theme. The export's off-whites map
 * onto the existing beige ramp rather than being hardcoded here: `#FFFFFF` ->
 * the pure white container, `#FAF8F5`/`#FDF9F2` -> surface, `#EDE7DD` ->
 * surfaceVariant, `#1F3A32`/`#A9744F` -> primary/secondary.
 */
private class OrdersPalette(
    val forest: Color,
    val copper: Color,
    val white: Color,
    val cream: Color,
    val variant: Color,
    val aura: Brush
)

@Composable
private fun ordersPalette(): OrdersPalette {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) {
        OrdersPalette(
            forest = scheme.primary,
            copper = scheme.secondary,
            white = scheme.surfaceContainerLowest,
            cream = scheme.surface,
            variant = scheme.surfaceVariant,
            // `luxAura` — the warm copper glow behind the whole stage.
            aura = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to scheme.secondary.copy(alpha = 0.18f),
                    0.6f to scheme.secondary.copy(alpha = 0.05f),
                    1f to scheme.secondary.copy(alpha = 0f)
                ),
                center = Offset(210f, 185f),
                radius = 175f
            )
        )
    }
}

// --- Paths ------------------------------------------------------------------

private fun path(data: String): Path = PathParser().parsePathString(data).toPath()

/** Every curve from the export, parsed once and reused across frames. */
private class OrdersArt {
    // Card crest monogram (authored around (0, 6)).
    val cardMonogram = path(
        "M 3,2 C 2,0.5 0,-0.5 -2,-0.5 C -5,-0.5 -7,2 -7,5.5 C -7,9 -4.5,11.5 -1.5,11.5 " +
            "C 1.5,11.5 4,10 4.5,8 C 4.8,7 4.8,6 4.8,5.5 L 0,5.5 L 0,6.5 L 3.5,6.5 " +
            "C 3,7.8 1.5,10 -1.5,10 C -3.5,10 -5.5,8 -5.5,5.5 C -5.5,3 -3.5,1 -1.5,1 " +
            "C -0.2,1 1.2,1.5 2.2,2.5 Z"
    )

    // Dual-dashed golden trajectory arcs: card -> bag.
    val arcMain = path("M 125 120 Q 200 45, 290 170")
    val arcSecond = path("M 130 145 Q 210 95, 285 180")

    // Dress (أزياء)
    val dressStrap = path("M 0,-13 C -2,-13 -3,-10 -1,-8 L 0,-6")
    val dressNeck = path("M -5,-5 Q 0,-2 5,-5")
    val dressBody = path("M -6,-5 L -4,0 Q -6,5 -3,7 L -11,14 C -11,15 11,15 11,14 L 3,7 Q 6,5 4,0 L 6,-5")

    // Lamp (منزل)
    val lampShade = path("M -9,-5 L 9,-5 L 6,-14 L -6,-14 Z")
    val lampBase = path("M -7,10 L 7,10")

    // Headphones (تقنية)
    val headband = path("M -11,4 C -11,-10 11,-10 11,4")

    // Luxury bag
    val bagBack = path("M -35,-24 L 35,-24 L 44,44 L -44,44 Z")
    val tissue = path("M -24,-22 C -18,-36 0,-34 6,-22 C 12,-36 28,-34 32,-20 Z")
    val handleMain = path("M -16,-24 C -16,-60 16,-60 16,-24")
    val handleGhost = path("M -13,-24 C -13,-54 13,-54 13,-24")
    val bagBody = path("M -40,-10 L 40,-10 L 48,46 C 48,49 45,52 42,52 L -42,52 C -45,52 -48,49 -48,46 Z")
    val bagMonogram = path(
        "M 4,-5 C 2.5,-7 -0.5,-8 -3.5,-8 C -8,-8 -11,-4 -11,0 C -11,4.5 -7.5,8 -3,8 " +
            "C 1.5,8 5.5,5.5 6,2.5 C 6.5,1 6.5,-0.5 6.5,-1 L 0,-1 L 0,0.5 L 4.5,0.5 " +
            "C 3.8,2.5 1.5,5.5 -3,5.5 C -6,5.5 -9,2.5 -9,0 C -9,-3.5 -6,-5.5 -3,-5.5 " +
            "C -0.8,-5.5 1.5,-4.8 3,-3.5 Z"
    )

    // Status pill glyphs (authored around their side icons).
    val check = path("M -3.5 0 L -1 2.8 L 3.8 -2.5")
    val chevron = path("M -2.5 -3 L 2.5 0 L -2.5 3")

    // Corner sparkles
    val starTop = path("M 345 80 Q 345 85 340 85 Q 345 85 345 90 Q 345 85 350 85 Q 345 85 345 80 Z")
    val starSide = path("M 50 250 Q 50 254 46 254 Q 50 254 50 258 Q 50 254 54 254 Q 50 254 50 250 Z")
}

// --- Drawing ----------------------------------------------------------------

/** Draws [drawingBlock] inside the export's 420x380 coordinate space. */
private fun DrawScope.unitLayer(unit: Float, drawingBlock: DrawScope.() -> Unit) {
    withTransform({ scale(unit, unit, pivot = Offset.Zero) }, drawingBlock)
}

private fun dashedStroke(width: Float, vararg intervals: Float): Stroke =
    Stroke(width = width, pathEffect = PathEffect.dashPathEffect(intervals))

private fun DrawScope.drawAura(palette: OrdersPalette) {
    drawCircle(brush = palette.aura, radius = 175f, center = Offset(210f, 185f))
}

private fun DrawScope.drawRings(palette: OrdersPalette) {
    // `<g opacity="0.32">` — folded into the stroke colours.
    val ring = palette.forest.copy(alpha = 0.32f)
    val tick = palette.copper.copy(alpha = 0.32f)
    drawCircle(
        color = ring,
        radius = 150f,
        center = Offset(210f, 185f),
        style = dashedStroke(0.8f, 3f, 5f)
    )
    drawCircle(color = tick, radius = 115f, center = Offset(210f, 185f), style = Stroke(0.5f))
    drawLine(tick, Offset(210f, 35f), Offset(210f, 55f), strokeWidth = 0.8f)
    drawLine(tick, Offset(210f, 315f), Offset(210f, 335f), strokeWidth = 0.8f)
    drawLine(tick, Offset(50f, 185f), Offset(70f, 185f), strokeWidth = 0.8f)
    drawLine(tick, Offset(350f, 185f), Offset(370f, 185f), strokeWidth = 0.8f)
}

private fun DrawScope.drawTrajectory(
    palette: OrdersPalette,
    art: OrdersArt,
    alpha: Float,
    phase: Float
) {
    // `stroke-dashoffset` on the export's group: the phase parameter of
    // PathEffect.dashPathEffect has the same "offset into the pattern" meaning.
    drawPath(
        path = art.arcMain,
        brush = Brush.linearGradient(
            COPPER_STOPS,
            start = Offset(125f, 45f),
            end = Offset(290f, 170f)
        ),
        alpha = alpha,
        style = Stroke(1.6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), phase))
    )
    drawPath(
        path = art.arcSecond,
        color = palette.copper.copy(alpha = 0.5f * alpha),
        style = Stroke(1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f), phase))
    )
}

private fun DrawScope.drawPathSparkles(palette: OrdersPalette, alpha: Float) {
    drawCircle(color = palette.copper.copy(alpha = alpha), radius = 1.5f, center = Offset(160f, 85f))
    drawCircle(color = palette.copper.copy(alpha = alpha), radius = 2f, center = Offset(215f, 70f))
    drawCircle(color = palette.copper.copy(alpha = alpha), radius = 1.5f, center = Offset(255f, 105f))
}

/** The floating showcase card, in the card group's local coordinates. */
private fun DrawScope.drawCard(palette: OrdersPalette, art: OrdersArt) {
    // Flat hairline frame — the export's soft drop shadow is deliberately
    // dropped (quiet-luxury surfaces stay flat).
    drawRoundRect(
        color = palette.white,
        size = Size(CARD_W, CARD_H),
        cornerRadius = CornerRadius(CARD_R)
    )
    drawRoundRect(
        color = palette.forest.copy(alpha = 0.12f),
        size = Size(CARD_W, CARD_H),
        cornerRadius = CornerRadius(CARD_R),
        style = Stroke(1f)
    )

    // Editorial GAZA STORE tag (its text rides in the pixel pass).
    drawRoundRect(
        color = palette.cream,
        topLeft = Offset(10f, 12f),
        size = Size(68f, 16f),
        cornerRadius = CornerRadius(8f)
    )

    // Curated-box crest medallion.
    drawCircle(color = palette.cream, radius = 28f, center = Offset(66f, 74f))
    drawCircle(
        color = palette.copper.copy(alpha = 0.6f),
        radius = 24f,
        center = Offset(66f, 74f),
        style = dashedStroke(0.7f, 2f, 3f)
    )
    drawCircle(color = palette.white, radius = 18f, center = Offset(66f, 74f), style = Stroke(1f))
    withTransform({ translate(66f, 68f) }) {
        drawPath(art.cardMonogram, palette.forest)
        drawCircle(color = palette.copper, radius = 1.2f, center = Offset(0f, -2f))
    }

    // The copper + button the pointer taps.
    withTransform({ translate(106f, 124f) }) {
        drawCircle(
            brush = Brush.linearGradient(
                COPPER_STOPS,
                start = Offset(-12f, -12f),
                end = Offset(12f, 12f)
            ),
            radius = 12f,
            center = Offset.Zero
        )
        drawCircle(
            color = palette.white.copy(alpha = 0.4f),
            radius = 12f,
            center = Offset.Zero,
            style = Stroke(0.8f)
        )
        drawLine(
            color = palette.white,
            start = Offset(-4.5f, 0f),
            end = Offset(4.5f, 0f),
            strokeWidth = 1.6f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = palette.white,
            start = Offset(0f, -4.5f),
            end = Offset(0f, 4.5f),
            strokeWidth = 1.6f,
            cap = StrokeCap.Round
        )
    }
}

/** The luxury pointer resting on the + button. */
private fun DrawScope.drawPointer(palette: OrdersPalette, alpha: Float) {
    drawCircle(color = palette.forest.copy(alpha = 0.15f * alpha), radius = 7f, center = Offset.Zero)
    drawCircle(color = palette.forest.copy(alpha = alpha), radius = 4f, center = Offset.Zero)
    drawCircle(color = palette.cream.copy(alpha = alpha), radius = 1.5f, center = Offset.Zero)
}

/** One flying category badge: disc, copper rings and its line-art glyph. */
private fun DrawScope.drawMark(palette: OrdersPalette, art: OrdersArt, mark: OrdersMark) {
    drawCircle(color = palette.white, radius = 17f, center = Offset.Zero)
    drawCircle(color = palette.copper, radius = 17f, center = Offset.Zero, style = Stroke(1.2f))
    drawCircle(
        color = palette.copper.copy(alpha = 0.6f),
        radius = 14.5f,
        center = Offset.Zero,
        style = dashedStroke(0.5f, 1.5f, 2f)
    )

    when (mark) {
        OrdersMark.Dress -> withTransform({
            translate(0f, 1f)
            scale(0.68f, 0.68f, pivot = Offset.Zero)
        }) {
            drawPath(art.dressStrap, palette.copper, style = Stroke(1f, cap = StrokeCap.Round))
            drawPath(art.dressNeck, palette.forest, style = Stroke(1.2f, cap = StrokeCap.Round))
            drawPath(art.dressBody, palette.forest, style = Stroke(1.3f, join = StrokeJoin.Round))
            drawLine(
                color = palette.copper,
                start = Offset(-4f, 2f),
                end = Offset(4f, 2f),
                strokeWidth = 1.4f,
                cap = StrokeCap.Round
            )
        }

        OrdersMark.Lamp -> withTransform({
            translate(0f, 1f)
            scale(0.68f, 0.68f, pivot = Offset.Zero)
        }) {
            drawPath(art.lampShade, palette.forest, style = Stroke(1.3f, join = StrokeJoin.Round))
            drawLine(palette.copper, Offset(-9f, -5f), Offset(9f, -5f), strokeWidth = 1.4f)
            drawLine(palette.forest, Offset(0f, -5f), Offset(0f, 10f), strokeWidth = 1.2f)
            drawCircle(color = palette.copper, radius = 1.6f, center = Offset(0f, -1f))
            drawPath(art.lampBase, palette.forest, style = Stroke(1.8f, cap = StrokeCap.Round))
        }

        OrdersMark.Perfume -> withTransform({
            translate(0f, 1f)
            scale(0.68f, 0.68f, pivot = Offset.Zero)
        }) {
            drawRoundRect(
                color = palette.copper,
                topLeft = Offset(-4f, -13f),
                size = Size(8f, 5f),
                cornerRadius = CornerRadius(1f)
            )
            drawRect(color = palette.forest, topLeft = Offset(-1.5f, -8f), size = Size(3f, 2f))
            drawRoundRect(
                color = palette.forest,
                topLeft = Offset(-9f, -6f),
                size = Size(18f, 19f),
                cornerRadius = CornerRadius(2.5f),
                style = Stroke(1.3f)
            )
            drawRoundRect(
                color = palette.cream,
                topLeft = Offset(-5.5f, -2f),
                size = Size(11f, 11f),
                cornerRadius = CornerRadius(1f)
            )
            drawRoundRect(
                color = palette.copper,
                topLeft = Offset(-5.5f, -2f),
                size = Size(11f, 11f),
                cornerRadius = CornerRadius(1f),
                style = dashedStroke(0.7f, 1f, 1f)
            )
            drawCircle(color = palette.copper, radius = 1.2f, center = Offset(0f, 3.5f))
        }

        OrdersMark.Audio -> withTransform({
            translate(0f, -1f)
            scale(0.68f, 0.68f, pivot = Offset.Zero)
        }) {
            drawPath(art.headband, palette.forest, style = Stroke(1.4f, cap = StrokeCap.Round))
            drawCircle(color = palette.copper, radius = 1.8f, center = Offset(-11f, 4f))
            drawCircle(color = palette.copper, radius = 1.8f, center = Offset(11f, 4f))
            listOf(-14f, 9f).forEach { x ->
                drawRoundRect(
                    color = palette.white,
                    topLeft = Offset(x, 2f),
                    size = Size(5f, 12f),
                    cornerRadius = CornerRadius(2.5f)
                )
                drawRoundRect(
                    color = palette.forest,
                    topLeft = Offset(x, 2f),
                    size = Size(5f, 12f),
                    cornerRadius = CornerRadius(2.5f),
                    style = Stroke(1.2f)
                )
            }
            drawLine(palette.copper, Offset(-11.5f, 5f), Offset(-11.5f, 11f), strokeWidth = 1f)
            drawLine(palette.copper, Offset(11.5f, 5f), Offset(11.5f, 11f), strokeWidth = 1f)
        }
    }
}

/** The signature Gaza Store bag, in the bag group's local coordinates. */
private fun DrawScope.drawBag(palette: OrdersPalette, art: OrdersArt) {
    // Flat contact ellipse.
    drawOval(
        color = palette.forest.copy(alpha = 0.08f),
        topLeft = Offset(-46f, 46f),
        size = Size(92f, 12f)
    )

    // Back wall with its silk lining.
    drawPath(art.bagBack, palette.variant)
    drawPath(art.bagBack, palette.forest, style = Stroke(1f, join = StrokeJoin.Round))
    drawPath(art.tissue, palette.cream)
    drawPath(art.tissue, palette.copper.copy(alpha = 0.6f), style = Stroke(0.8f))

    // Handles, behind the front wall.
    drawPath(
        path = art.handleMain,
        brush = Brush.linearGradient(
            COPPER_STOPS,
            start = Offset(-16f, -60f),
            end = Offset(16f, -24f)
        ),
        style = Stroke(2.6f, cap = StrokeCap.Round)
    )
    drawPath(art.handleGhost, palette.forest.copy(alpha = 0.35f), style = Stroke(0.75f))

    // Front body with its perspective flare, then the burnished trim.
    drawPath(art.bagBody, palette.white)
    drawPath(art.bagBody, palette.forest, style = Stroke(1.5f, join = StrokeJoin.Round))
    drawLine(
        brush = Brush.linearGradient(
            COPPER_STOPS,
            start = Offset(-40f, -10f),
            end = Offset(40f, -10f)
        ),
        start = Offset(-40f, -10f),
        end = Offset(40f, -10f),
        strokeWidth = 2.4f,
        cap = StrokeCap.Round
    )

    // Monogram medallion.
    withTransform({ translate(0f, 22f) }) {
        drawCircle(color = palette.cream, radius = 14f, center = Offset.Zero)
        drawCircle(color = palette.forest, radius = 14f, center = Offset.Zero, style = Stroke(0.6f))
        drawCircle(
            color = palette.copper,
            radius = 16.5f,
            center = Offset.Zero,
            style = dashedStroke(0.7f, 1.5f, 2f)
        )
        drawPath(art.bagMonogram, palette.forest)
        drawCircle(color = palette.copper, radius = 1.2f, center = Offset(0f, -10.5f))
    }

    // Rivets at the opening.
    listOf(-16f, 16f).forEach { x ->
        drawCircle(color = palette.forest, radius = 2.5f, center = Offset(x, -6f))
        drawCircle(color = palette.copper, radius = 2.5f, center = Offset(x, -6f), style = Stroke(1f))
    }
}

/** The emerald completion pill; its copy is drawn in the pixel pass. */
private fun DrawScope.drawStatusPill(palette: OrdersPalette, art: OrdersArt, alpha: Float) {
    drawRoundRect(
        brush = Brush.linearGradient(
            EMERALD_STOPS.map { it.copy(alpha = alpha) },
            start = Offset(-125f, -20f),
            end = Offset(125f, 20f)
        ),
        topLeft = Offset(-125f, -20f),
        size = Size(250f, 40f),
        cornerRadius = CornerRadius(20f)
    )
    drawRoundRect(
        color = palette.copper.copy(alpha = 0.45f * alpha),
        topLeft = Offset(-125f, -20f),
        size = Size(250f, 40f),
        cornerRadius = CornerRadius(20f),
        style = Stroke(1f)
    )

    // Left: copper disc with the white checkmark.
    drawCircle(color = palette.copper.copy(alpha = alpha), radius = 9f, center = Offset(-98f, 0f))
    drawCircle(
        color = palette.white.copy(alpha = 0.3f * alpha),
        radius = 9f,
        center = Offset(-98f, 0f),
        style = Stroke(0.8f)
    )
    withTransform({ translate(-98f, 0f) }) {
        drawPath(
            path = art.check,
            color = palette.white.copy(alpha = alpha),
            style = Stroke(1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // Right: outlined ring with the express-checkout chevron.
    drawCircle(
        color = palette.white.copy(alpha = 0.12f * alpha),
        radius = 9f,
        center = Offset(98f, 0f)
    )
    drawCircle(color = palette.copper.copy(alpha = alpha), radius = 9f, center = Offset(98f, 0f), style = Stroke(1f))
    withTransform({ translate(98f, 0f) }) {
        drawPath(
            path = art.chevron,
            color = palette.copper.copy(alpha = alpha),
            style = Stroke(1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

private fun DrawScope.drawCornerSparkles(palette: OrdersPalette, art: OrdersArt, alpha: Float) {
    drawPath(art.starTop, palette.copper.copy(alpha = 0.8f * alpha))
    drawPath(art.starSide, palette.copper.copy(alpha = 0.7f * alpha))
    drawCircle(color = palette.copper.copy(alpha = 0.5f * alpha), radius = 1.5f, center = Offset(360f, 225f))
    drawCircle(color = palette.copper.copy(alpha = 0.6f * alpha), radius = 1.5f, center = Offset(75f, 55f))
}

// --- Pixel-pass text --------------------------------------------------------

private fun DrawScope.drawCentered(
    layout: TextLayoutResult,
    center: Offset,
    color: Color,
    alpha: Float = 1f
) {
    drawText(
        textLayoutResult = layout,
        color = color,
        alpha = alpha,
        topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f)
    )
}

/**
 * The four Arabic labels, measured at real sp. Each rides below its badge at
 * the export's pill offset, rotated with the badge's tilt.
 */
private fun DrawScope.drawMarkLabels(
    layouts: List<TextLayoutResult>,
    marks: List<FlightFrame>,
    unit: Float,
    copper: Color
) {
    layouts.forEachIndexed { index, layout ->
        val frame = marks[index]
        if (frame.alpha <= 0f) return@forEachIndexed

        val radians = Math.toRadians(frame.rotation.toDouble())
        val reach = MARK_LABEL_OFFSET * frame.scale * unit
        val center = Offset(
            x = frame.x * unit - (reach * sin(radians)).toFloat(),
            y = frame.y * unit + (reach * cos(radians)).toFloat()
        )
        rotate(degrees = frame.rotation, pivot = center) {
            drawCentered(layout, center, copper, frame.alpha)
        }
    }
}
