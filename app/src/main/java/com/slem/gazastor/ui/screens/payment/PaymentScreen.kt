package com.slem.gazastor.ui.screens.payment

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.core.di.AppModule
import com.slem.gazastor.data.repository.SettingsRepository
import com.slem.gazastor.ui.components.AmountText
import com.slem.gazastor.ui.components.AppButton
import com.slem.gazastor.ui.components.CheckoutStepIndicator
import com.slem.gazastor.ui.components.EmptyState
import com.slem.gazastor.ui.components.SectionTitle
import com.slem.gazastor.ui.components.SummaryRow
import com.slem.gazastor.ui.model.WhatsAppOrderItem
import com.slem.gazastor.ui.model.WhatsAppOrderMessage
import com.slem.gazastor.ui.screens.checkout.CheckoutError
import com.slem.gazastor.ui.screens.checkout.CheckoutEvent
import com.slem.gazastor.ui.screens.checkout.CheckoutUiState
import com.slem.gazastor.ui.screens.checkout.CheckoutViewModel
import com.slem.gazastor.ui.screens.checkout.PlacedOrder
import com.slem.gazastor.ui.theme.Dimens
import com.slem.gazastor.ui.util.openWhatsApp
import com.slem.gazastor.ui.util.resolveWhatsAppNumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The step of the checkout flow the payment screen sits on. */
private const val PaymentStep = 2

/**
 * How long the "WhatsApp is not available" notice stays up before the app
 * moves on to the order history. The order is already saved at that point.
 */
private const val WhatsAppFallbackDelayMs = 2_000L

/**
 * Helper to call the checkout error message extension from the checkout package.
 * Needed because both CheckoutError and PaymentFieldError have a message() extension.
 */
@Composable
private fun checkoutErrorMessage(error: CheckoutError): String = stringResource(
    when (error) {
        CheckoutError.NameRequired -> R.string.checkout_error_name
        CheckoutError.PhoneRequired -> R.string.checkout_error_phone
        CheckoutError.AddressRequired -> R.string.checkout_error_address
        CheckoutError.NotSignedIn -> R.string.checkout_error_auth
        CheckoutError.CartEmpty -> R.string.cart_empty
        CheckoutError.SubmitFailed -> R.string.checkout_error_unknown
    }
)

/**
 * The payment step of the checkout flow: pick how to pay, then confirm.
 *
 * The delivery details and the order total come from the checkout view model,
 * which is shared with the checkout screen, so the order is placed through the
 * exact same flow as before - only the payment step was added in front of it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBackClick: () -> Unit,
    onOrderPlaced: () -> Unit,
    checkoutViewModel: CheckoutViewModel,
    paymentViewModel: PaymentViewModel = viewModel(),
    settingsRepository: SettingsRepository = AppModule.settingsRepository
) {
    val checkoutState by checkoutViewModel.uiState.collectAsState()
    val paymentState by paymentViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val whatsAppNotAvailableMsg = stringResource(R.string.payment_whatsapp_not_available)
    val whatsAppPaymentLabel = stringResource(R.string.payment_method_whatsapp)

    // Every non-WhatsApp method places the order through the event channel,
    // exactly as the checkout screen did before the payment step existed.
    LaunchedEffect(checkoutViewModel) {
        checkoutViewModel.events.collect { event ->
            when (event) {
                is CheckoutEvent.OrderPlaced -> onOrderPlaced()
            }
        }
    }

    val onConfirm: () -> Unit = {
        if (paymentState.method == PaymentMethod.WhatsApp) {
            // The WhatsApp flow needs the placed order itself, not just a
            // navigation signal, so it runs here instead of through the event
            // channel. Doing it inside one coroutine keeps the order, the
            // message and the hand-over in a single, cancellable sequence.
            scope.launch {
                val placed = checkoutViewModel.submitForWhatsApp().getOrNull()
                if (placed != null) {
                    val opened = openWhatsAppChat(
                        context = context,
                        settingsRepository = settingsRepository,
                        placed = placed,
                        paymentLabel = whatsAppPaymentLabel
                    )
                    if (!opened) {
                        // The order is already saved: explain why the app moves
                        // on, then show the order history.
                        launch {
                            snackbarHostState.showSnackbar(
                                message = whatsAppNotAvailableMsg,
                                duration = SnackbarDuration.Short
                            )
                        }
                        delay(WhatsAppFallbackDelayMs)
                    }
                    onOrderPlaced()
                }
            }
        } else {
            checkoutViewModel.submit()
        }
    }

    PaymentContent(
        checkoutState = checkoutState,
        paymentState = paymentState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onConfirm = onConfirm,
        onMethodChange = paymentViewModel::selectMethod,
        onCardNumberChange = paymentViewModel::onCardNumberChange,
        onCardExpiryChange = paymentViewModel::onCardExpiryChange,
        onCardCvvChange = paymentViewModel::onCardCvvChange,
        onCardHolderChange = paymentViewModel::onCardHolderChange,
        onWalletChange = paymentViewModel::onWalletChange,
        onWalletPhoneChange = paymentViewModel::onWalletPhoneChange,
        onFieldBlurred = paymentViewModel::onFieldBlurred
    )
}

/**
 * Opens WhatsApp with the order pre-filled, and returns whether an app
 * accepted the intent.
 *
 * The number comes from the backend setting, with a hardcoded fallback, so a
 * missing or unreachable setting never blocks a placed order. Customer details,
 * totals and lines are read from [placed] - never from the cart, which is
 * already empty by now.
 */
