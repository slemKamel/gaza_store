package com.slem.gazastor.ui.screens.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.AmountText
import com.slem.gazastor.ui.components.EmptyState
import com.slem.gazastor.ui.components.ErrorState
import com.slem.gazastor.ui.components.LoadingIndicator
import com.slem.gazastor.ui.components.PillBadge
import com.slem.gazastor.ui.components.SignInRequired
import com.slem.gazastor.ui.theme.Dimens

private const val STATUS_PENDING = "pending"
private const val STATUS_CONFIRMED = "confirmed"
private const val STATUS_SHIPPED = "shipped"
private const val STATUS_DELIVERED = "delivered"
private const val STATUS_CANCELLED = "cancelled"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onSignInClick: () -> Unit,
    viewModel: OrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    OrdersContent(
        uiState = uiState,
        onSignInClick = onSignInClick,
        onRetry = viewModel::retry
    )
}

/** Stateless order history with sign-in, loading, error and empty states. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrdersContent(
    uiState: OrdersUiState,
    onSignInClick: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // The TopAppBar carries the status inset itself and the parent's
        // bottom bar owns the bottom edge — no system insets here, or they'd
        // pad the content twice.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_orders)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        when {
            !uiState.isSignedIn -> SignInRequired(
                message = stringResource(R.string.orders_sign_in_required),
                onSignInClick = onSignInClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            uiState.isLoading -> LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            uiState.isError -> ErrorState(
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            uiState.isEmpty -> EmptyState(
                message = stringResource(R.string.orders_empty),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
            ) {
                items(items = uiState.orders, key = { it.id }) { order ->
                    OrderCard(order = order)
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.orders_order_number, order.shortId),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (order.date.isNotEmpty()) {
                        Text(
                            text = order.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(status = order.status)
            }

            if (order.lines.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                order.lines.forEach { line ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.orders_item_quantity, line.quantity),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = line.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        AmountText(
                            value = line.lineTotal,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cart_total),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AmountText(
                    value = order.total,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (container, content) = when (status) {
        STATUS_PENDING -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer
        STATUS_CONFIRMED -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
        STATUS_DELIVERED -> MaterialTheme.colorScheme.primary to
            MaterialTheme.colorScheme.onPrimary
        STATUS_CANCELLED -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to
            MaterialTheme.colorScheme.onSurfaceVariant
    }
    PillBadge(text = status.label(), containerColor = container, contentColor = content)
}

@Composable
private fun String.label(): String = when (this) {
    STATUS_PENDING -> stringResource(R.string.orders_status_pending)
    STATUS_CONFIRMED -> stringResource(R.string.orders_status_confirmed)
    STATUS_SHIPPED -> stringResource(R.string.orders_status_shipped)
    STATUS_DELIVERED -> stringResource(R.string.orders_status_delivered)
    STATUS_CANCELLED -> stringResource(R.string.orders_status_cancelled)
    else -> stringResource(R.string.orders_status_unknown)
}
