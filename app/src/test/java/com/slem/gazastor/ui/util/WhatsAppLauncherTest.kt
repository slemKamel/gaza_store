package com.slem.gazastor.ui.util

import com.slem.gazastor.data.repository.FALLBACK_WHATSAPP_NUMBER
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The WhatsApp hand-over is shared by the payment flow and the account support
 * row, so the number validation has to be strict: `wa.me` only accepts digits.
 */
class WhatsAppLauncherTest {

    @Test
    fun validNumber_isElevenToFifteenAsciiDigits() {
        assertTrue(isValidWhatsAppNumber("972567982362"))
        assertTrue(isValidWhatsAppNumber("05991234567"))
        assertFalse(isValidWhatsAppNumber("1234567890")) // 10 digits
        assertFalse(isValidWhatsAppNumber("1234567890123456")) // 16 digits
    }

    @Test
    fun punctuationAndNonAsciiDigits_areRejected() {
        assertFalse(isValidWhatsAppNumber("+972567982362"))
        assertFalse(isValidWhatsAppNumber("972 567 982 362"))
        assertFalse(isValidWhatsAppNumber("972-567-982-362"))
        assertFalse(isValidWhatsAppNumber("٩٧٢٥٦٧٩٨٢٣٦٢"))
        assertFalse(isValidWhatsAppNumber(""))
    }

    @Test
    fun resolve_prefersAValidConfiguredNumber() {
        assertEquals("972500000000", resolveWhatsAppNumber("972500000000"))
    }

    @Test
    fun resolve_fallsBackWhenTheConfiguredNumberIsMissingOrInvalid() {
        assertEquals(FALLBACK_WHATSAPP_NUMBER, resolveWhatsAppNumber(null))
        assertEquals(FALLBACK_WHATSAPP_NUMBER, resolveWhatsAppNumber(""))
        assertEquals(FALLBACK_WHATSAPP_NUMBER, resolveWhatsAppNumber("+972500000000"))
        assertEquals(FALLBACK_WHATSAPP_NUMBER, resolveWhatsAppNumber("123"))
    }

    @Test
    fun fallbackNumber_isItselfValid() {
        assertNotNull(resolveWhatsAppNumber(null))
        assertTrue(isValidWhatsAppNumber(FALLBACK_WHATSAPP_NUMBER))
    }
}
