package com.slem.gazastor.ui.screens.payment

import com.slem.gazastor.ui.model.CardBrand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

/**
 * The payment form state: the chosen method, the values typed into it and when
 * a field is allowed to complain about what was typed.
 */
class PaymentViewModelTest {

    private val viewModel = PaymentViewModel()

    private fun futureExpiry(): String {
        val next = YearMonth.now().plusYears(2)
        val month = next.monthValue.toString().padStart(2, '0')
        val year = (next.year % 100).toString().padStart(2, '0')
        return "$month/$year"
    }

    @Test
    fun startsOnCashAndIsReadyToConfirm() {
        val state = viewModel.uiState.value
        assertEquals(PaymentMethod.Cash, state.method)
        assertTrue(state.isReady)
    }

    @Test
    fun selectMethod_switchesTheMethod() {
        viewModel.selectMethod(PaymentMethod.Card)
        assertEquals(PaymentMethod.Card, viewModel.uiState.value.method)
    }

    @Test
    fun onCardNumberChange_keepsThePlainDigits() {
        viewModel.onCardNumberChange("4111111122223333")
        assertEquals("4111111122223333", viewModel.uiState.value.cardNumber)
        assertEquals(CardBrand.Visa, viewModel.uiState.value.cardBrand)
    }

    @Test
    fun onCardExpiryChange_keepsThePlainDigits() {
        viewModel.onCardExpiryChange("1229")
        assertEquals("1229", viewModel.uiState.value.cardExpiry)
    }

    @Test
    fun onCardCvvChange_keepsDigitsOnlyAndAtMostFour() {
        viewModel.onCardCvvChange("12a45")
        assertEquals("1245", viewModel.uiState.value.cardCvv)
    }

    @Test
    fun card_isNotReadyUntilEveryFieldIsValid() {
        viewModel.selectMethod(PaymentMethod.Card)

        viewModel.onCardNumberChange("4111111111111111")
        viewModel.onCardExpiryChange(futureExpiry())
        viewModel.onCardCvvChange("123")
        assertFalse(viewModel.uiState.value.isReady)

        viewModel.onCardHolderChange("أحمد محمود")
        assertTrue(viewModel.uiState.value.isReady)
    }

    @Test
    fun wallet_isNotReadyWithoutAChosenWallet() {
        viewModel.selectMethod(PaymentMethod.Wallet)
        viewModel.onWalletPhoneChange("0599000000")
        assertFalse(viewModel.uiState.value.isReady)

        viewModel.onWalletChange(WalletProvider.JawwalPay)
        assertTrue(viewModel.uiState.value.isReady)
    }

    @Test
    fun bankTransfer_isAlwaysReady() {
        viewModel.selectMethod(PaymentMethod.BankTransfer)
        assertTrue(viewModel.uiState.value.isReady)
    }

    @Test
    fun error_isHiddenUntilTheFieldWasLeft() {
        viewModel.selectMethod(PaymentMethod.Card)
        assertNull(viewModel.uiState.value.errorFor(PaymentField.CardNumber))

        viewModel.onFieldBlurred(PaymentField.CardNumber)
        assertEquals(
            PaymentFieldError.CardNumber,
            viewModel.uiState.value.errorFor(PaymentField.CardNumber)
        )
    }

    @Test
    fun error_disappearsOnceTheFieldIsValid() {
        viewModel.selectMethod(PaymentMethod.Card)
        viewModel.onFieldBlurred(PaymentField.CardNumber)
        assertEquals(
            PaymentFieldError.CardNumber,
            viewModel.uiState.value.errorFor(PaymentField.CardNumber)
        )

        viewModel.onCardNumberChange("4111111111111111")
        assertNull(viewModel.uiState.value.errorFor(PaymentField.CardNumber))
    }

    @Test
    fun walletError_complainsAboutTheMissingWalletOnceThePhoneWasTouched() {
        viewModel.selectMethod(PaymentMethod.Wallet)
        viewModel.onFieldBlurred(PaymentField.WalletPhone)
        assertEquals(
            PaymentFieldError.Wallet,
            viewModel.uiState.value.errorFor(PaymentField.WalletPhone)
        )
    }
}
