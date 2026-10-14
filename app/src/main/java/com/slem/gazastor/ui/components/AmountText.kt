package com.slem.gazastor.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.slem.gazastor.R
import com.slem.gazastor.ui.model.formatPriceValue

/**
 * Renders a monetary amount with the shekel suffix (e.g. "1,500 ₪").
 * Unlike [PriceText] it adds no "السعر:" label, so it fits totals and lines.
 */
@Composable
fun AmountText(
    value: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified
) {
    Text(
        text = stringResource(R.string.currency_format, formatPriceValue(value)),
        modifier = modifier,
        style = style,
        color = color
    )
}
