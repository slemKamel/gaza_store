package com.slem.gazastor.ui.screens.payment

import androidx.lifecycle.ViewModel
import com.slem.gazastor.ui.model.CardBrand
import com.slem.gazastor.ui.model.cardDigits
import com.slem.gazastor.ui.model.detectCardBrand
import com.slem.gazastor.ui.model.digitsOf
import com.slem.gazastor.ui.model.expiryDigits
import com.slem.gazastor.ui.model.isValidCardCvv
import com.slem.gazastor.ui.model.isValidCardExpiry
import com.slem.gazastor.ui.model.isValidCardNumber
import com.slem.gazastor.ui.model.isValidPhone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** The payment methods the customer can choose on the payment screen. */
enum class PaymentMethod {
    Cash,
    WhatsApp,
    Card,
    Wallet,
    BankTransfer
}

/** The e-wallets offered by the "محفظة إلكترونية" method. */
enum class WalletProvider {
    JawwalPay,
    PalPay,
    Reflect
}

/** The fields that carry their own inline validation message. */
enum class PaymentField {
    CardNumber,
    CardExpiry,
    CardCvv,
    CardHolder,
    WalletPhone
}

/** The validation failures the payment form can report. */
enum class PaymentFieldError {
    CardNumber,
    CardExpiry,
    CardCvv,
    CardHolder,
    Wallet,
    WalletPhone
}

/**
 * The state of the payment form.
 *
 * Everything here lives in memory only: the card number and the security code
 * are never written to disk, never put in a `SavedStateHandle` and never
 * logged, so a process death simply drops them.
 */
data class PaymentUiState(
    val method: PaymentMethod = PaymentMethod.Cash,
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvv: String = "",
    val cardHolder: String = "",
    val wallet: WalletProvider? = null,
    val walletPhone: String = "",
    /** The fields the customer already left, and whose error may be shown. */
    val touched: Set<PaymentField> = emptySet()
) {
    /** The card brand recognised from the leading digits, if any. */
    val cardBrand: CardBrand? get() = detectCardBrand(cardNumber)

    /** True when the chosen method carries everything needed to place the order. */
    val isReady: Boolean
        get() = when (method) {
            PaymentMethod.Cash -> true
            PaymentMethod.WhatsApp -> true
            PaymentMethod.Card -> isValidCardNumber(cardNumber) &&
                isValidCardExpiry(cardExpiry) &&
                isValidCardCvv(cardCvv) &&
                cardHolder.trim().length >= MinCardHolderLength
            PaymentMethod.Wallet -> wallet != null && isValidPhone(walletPhone)
            PaymentMethod.BankTransfer -> true
        }

    /**
     * The validation message of [field], or null while the field is still
     * untouched or valid. The wallet row only complains once the customer has
     * touched the phone field next to it.
     */
    fun errorFor(field: PaymentField): PaymentFieldError? {
        if (field !in touched) return null
        return when (field) {
            PaymentField.CardNumber ->
                if (isValidCardNumber(cardNumber)) null else PaymentFieldError.CardNumber

            PaymentField.CardExpiry ->
                if (isValidCardExpiry(cardExpiry)) null else PaymentFieldError.CardExpiry

            PaymentField.CardCvv ->
                if (isValidCardCvv(cardCvv)) null else PaymentFieldError.CardCvv

            PaymentField.CardHolder ->
                if (cardHolder.trim().length >= MinCardHolderLength) {
                    null
                } else {
                    PaymentFieldError.CardHolder
                }

            PaymentField.WalletPhone -> when {
                wallet == null -> PaymentFieldError.Wallet
                isValidPhone(walletPhone) -> null
                else -> PaymentFieldError.WalletPhone
            }
        }
    }
}

/** Shortest accepted cardholder name. */
private const val MinCardHolderLength = 2

/**
 * Holds the payment form: the chosen method and the values typed into it.
 *
 * The view model is scoped to the payment destination, so a rotation keeps the
 * selection and the typed values. It has no repository dependency and never
 * persists anything.
 */
class PaymentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    /** Picks the payment method; the form of the previous one is kept aside. */
    fun selectMethod(method: PaymentMethod) {
        _uiState.update { it.copy(method = method) }
    }

    /**
     * Stores the card number as plain digits.
     *
     * The digits are grouped in blocks of four for the display only, so the
     * text of the field is never rewritten while it is typed.
     */
    fun onCardNumberChange(value: String) {
        _uiState.update { it.copy(cardNumber = cardDigits(value)) }
    }

    /**
     * Stores the expiry as plain digits: the first two are the month and the
     * last two the year. The slash is only a matter of display.
     */
    fun onCardExpiryChange(value: String) {
        _uiState.update { it.copy(cardExpiry = expiryDigits(value)) }
    }

    /** Stores the security code, digits only. */
    fun onCardCvvChange(value: String) {
        _uiState.update { it.copy(cardCvv = digitsOf(value).take(4)) }
    }

    /** Stores the name printed on the card. */
    fun onCardHolderChange(value: String) {
        _uiState.update { it.copy(cardHolder = value.take(MaxCardHolderLength)) }
    }

    /** Picks the e-wallet used for the payment. */
    fun onWalletChange(wallet: WalletProvider) {
        _uiState.update { it.copy(wallet = wallet) }
    }

    /** Stores the wallet phone number, digits only. */
    fun onWalletPhoneChange(value: String) {
        _uiState.update { it.copy(walletPhone = digitsOf(value).take(15)) }
    }

    /** Marks a field as visited, so its validation message may be shown. */
    fun onFieldBlurred(field: PaymentField) {
        _uiState.update { it.copy(touched = it.touched + field) }
    }
}

/** Longest cardholder name kept in memory. */
private const val MaxCardHolderLength = 40
