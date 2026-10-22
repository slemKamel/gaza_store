package com.slem.gazastor.ui.model

import java.time.YearMonth

/**
 * Card brands the payment screen can recognise from the leading digits.
 */
enum class CardBrand {
    Visa,
    Mastercard
}

/** Longest card number accepted while typing (ISO/IEC 7812 allows 19 digits). */
private const val MaxCardDigits = 19

/** Digits of a card number that may be entered before it is considered complete. */
private const val MinCardDigits = 13

/** Length of the security code printed on the back of a card. */
private const val MinCvvLength = 3

/** Length of the security code printed on the back of an American Express card. */
private const val MaxCvvLength = 4

/** Number of digits of a local phone number. */
private const val MinPhoneDigits = 9

/** Number of digits of an international phone number. */
private const val MaxPhoneDigits = 15

/** Keeps only the digits of [value], so formatting never fights the keyboard. */
fun digitsOf(value: String): String = value.filter { it.isDigit() }

/**
 * The digits of a card number, capped at the longest number a card can have.
 *
 * The field keeps the plain digits and only the *display* is grouped in
 * blocks of four (see `CardNumberTransformation`): rewriting the text of the
 * field on every keystroke moves the caret around and drops characters.
 */
fun cardDigits(value: String): String = digitsOf(value).take(MaxCardDigits)

/**
 * The digits of an expiry date: the first two are the month, the last two the
 * year. The slash is only a matter of display (see `CardExpiryTransformation`).
 */
fun expiryDigits(value: String): String = digitsOf(value).take(4)

/**
 * Detects the card brand from the leading digits: Visa starts with 4, while
 * Mastercard uses the 51-55 and 2221-2720 ranges.
 */
fun detectCardBrand(value: String): CardBrand? {
    val digits = digitsOf(value)
    if (digits.length < 2) return null
    if (digits.startsWith("4")) return CardBrand.Visa
    if (digits.length >= 2) {
        val prefix = digits.take(2).toIntOrNull() ?: return null
        if (prefix in 51..55) return CardBrand.Mastercard
    }
    if (digits.length >= 4) {
        val prefix = digits.take(4).toIntOrNull() ?: return null
        if (prefix in 2221..2720) return CardBrand.Mastercard
    }
    return null
}

/**
 * True when the card number is complete and passes the Luhn check digit, so
 * an obviously mistyped number is caught before the order is placed.
 */
fun isValidCardNumber(value: String): Boolean {
    val digits = digitsOf(value)
    if (digits.length !in MinCardDigits..MaxCardDigits) return false
    return passesLuhn(digits)
}

/** Luhn (mod 10) check digit. */
private fun passesLuhn(digits: String): Boolean {
    var sum = 0
    var double = false
    for (index in digits.lastIndex downTo 0) {
        var digit = digits[index] - '0'
        if (double) {
            digit *= 2
            if (digit > 9) digit -= 9
        }
        sum += digit
        double = !double
    }
    return sum % 10 == 0
}

/**
 * True when the expiry is a real `MM/YY` date that has not passed yet.
 */
fun isValidCardExpiry(value: String): Boolean {
    val digits = digitsOf(value)
    if (digits.length != 4) return false
    val month = digits.take(2).toIntOrNull() ?: return false
    val year = digits.takeLast(2).toIntOrNull() ?: return false
    if (month !in 1..12) return false
    val expiry = runCatching { YearMonth.of(2000 + year, month) }.getOrNull() ?: return false
    return !expiry.isBefore(YearMonth.now())
}

/** True for a 3 or 4 digit security code. */
fun isValidCardCvv(value: String): Boolean {
    val digits = digitsOf(value)
    return digits.length in MinCvvLength..MaxCvvLength
}

/** True for a phone number of a plausible local or international length. */
fun isValidPhone(value: String): Boolean {
    val digits = digitsOf(value)
    return digits.length in MinPhoneDigits..MaxPhoneDigits
}
