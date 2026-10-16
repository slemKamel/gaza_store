package com.slem.gazastor.ui.components

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The showcase's motion comes from CSS keyframe tracks, so the sampler has to
 * reproduce CSS semantics: values are held at the stops and the
 * `animation-timing-function` applies to *each* segment rather than once across
 * the whole timeline.
 */
class ShowcaseKeyframesTest {

    private val times = floatArrayOf(0f, 0.5f, 1f)
    private val values = floatArrayOf(0f, 10f, 20f)

    @Test
    fun keyframesValue_clampsOutsideTheRange() {
        assertEquals(0f, keyframesValue(-1f, times, values, LinearEasing), TOLERANCE)
        assertEquals(20f, keyframesValue(2f, times, values, LinearEasing), TOLERANCE)
    }

    @Test
    fun keyframesValue_returnsEachStopValueExactly() {
        assertEquals(0f, keyframesValue(0f, times, values, LinearEasing), TOLERANCE)
        assertEquals(10f, keyframesValue(0.5f, times, values, LinearEasing), TOLERANCE)
        assertEquals(20f, keyframesValue(1f, times, values, LinearEasing), TOLERANCE)
    }

    @Test
    fun keyframesValue_interpolatesWithinASegment() {
        assertEquals(5f, keyframesValue(0.25f, times, values, LinearEasing), TOLERANCE)
        assertEquals(15f, keyframesValue(0.75f, times, values, LinearEasing), TOLERANCE)
    }

    @Test
    fun keyframesValue_easesEachSegmentNotTheWholeTimeline() {
        // Snapping to the end of the current segment separates the two, because
        // a whole-timeline easing would pull every sample off its stop value.
        val snapToSegmentEnd = Easing { 1f }

        assertEquals(10f, keyframesValue(0.1f, times, values, snapToSegmentEnd), TOLERANCE)
        assertEquals(20f, keyframesValue(0.6f, times, values, snapToSegmentEnd), TOLERANCE)
    }

    @Test
    fun keyframesValue_holdsTheValueAcrossAFlatSegment() {
        val holds = floatArrayOf(0f, 0.6f, 0.8f, 1f)
        val plateau = floatArrayOf(0f, 5f, 5f, 9f)

        assertEquals(5f, keyframesValue(0.7f, holds, plateau, LinearEasing), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
