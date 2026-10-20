package com.slem.gazastor.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.EmptyState
import com.slem.gazastor.ui.components.ItemGridCard
import com.slem.gazastor.ui.components.QuickCartActions
import com.slem.gazastor.ui.components.SkeletonCard
import com.slem.gazastor.ui.components.fullWidthItem
import com.slem.gazastor.ui.model.ItemUi
import com.slem.gazastor.ui.screens.cart.CartViewModel
import com.slem.gazastor.ui.screens.cart.quickCartActions
import com.slem.gazastor.ui.theme.Dimens

/** The "المفضلة" tab: every product the user saved, from the local store. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    onItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartState by cartViewModel.uiState.collectAsState()

    FavoritesContent(
        uiState = uiState,
        onItemClick = onItemClick,
        onFavoriteClick = viewModel::toggleFavorite,
        quickCart = cartViewModel.quickCartActions(cartState)
    )
}

/** Stateless favorites grid. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FavoritesContent(
    uiState: FavoritesUiState,
    onItemClick: (String) -> Unit,
    onFavoriteClick: (ItemUi) -> Unit,
    quickCart: QuickCartActions = QuickCartActions()
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // The TopAppBar carries the status inset itself and the parent's
        // bottom bar owns the bottom edge — no system insets here, or they'd
        // pad the content twice.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_favorites)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Dimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
        ) {
            if (uiState.isLoading && uiState.items.isEmpty()) {
                repeat(4) { index ->
                    item(key = "skeleton-$index") { SkeletonCard() }
                }
            }

            if (uiState.isEmpty) {
                fullWidthItem(key = "empty") {
                    EmptyState(message = stringResource(R.string.favorites_empty))
                }
            }

            items(uiState.items, key = { it.id }) { item ->
                ItemGridCard(
                    item = item,
                    onClick = { onItemClick(item.id) },
                    isFavorite = true,
                    onFavoriteClick = { onFavoriteClick(item) },
                    quantity = quickCart.quantities[item.id] ?: 0,
                    onAdd = { quickCart.onAdd(item) },
                    onIncrease = { quickCart.onIncrease(item.id) },
                    onDecrease = { quickCart.onDecrease(item.id) }
                )
            }
        }
    }
}
