package com.slem.gazastor.ui.screens.account

import org.junit.Assert.assertEquals
import org.junit.Test

/** The avatar initials helper is pure presentation, so it is tested directly. */
class AccountInitialsTest {

    @Test
    fun twoWordName_usesTheFirstLetterOfEachWord() {
        // "محمد أحمد" -> م + أ
        assertEquals("مأ", accountInitials("محمد أحمد", "a@b.com"))
        assertEquals("MA", accountInitials("Mohammed Ahmed", null))
    }

    @Test
    fun singleWordName_usesOneLetter() {
        assertEquals("م", accountInitials("محمد", null))
    }

    @Test
    fun blankName_fallsBackToTheEmailLocalPart() {
        assertEquals("S", accountInitials("   ", "sara@example.com"))
    }

    @Test
    fun missingNameAndEmail_usesTheNeutralDot() {
        assertEquals("•", accountInitials(null, null))
        assertEquals("•", accountInitials("  ", "  "))
    }

    @Test
    fun extraWhitespace_isIgnored() {
        assertEquals("مأ", accountInitials("  محمد   أحمد  ", null))
    }
}
