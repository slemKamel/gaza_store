package com.slem.gazastor.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The quick-orders scene is driven by the export's CSS keyframe table, so the
 * tracks are asserted at their stop percentages: exact stops, gate openings and
 * closings, the back-out overshoot, and a seamless loop (t=1 matches t=0).
 */
class QuickOrdersKeyframesTest {

    // --- Flight ---------------------------------------------------------

    @Test
    fun dressFlight_hitsItsMidFlightStopExactly() {
        val frame = OrdersMark.Dress.at(0.32f)
        assertEquals(175f, frame.x, TOLERANCE)
        assertEquals(60f, frame.y, TOLERANCE)
        assertEquals(0.8f, frame.scale, TOLERANCE)
        assertEquals(10f, frame.rotation, TOLERANCE)
        assertEquals(1f, frame.alpha, TOLERANCE)
    }

    @Test
    fun dressFlight_isFullyGoneBehindTheBag() {
        val arrived = OrdersMark.Dress.at(0.52f)
        assertEquals(0f, arrived.alpha, TOLERANCE)
        assertEquals(288f, arrived.x, TOLERANCE)
        assertEquals(0.2f, arrived.scale, TOLERANCE)
    }

    @Test
    fun flight_loopsBackToItsRestState() {
        OrdersMark.entries.forEach { mark ->
            assertEquals(
                "${mark.name} must dissolve identically at both ends of the loop",
                mark.at(0f).alpha,
                mark.at(1f).alpha,
                TOLERANCE
            )
            assertEquals(0f, mark.at(1f).alpha, TOLERANCE)
        }
    }

    @Test
    fun lampFlight_counterRotatesThenSettles() {
        assertEquals(-8f, OrdersMark.Lamp.at(0.35f).rotation, TOLERANCE)
        assertEquals(0f, OrdersMark.Lamp.at(0.45f).rotation, TOLERANCE)
    }

    @Test
    fun perfumeFlight_peaksAtItsTwelveDegreeTilt() {
        assertEquals(12f, OrdersMark.Perfume.at(0.37f).rotation, TOLERANCE)
    }

    // --- Tap ------------------------------------------------------------

    @Test
    fun tap_pressesAtEighteenPercentAndLeavesByTwentyEight() {
        assertEquals(0f, tapFrame(0.10f).alpha, TOLERANCE)

        val down = tapFrame(0.18f)
        assertEquals(0.82f, down.scale, TOLERANCE)
        assertEquals(1f, down.alpha, TOLERANCE)
        assertEquals(138f, down.x, TOLERANCE)
        assertEquals(196f, down.y, TOLERANCE)

        assertEquals(0f, tapFrame(0.28f).alpha, TOLERANCE)
    }

    @Test
    fun ripple_radiatesAndFades() {
        assertEquals(0f, rippleFrame(0.17f).alpha, TOLERANCE)
        assertEquals(0.85f, rippleFrame(0.18f).alpha, TOLERANCE)
        assertEquals(1.6f, rippleFrame(0.26f).scale, TOLERANCE)
        assertEquals(0f, rippleFrame(0.26f).alpha, TOLERANCE)
    }

    // --- Bag & badges ---------------------------------------------------

    @Test
    fun cartBadge_overshootsPastItsStatedScaleThenSettles() {
        // Back-out easing peaks mid-segment, above the 1.3 stop value…
        assertTrue(popFrame(0.51f).scale > 1.3f)
        // …then lands exactly on it.
        assertEquals(1.3f, popFrame(0.53f).scale, TOLERANCE)
        assertEquals(1f, popFrame(0.58f).alpha, TOLERANCE)
        assertEquals(0f, popFrame(0.98f).alpha, TOLERANCE)
    }

    @Test
    fun bagCushion_bouncesThenSettles() {
        val absorb = bagFrame(0.55f)
        assertEquals(1.08f, absorb.scale, TOLERANCE)
        assertEquals(-5f, absorb.dy, TOLERANCE)

        val dampen = bagFrame(0.60f)
        assertEquals(0.96f, dampen.scale, TOLERANCE)
        assertEquals(2f, dampen.dy, TOLERANCE)

        val settled = bagFrame(0.72f)
        assertEquals(1f, settled.scale, TOLERANCE)
        assertEquals(0f, settled.dy, TOLERANCE)
    }

    // --- Status pill -----------------------------------------------------

    @Test
    fun statusPill_risesHoldsThenExitsUpward() {
        assertEquals(0f, pillFrame(0.54f).alpha, TOLERANCE)
        assertEquals(12f, pillFrame(0.54f).dy, TOLERANCE)

        val risen = pillFrame(0.62f)
        assertEquals(1f, risen.alpha, TOLERANCE)
        assertEquals(0f, risen.dy, TOLERANCE)

        val exiting = pillFrame(0.98f)
        assertEquals(-6f, exiting.dy, TOLERANCE)
        assertEquals(0f, exiting.alpha, TOLERANCE)
    }

    // --- Ambient ---------------------------------------------------------

    @Test
    fun arcGlow_peaksAcrossTheFlightWindow() {
        assertEquals(0.15f, arcFrame(0.15f).alpha, TOLERANCE)
        assertEquals(0.75f, arcFrame(0.4f).alpha, TOLERANCE)
        assertEquals(300f, arcFrame(0.15f).phase, TOLERANCE)
        assertEquals(-300f, arcFrame(0.6f).phase, TOLERANCE)
    }

    @Test
    fun cardBreath_roundTripsItsFiveUnitFloat() {
        assertEquals(0f, cardFloatDy(0f), TOLERANCE)
        assertEquals(-5f, cardFloatDy(0.5f), TOLERANCE)
        assertEquals(0f, cardFloatDy(1f), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
