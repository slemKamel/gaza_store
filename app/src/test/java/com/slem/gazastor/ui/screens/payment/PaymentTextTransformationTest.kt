package com.slem.gazastor.ui.screens.payment

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The display of the card number and of the expiry date.
 *
 * The digits are grouped and slashed without rewriting the text of the field,
 * and the mapping keeps the caret on the digit it was on.
 */
class PaymentTextTransformationTest {

    private fun filtered(
        transformation: androidx.compose.ui.text.input.VisualTransformation,
        value: String
    ) = transformation.filter(AnnotatedString(value))

    @Test
    fun cardNumber_isGroupedInBlocksOfFour() {
        val result = filtered(CardNumberTransformation, "4111111122223333")
        assertEquals("4111 1111 2222 3333", result.text.text)
    }

    @Test
    fun cardNumber_keepsAnIncompleteNumberReadable() {
        assertEquals("4111 111", filtered(CardNumberTransformation, "4111111").text.text)
        assertEquals("4111", filtered(CardNumberTransformation, "4111").text.text)
        assertEquals("", filtered(CardNumberTransformation, "").text.text)
    }

    @Test
    fun cardExpiry_insertsTheSlashAfterTheMonth() {
        assertEquals("12", filtered(CardExpiryTransformation, "12").text.text)
        assertEquals("12/2", filtered(CardExpiryTransformation, "122").text.text)
        assertEquals("12/29", filtered(CardExpiryTransformation, "1229").text.text)
    }

    @Test
    fun cardNumber_mapsTheCaretOntoTheGroupedText() {
        val mapping = filtered(CardNumberTransformation, "4111111122223333").offsetMapping
        // The caret behind the fourth digit stays in front of the separator.
        assertEquals(4, mapping.originalToTransformed(4))
        assertEquals(6, mapping.originalToTransformed(5))
        // The caret at the end of the number sits behind the last digit.
        assertEquals(19, mapping.originalToTransformed(16))
    }

    @Test
    fun cardNumber_mapsTheGroupedTextBackOntoTheDigits() {
        val mapping = filtered(CardNumberTransformation, "4111111122223333").offsetMapping
        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(4, mapping.transformedToOriginal(4))
        assertEquals(4, mapping.transformedToOriginal(5))
        assertEquals(16, mapping.transformedToOriginal(19))
    }

    @Test
    fun cardExpiry_mapsTheCaretOntoTheSlash() {
        val mapping = filtered(CardExpiryTransformation, "1229").offsetMapping
        assertEquals(2, mapping.originalToTransformed(2))
        assertEquals(4, mapping.originalToTransformed(3))
        assertEquals(5, mapping.originalToTransformed(4))
        assertEquals(2, mapping.transformedToOriginal(3))
        assertEquals(4, mapping.transformedToOriginal(5))
    }
}
