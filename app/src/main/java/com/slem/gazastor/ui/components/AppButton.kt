package com.slem.gazastor.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slem.gazastor.ui.theme.Dimens

/**
 * The app's single button primitive so every CTA shares one height, shape and
 * label style (auth, onboarding, detail, cart, checkout, account). The outlined
 * variant is the secondary voice (e.g. "متابعة كزائر", sign out).
 *
 * [trailingContent] adds a slot after the label, so a screen can mix scripts
 * inside one button - the Arabic label stays in the Arabic face while a price
 * is set in Inter.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    outlined: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = if (outlined) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                strokeWidth = 2.dp
            )
        } else {
            leadingIcon?.invoke()
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            trailingContent?.invoke()
        }
    }

    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = modifier
                .fillMaxWidth()
                .height(Dimens.ButtonHeight),
            shape = MaterialTheme.shapes.medium,
            content = content
        )
    } else {
        Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = modifier
                .fillMaxWidth()
                .height(Dimens.ButtonHeight),
            shape = MaterialTheme.shapes.medium,
            content = content
        )
    }
}
