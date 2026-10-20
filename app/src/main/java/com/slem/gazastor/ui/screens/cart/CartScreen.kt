package com.slem.gazastor.ui.screens.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.AmountText
import com.slem.gazastor.ui.components.LoadingIndicator
import com.slem.gazastor.ui.components.ProductImage
import com.slem.gazastor.ui.components.QuantityStepper
import com.slem.gazastor.ui.components.SummaryRow
import com.slem.gazastor.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBrowseClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onSignInClick: () -> Unit,
    viewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CartContent(
        uiState = uiState,
        onBrowseClick = onBrowseClick,
        onCheckoutClick = onCheckoutClick,
        onSignInClick = onSignInClick,
        onIncrease = viewModel::increase,
        onDecrease = viewModel::decrease,
        onRemove = viewModel::remove
    )
}

/** Stateless cart: line rows plus the summary bottom bar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CartContent(
    uiState: CartUiState,
    onBrowseClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onSignInClick: () -> Unit,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // The TopAppBar carries the status inset itself and the parent's
        // bottom bar owns the bottom edge — no system insets here, or they'd
        // pad the content twice.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_cart)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            if (!uiState.isLoading && !uiState.isEmpty) {
                CartSummary(
                    uiState = uiState,
                    onCheckoutClick = onCheckoutClick,
                    onSignInClick = onSignInClick
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            uiState.isEmpty -> EmptyCart(
                onBrowseClick = onBrowseClick,
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
                items(items = uiState.lines, key = { it.itemId }) { line ->
                    CartLineRow(
                        line = line,
                        onIncrease = { onIncrease(line.itemId) },
                        onDecrease = { onDecrease(line.itemId) },
                        onRemove = { onRemove(line.itemId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartLineRow(
    line: CartLineUi,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(
                model = line.imageUrl,
                contentDescription = line.title,
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.small),
                fallbackIconKey = line.iconKey
            )

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                AmountText(
                    value = line.unitPrice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuantityStepper(
                    quantity = line.quantity,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.cart_remove),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AmountText(
                    value = line.lineTotal,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun CartSummary(
    uiState: CartUiState,
    onCheckoutClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(
                label = stringResource(R.string.cart_subtotal),
                value = uiState.subtotal
            )
            SummaryRow(
                label = stringResource(R.string.cart_delivery),
                value = uiState.deliveryFee
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(
                label = stringResource(R.string.cart_total),
                value = uiState.total,
                emphasized = true
            )

            if (!uiState.isSignedIn) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.cart_sign_in_to_checkout),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = if (uiState.isSignedIn) onCheckoutClick else onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight)
            ) {
                Text(
                    text = stringResource(
                        if (uiState.isSignedIn) {
                            R.string.cart_checkout
                        } else {
                            R.string.action_sign_in
                        }
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun EmptyCart(onBrowseClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.cart_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBrowseClick) {
                Text(stringResource(R.string.action_browse))
            }
        }
    }
}
