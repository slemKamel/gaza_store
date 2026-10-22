package com.slem.gazastor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.slem.gazastor.R
import com.slem.gazastor.ui.theme.Dimens

/**
 * The slim three-step bar of the checkout flow: العنوان - الدفع - التأكيد.
 *
 * A finished step is a solid green dot, the current step a copper dot with a
 * copper label, and a step still to come an outlined hairline ring. The bars
 * between the dots fill green as the flow advances.
 *
 * The row is laid out right-to-left with the layout direction the app forces,
 * so the first step sits on the right, exactly where the eye starts.
 */
@Composable
fun CheckoutStepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val labels = listOf(
        stringResource(R.string.payment_step_address),
        stringResource(R.string.payment_step_payment),
        stringResource(R.string.payment_step_confirm)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.ScreenPadding,
                vertical = Dimens.PaymentStepIndicatorPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val step = index + 1
            StepMarker(step = step, currentStep = currentStep, label = label)
            if (step < labels.size) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.PaymentStepConnectorHeight)
                        .background(
                            color = if (step < currentStep) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                )
            }
        }
    }
}

/** One dot plus its label in the step indicator. */
@Composable
private fun StepMarker(step: Int, currentStep: Int, label: String) {
    val isDone = step < currentStep
    val isCurrent = step == currentStep

    val dotColor: Color = when {
        isDone -> MaterialTheme.colorScheme.primary
        isCurrent -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val labelColor: Color = when {
        isDone -> MaterialTheme.colorScheme.primary
        isCurrent -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(
                    if (isCurrent) {
                        Dimens.PaymentStepCurrentDotSize
                    } else {
                        Dimens.PaymentStepDotSize
                    }
                )
                .then(
                    if (isCurrent || isDone) {
                        Modifier.background(dotColor, CircleShape)
                    } else {
                        Modifier.border(Dimens.Border, dotColor, CircleShape)
                    }
                )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isCurrent) FontWeight.SemiBold else null,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