private suspend fun openWhatsAppChat(
    context: Context,
    settingsRepository: SettingsRepository,
    placed: PlacedOrder,
    paymentLabel: String
): Boolean {
    val number = resolveWhatsAppNumber(settingsRepository.getWhatsAppNumber().getOrNull())
        ?: return false
    val message = WhatsAppOrderMessage.build(
        orderId = placed.order.id,
        customerName = placed.customerName,
        address = placed.address,
        phone = placed.phone,
        items = placed.lines.map { line ->
            WhatsAppOrderItem(
                title = line.title,
                quantity = line.quantity,
                lineTotal = line.lineTotal
            )
        },
        subtotal = placed.subtotal,
        deliveryFee = placed.deliveryFee,
        total = placed.total,
        paymentLabel = paymentLabel
    )
    // No WhatsApp and no browser to fall back to means the order stays saved;
    // the caller shows the fallback notice.
    return openWhatsApp(context, number, message)
}

/** Stateless payment step: the method cards, the order summary and the CTA. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PaymentContent(
    checkoutState: CheckoutUiState,
    paymentState: PaymentUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBackClick: () -> Unit,
    onConfirm: () -> Unit,
    onMethodChange: (PaymentMethod) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvvChange: (String) -> Unit,
    onCardHolderChange: (String) -> Unit,
    onWalletChange: (WalletProvider) -> Unit,
    onWalletPhoneChange: (String) -> Unit,
    onFieldBlurred: (PaymentField) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // The TopAppBar carries the status inset itself and the sticky bar owns
        // the bottom edge - no system insets here, or they'd pad twice.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.title_payment)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
                CheckoutStepIndicator(currentStep = PaymentStep)
            }
        },
        bottomBar = {
            if (!checkoutState.isCartEmpty) {
                PaymentBottomBar(
                    total = checkoutState.total,
                    enabled = paymentState.isReady && !checkoutState.isSubmitting,
                    isSubmitting = checkoutState.isSubmitting,
                    onConfirm = onConfirm
                )
            }
        }
    ) { paddingValues ->
        if (checkoutState.isCartEmpty) {
            EmptyState(
                message = stringResource(R.string.cart_empty),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle(stringResource(R.string.payment_section_title))

            // 1. Cash on Delivery
            PaymentMethodCard(
                title = stringResource(R.string.payment_method_cash),
                subtitle = stringResource(R.string.payment_method_cash_desc),
                iconRes = R.drawable.ic_payment_cash,
                selected = paymentState.method == PaymentMethod.Cash,
                onSelect = { onMethodChange(PaymentMethod.Cash) },
                expandedContent = { CashPaymentForm() }
            )

            // 2. WhatsApp Order (NEW - placed second)
            PaymentMethodCard(
                title = stringResource(R.string.payment_method_whatsapp),
                subtitle = stringResource(R.string.payment_method_whatsapp_desc),
                iconRes = R.drawable.ic_payment_whatsapp,
                selected = paymentState.method == PaymentMethod.WhatsApp,
                onSelect = { onMethodChange(PaymentMethod.WhatsApp) },
                expandedContent = { WhatsAppPaymentForm() }
            )

            // 3. Bank Card
            PaymentMethodCard(
                title = stringResource(R.string.payment_method_card),
                subtitle = stringResource(R.string.payment_method_card_desc),
                iconRes = R.drawable.ic_payment_card,
                selected = paymentState.method == PaymentMethod.Card,
                onSelect = { onMethodChange(PaymentMethod.Card) },
                expandedContent = {
                    CardPaymentForm(
                        state = paymentState,
                        onNumberChange = onCardNumberChange,
                        onExpiryChange = onCardExpiryChange,
                        onCvvChange = onCardCvvChange,
                        onHolderChange = onCardHolderChange,
                        onFieldBlurred = onFieldBlurred
                    )
                }
            )

            // 4. E-Wallet
            PaymentMethodCard(
                title = stringResource(R.string.payment_method_wallet),
                subtitle = stringResource(R.string.payment_method_wallet_desc),
                iconRes = R.drawable.ic_payment_wallet,
                selected = paymentState.method == PaymentMethod.Wallet,
                onSelect = { onMethodChange(PaymentMethod.Wallet) },
                expandedContent = {
                    WalletPaymentForm(
                        state = paymentState,
                        onWalletChange = onWalletChange,
                        onPhoneChange = onWalletPhoneChange,
                        onFieldBlurred = onFieldBlurred
                    )
                }
            )

            // 5. Bank Transfer
            PaymentMethodCard(
                title = stringResource(R.string.payment_method_bank),
                subtitle = stringResource(R.string.payment_method_bank_desc),
                iconRes = R.drawable.ic_payment_bank,
                selected = paymentState.method == PaymentMethod.BankTransfer,
                onSelect = { onMethodChange(PaymentMethod.BankTransfer) },
                expandedContent = { BankTransferForm() }
            )

            SectionTitle(stringResource(R.string.checkout_order_summary))

            SummaryRow(
                label = stringResource(R.string.cart_subtotal),
                value = checkoutState.subtotal
            )
            SummaryRow(
                label = stringResource(R.string.cart_delivery),
                value = checkoutState.deliveryFee
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SummaryRow(
                label = stringResource(R.string.cart_total),
                value = checkoutState.total,
                emphasized = true
            )

            checkoutState.error?.let { error ->
                Text(
                    text = checkoutErrorMessage(error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SecurePaymentNote()
        }
    }
}

/**
 * The reassuring line under the order summary: the payment is encrypted.
 */
@Composable
private fun SecurePaymentNote(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.PaymentSecureIconSize)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = stringResource(R.string.payment_secure),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * The sticky bottom bar: the order total and the confirm button. The button
 * stays disabled until the chosen method carries everything it needs.
 */
@Composable
private fun PaymentBottomBar(
    total: Double,
    enabled: Boolean,
    isSubmitting: Boolean,
    onConfirm: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(Dimens.PaymentBottomBarPadding)
            ) {
                AppButton(
                    text = stringResource(R.string.payment_confirm),
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    loading = isSubmitting,
                    trailingContent = {
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        AmountText(
                            value = total,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }
    }
}