package com.slem.gazastor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.slem.gazastor.R
import com.slem.gazastor.ui.theme.Dimens
import com.slem.gazastor.ui.theme.LatinFontFamily

/**
 * Small − / value / + stepper. Shared by the detail screen (choose how many to
 * add) and the cart (change a line's quantity).
 *
 * The minus is a text glyph on purpose: `Icons.Filled.Remove` lives in
 * material-icons-extended, which this project deliberately does not depend on.
 */
@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val decreaseLabel = stringResource(R.string.cart_decrease)
    val increaseLabel = stringResource(R.string.cart_increase)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .size(36.dp)
                    .semantics { contentDescription = decreaseLabel }
            ) {
                Text(
                    text = "\u2212",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontFamily = LatinFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = increaseLabel
                )
            }
        }
    }
}
