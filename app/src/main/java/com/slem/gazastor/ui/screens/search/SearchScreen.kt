package com.slem.gazastor.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.AppTextField
import com.slem.gazastor.ui.components.EmptyState
import com.slem.gazastor.ui.components.ErrorState
import com.slem.gazastor.ui.components.ItemGridCard
import com.slem.gazastor.ui.components.QuickCartActions
import com.slem.gazastor.ui.components.SectionTitle
import com.slem.gazastor.ui.components.SkeletonCard
import com.slem.gazastor.ui.components.fullWidthItem
import com.slem.gazastor.ui.model.ItemUi
import com.slem.gazastor.ui.screens.cart.CartViewModel
import com.slem.gazastor.ui.screens.cart.quickCartActions
import com.slem.gazastor.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    onItemClick: (String) -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val cartState by cartViewModel.uiState.collectAsState()

    SearchContent(
        uiState = uiState,
        favoriteIds = favoriteIds,
        onQueryChange = viewModel::onQueryChange,
        onItemClick = onItemClick,
        onFavoriteClick = viewModel::toggleFavorite,
        onRetry = viewModel::retry,
        onBackClick = onBackClick,
        quickCart = cartViewModel.quickCartActions(cartState)
    )
}

/** Stateless search box + result states. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onItemClick: (String) -> Unit,
    onRetry: () -> Unit,
    favoriteIds: Set<String> = emptySet(),
    onFavoriteClick: (ItemUi) -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    quickCart: QuickCartActions = QuickCartActions()
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_search)) },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
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
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(horizontal = Dimens.ScreenPadding),
                placeholder = stringResource(R.string.search_hint),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.action_clear)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                )
            )

            Spacer(modifier = Modifier.height(Dimens.SectionSpacing))

            if (uiState.isLoading && uiState.results.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.results.isNotEmpty()) {
                    SearchGrid {
                        fullWidthItem(key = "results-title") {
                            SectionTitle(stringResource(R.string.search_results_title))
                        }
                        items(uiState.results, key = { it.id }) { item ->
                            ItemGridCard(
                                item = item,
                                onClick = { onItemClick(item.id) },
                                isFavorite = item.id in favoriteIds,
                                onFavoriteClick = { onFavoriteClick(item) },
                                quantity = quickCart.quantities[item.id] ?: 0,
                                onAdd = { quickCart.onAdd(item) },
                                onIncrease = { quickCart.onIncrease(item.id) },
                                onDecrease = { quickCart.onDecrease(item.id) }
                            )
                        }
                    }
                } else if (uiState.isLoading || uiState.isError) {
                    // Same placeholder scaffolding as Home: pulsing while the
                    // query runs, frozen behind the error card when it fails.
                    SearchGrid {
                        if (uiState.isError) {
                            fullWidthItem(key = "error") { ErrorState(onRetry = onRetry) }
                        }
                        repeat(4) { index ->
                            item(key = "skeleton-$index") {
                                SkeletonCard(pulse = !uiState.isError)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isEmpty) {
                            EmptyState(message = stringResource(R.string.search_no_results))
                        } else {
                            Text(
                                text = stringResource(R.string.search_prompt),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Two-column grid with the shared catalog padding, used by every Search state. */
@Composable
private fun SearchGrid(content: LazyGridScope.() -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = Dimens.ScreenPadding,
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding
        ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
        content = content
    )
}
