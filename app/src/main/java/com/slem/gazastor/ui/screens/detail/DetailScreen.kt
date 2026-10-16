package com.slem.gazastor.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.EmptyState
import com.slem.gazastor.ui.components.ErrorState
import com.slem.gazastor.ui.components.LoadingIndicator
import com.slem.gazastor.ui.components.PriceText
import com.slem.gazastor.ui.components.ProductImage
import com.slem.gazastor.ui.components.QuantityStepper
import com.slem.gazastor.ui.model.ItemUi
import com.slem.gazastor.ui.screens.cart.CartViewModel
import com.slem.gazastor.ui.theme.Dimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: String,
    viewModel: DetailViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addedMessage = stringResource(R.string.cart_added)
    var quantity by remember { mutableIntStateOf(1) }

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
        quantity = 1
    }

    DetailContent(
        uiState = uiState,
        quantity = quantity,
        onQuantityChange = { quantity = it },
        onAddToCart = { item, selectedQuantity ->
            cartViewModel.addItem(item, selectedQuantity)
            quantity = 1
            scope.launch { snackbarHostState.showSnackbar(addedMessage) }
        },
        onBackClick = onBackClick,
        onRetry = viewModel::retry,
        snackbarHostState = snackbarHostState
    )
}

/** Stateless product detail: gallery, price, quantity picker and add-to-cart. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailContent(
    uiState: DetailUiState,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: (ItemUi, Int) -> Unit,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(uiState.item?.title ?: stringResource(R.string.title_detail)) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            }

            if (uiState.isError) {
                ErrorState(onRetry = onRetry)
            }

            uiState.item?.let { item ->
                val gallery = remember(item.id, item.imageUrl, item.images) {
                    (listOf(item.imageUrl) + item.images)
                        .filter { it.isNotBlank() }
                        .distinct()
                }

                if (gallery.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { gallery.size })

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            ProductImage(
                                model = gallery[page],
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }

                        if (gallery.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(gallery.size) { index ->
                                    val selected = pagerState.currentPage == index
                                    Spacer(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .size(if (selected) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (selected) {
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
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (item.price.isNotEmpty()) {
                    PriceText(price = item.price, style = MaterialTheme.typography.titleLarge)
                }

                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.priceValue != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.cart_quantity),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        QuantityStepper(
                            quantity = quantity,
                            onDecrease = { if (quantity > 1) onQuantityChange(quantity - 1) },
                            onIncrease = { onQuantityChange(quantity + 1) }
                        )
                    }

                    Button(
                        onClick = { onAddToCart(item, quantity) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight)
                    ) {
                        Text(
                            text = stringResource(R.string.cart_add_to_cart),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            if (uiState.item == null && !uiState.isLoading && !uiState.isError) {
                EmptyState(message = stringResource(R.string.detail_not_found))
            }
        }
    }
}
