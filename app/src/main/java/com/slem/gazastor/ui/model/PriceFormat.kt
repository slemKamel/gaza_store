package com.slem.gazastor.ui.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats a numeric amount for display, e.g. 1500.0 -> "1,500", 12.5 -> "12.5".
 * Western digits are used deliberately so prices read the same in Arabic and
 * match the [com.slem.gazastor.R.string.currency_format] suffix.
 */
fun formatPriceValue(value: Double): String =
    DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US)).format(value)
