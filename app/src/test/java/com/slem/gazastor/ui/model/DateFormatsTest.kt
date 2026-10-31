package com.slem.gazastor.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatsTest {

    @Test
    fun memberSince_formatsAsYearAndMonth() {
        assertEquals("2026/03", formatMemberSince("2026-03-12T10:00:00+00:00"))
        assertEquals("2026/11", formatMemberSince("2026-11-01T23:59:59.123456+02:00"))
    }

    @Test
    fun memberSince_missingOrInvalidValuesBecomeEmpty() {
        assertEquals("", formatMemberSince(null))
        assertEquals("", formatMemberSince(""))
        assertEquals("", formatMemberSince("   "))
        assertEquals("", formatMemberSince("not-a-date"))
    }

    @Test
    fun orderDate_stillFormatsTheFullDate() {
        assertEquals("2026/10/14", formatOrderDate("2026-10-14T09:30:00+00:00"))
        assertEquals("", formatOrderDate(null))
    }
}
