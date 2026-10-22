package com.slem.gazastor.ui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

/**
 * The pure helpers behind the payment form: how a card number, an expiry and a
 * phone number are formatted while typing, and when they are valid.
 */
class PaymentFormatTest {

    @Test
    fun cardDigits_keepsTheDigitsOnly() {
        assertEquals("41111111", cardDigits("4111-1111"))
        assertEquals("41111111", cardDigits("4111 1111"))
    }

    @Test
    fun cardDigits_stopsAtTheLongestPossibleNumber() {
        assertEquals(19, cardDigits("4".repeat(25)).length)
    }

    @Test
    fun expiryDigits_keepsFourDigitsOnly() {
        assertEquals("1229", expiryDigits("12/29"))
        assertEquals("12", expiryDigits("12"))
    }

    @Test
    fun detectCardBrand_recognisesVisaAndMastercard() {
        assertEquals(CardBrand.Visa, detectCardBrand("4111111111111111"))
        assertEquals(CardBrand.Mastercard, detectCardBrand("5555555555554444"))
        assertEquals(CardBrand.Mastercard, detectCardBrand("2221000000000000"))
    }

    @Test
    fun detectCardBrand_returnsNullForAnUnknownPrefix() {
        assertNull(detectCardBrand("9999999999999999"))
        assertNull(detectCardBrand("4"))
    }

    @Test
    fun isValidCardNumber_acceptsANumberThatPassesTheLuhnCheck() {
        assertTrue(isValidCardNumber("4111111111111111"))
        assertTrue(isValidCardNumber("4111 1111 1111 1111"))
    }

    @Test
    fun isValidCardNumber_rejectsATypoAndATooShortNumber() {
        assertFalse(isValidCardNumber("4111111111111112"))
        assertFalse(isValidCardNumber("411111"))
    }

    @Test
    fun isValidCardExpiry_needsARealFutureMonth() {
        val nextYear = YearMonth.now().plusYears(1)
        val month = nextYear.monthValue.toString().padStart(2, '0')
        val year = (nextYear.year % 100).toString().padStart(2, '0')
        assertTrue(isValidCardExpiry("$month/$year"))
        assertFalse(isValidCardExpiry("13/29"))
        assertFalse(isValidCardExpiry("12"))
    }

    @Test
    fun isValidCardExpiry_rejectsAMonthThatAlreadyPassed() {
        val lastYear = YearMonth.now().minusYears(1)
        val month = lastYear.monthValue.toString().padStart(2, '0')
        val year = (lastYear.year % 100).toString().padStart(2, '0')
        assertFalse(isValidCardExpiry("$month/$year"))
    }

    @Test
    fun isValidCardCvv_acceptsThreeOrFourDigits() {
        assertTrue(isValidCardCvv("123"))
        assertTrue(isValidCardCvv("1234"))
        assertFalse(isValidCardCvv("12"))
        assertFalse(isValidCardCvv("12345"))
    }

    @Test
    fun isValidPhone_acceptsAPlausibleNumber() {
        assertTrue(isValidPhone("0599000000"))
        assertFalse(isValidPhone("0599000"))
    }
}
