package com.slem.gazastor.ui.screens.payment

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.AppTextField
import com.slem.gazastor.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long the "copied" confirmation stays on screen. */
private const val CopyConfirmationMs = 2_000L

/** Size of the copy glyph inside its button. */
private val CopyIconSize = 20.dp

/**
 * Reports the moment a field loses focus, so its validation message only
 * appears once the customer actually edited that field.
 *
 * The flag is kept here (and not in the view model) because it is a pure UI
 * concern: a field that never took focus was never "left".
 */
@Composable
internal fun Modifier.onFieldBlur(action: () -> Unit): Modifier {
    var wasFocused by remember { mutableStateOf(false) }
    return this.onFocusChanged { focusState ->
        if (focusState.isFocused) {
            wasFocused = true
        } else if (wasFocused) {
            wasFocused = false
            action()
        }
    }
}

/**
 * The form of the "cash on delivery" method: a single reassuring line, no
 * fields to fill.
 */
@Composable
internal fun CashPaymentForm(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.payment_cash_note),
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * The form of the "WhatsApp order" method: a single reassuring line, no
 * fields to fill.
 */
@Composable
internal fun WhatsAppPaymentForm(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.payment_whatsapp_note),
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * The form of the "bank card" method: the card number (grouped in blocks of
 * four, with the brand recognised while typing), the expiry, the security
 * code and the name printed on the card.
 */
@Composable
internal fun CardPaymentForm(
    state: PaymentUiState,
    onNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onHolderChange: (String) -> Unit,
    onFieldBlurred: (PaymentField) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaymentFieldGap)
    ) {
        AppTextField(
            value = state.cardNumber,
            onValueChange = onNumberChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFieldBlur { onFieldBlurred(PaymentField.CardNumber) },
            label = stringResource(R.string.payment_card_number),
            placeholder = stringResource(R.string.payment_card_number_placeholder),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            visualTransformation = CardNumberTransformation,
            trailingIcon = {
                state.cardBrand?.let { brand ->
                    CardBrandBadge(brand = brand)
                }
            },
            errorText = state.errorFor(PaymentField.CardNumber)?.message()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.PaymentFieldGap)
        ) {
            AppTextField(
                value = state.cardExpiry,
                onValueChange = onExpiryChange,
                modifier = Modifier
                    .weight(1f)
                    .onFieldBlur { onFieldBlurred(PaymentField.CardExpiry) },
                label = stringResource(R.string.payment_card_expiry),
                placeholder = stringResource(R.string.payment_card_expiry_placeholder),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = CardExpiryTransformation,
                errorText = state.errorFor(PaymentField.CardExpiry)?.message()
            )

            AppTextField(
                value = state.cardCvv,
                onValueChange = onCvvChange,
                modifier = Modifier
                    .weight(1f)
                    .onFieldBlur { onFieldBlurred(PaymentField.CardCvv) },
                label = stringResource(R.string.payment_card_cvv),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = PasswordVisualTransformation(),
                errorText = state.errorFor(PaymentField.CardCvv)?.message()
            )
        }

        AppTextField(
            value = state.cardHolder,
            onValueChange = onHolderChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFieldBlur { onFieldBlurred(PaymentField.CardHolder) },
            label = stringResource(R.string.payment_card_holder),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            errorText = state.errorFor(PaymentField.CardHolder)?.message()
        )
    }
}

/**
 * The form of the "e-wallet" method: the wallet to pay with, and the phone
 * number registered to it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WalletPaymentForm(
    state: PaymentUiState,
    onWalletChange: (WalletProvider) -> Unit,
    onPhoneChange: (String) -> Unit,
    onFieldBlurred: (PaymentField) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaymentFieldGap)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WalletProvider.entries.forEach { provider ->
                WalletChip(
                    label = stringResource(provider.labelRes()),
                    selected = state.wallet == provider,
                    onClick = { onWalletChange(provider) }
                )
            }
        }

        AppTextField(
            value = state.walletPhone,
            onValueChange = onPhoneChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFieldBlur { onFieldBlurred(PaymentField.WalletPhone) },
            label = stringResource(R.string.payment_wallet_phone),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            errorText = state.errorFor(PaymentField.WalletPhone)?.message()
        )
    }
}

/**
 * The form of the "bank transfer" method: the account details with a copy
 * button, and a note that the receipt is uploaded later.
 */
@Composable
internal fun BankTransferForm(modifier: Modifier = Modifier) {
    val details = stringResource(R.string.payment_bank_details)
    val copyLabel = stringResource(R.string.payment_bank_copy)
    val copiedLabel = stringResource(R.string.payment_bank_copied)
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(CopyConfirmationMs)
            copied = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text(
                text = details,
                modifier = Modifier.padding(Dimens.PaymentBankDetailsPadding),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.PaymentFieldGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText(copyLabel, details))
                        )
                    }
                    copied = true
                }
            ) {
                if (copied) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = copiedLabel,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(CopyIconSize)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = copyLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(CopyIconSize)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = if (copied) copiedLabel else copyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = if (copied) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.payment_bank_note),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** The label of an e-wallet, as shown on its chip. */
private fun WalletProvider.labelRes(): Int = when (this) {
    WalletProvider.JawwalPay -> R.string.payment_wallet_jawwal
    WalletProvider.PalPay -> R.string.payment_wallet_palpay
    WalletProvider.Reflect -> R.string.payment_wallet_reflect
}

/** The inline validation message of a payment field. */
@Composable
internal fun PaymentFieldError.message(): String = stringResource(
    when (this) {
        PaymentFieldError.CardNumber -> R.string.payment_error_card_number
        PaymentFieldError.CardExpiry -> R.string.payment_error_card_expiry
        PaymentFieldError.CardCvv -> R.string.payment_error_card_cvv
        PaymentFieldError.CardHolder -> R.string.payment_error_card_holder
        PaymentFieldError.Wallet -> R.string.payment_error_wallet
        PaymentFieldError.WalletPhone -> R.string.payment_error_wallet_phone
    }
)
