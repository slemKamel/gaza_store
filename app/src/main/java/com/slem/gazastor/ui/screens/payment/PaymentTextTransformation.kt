package com.slem.gazastor.ui.screens.payment

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Groups the card number in blocks of four while it is typed.
 *
 * The field keeps the plain digits and only the *display* is grouped, so the
 * keyboard and the state never fight over the text: rewriting the text on
 * every keystroke (by inserting the spaces back) moves the caret and makes
 * the field drop characters.
 */
internal object CardNumberTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText =
        insertSeparators(
            source = text.text,
            separator = " ",
            breaksBefore = setOf(4, 8, 12, 16)
        )
}

/**
 * Shows the expiry as `MM/YY`: the field keeps the four digits and the slash
 * is only a matter of display, for the same reason as the card number.
 */
internal object CardExpiryTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText =
        insertSeparators(
            source = text.text,
            separator = "/",
            breaksBefore = setOf(2)
        )
}

/**
 * Builds the transformed text and the mapping that keeps the caret on the
 * digit it was on, even though the display is longer than the text.
 *
 * [breaksBefore] holds the indexes of the source characters a separator is
 * inserted in front of; a separator is never added after the last character.
 */
private fun insertSeparators(
    source: String,
    separator: String,
    breaksBefore: Set<Int>
): TransformedText {
    val starts = IntArray(source.length)
    val separatorBefore = BooleanArray(source.length)
    val transformed = StringBuilder(source.length + separator.length * breaksBefore.size)

    source.forEachIndexed { index, char ->
        if (index in breaksBefore) {
            transformed.append(separator)
            separatorBefore[index] = true
        }
        starts[index] = transformed.length
        transformed.append(char)
    }

    return TransformedText(
        text = AnnotatedString(transformed.toString()),
        offsetMapping = SeparatorOffsetMapping(
            sourceLength = source.length,
            separatorLength = separator.length,
            starts = starts,
            separatorBefore = separatorBefore
        )
    )
}

/**
 * Maps a caret between two source characters to its place in the transformed
 * text, and back.
 */
private class SeparatorOffsetMapping(
    private val sourceLength: Int,
    private val separatorLength: Int,
    private val starts: IntArray,
    private val separatorBefore: BooleanArray
) : OffsetMapping {

    /** The length of the transformed text, the separators included. */
    private val transformedLength: Int =
        if (sourceLength == 0) 0 else starts[sourceLength - 1] + 1

    override fun originalToTransformed(offset: Int): Int {
        if (offset <= 0) return 0
        if (offset >= sourceLength) return transformedLength
        val start = starts[offset]
        // A caret that sits in front of a group starts before its separator.
        return if (separatorBefore[offset]) start - separatorLength else start
    }

    override fun transformedToOriginal(offset: Int): Int {
        var original = 0
        var transformed = 0
        while (original < sourceLength) {
            if (separatorBefore[original]) {
                if (transformed + separatorLength > offset) return original
                transformed += separatorLength
            }
            if (transformed + 1 > offset) return original
            transformed += 1
            original += 1
        }
        return original
    }
}
