package com.slem.gazastor.ui.screens.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.EmptyState
import com.slem.gazastor.ui.components.ErrorState
import com.slem.gazastor.ui.components.ItemGridCard
import com.slem.gazastor.ui.components.LoadingIndicator
import com.slem.gazastor.ui.components.fullWidthItem
import com.slem.gazastor.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    categoryId: String,
    viewModel: ListViewModel = viewModel(),
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.loadItems(categoryId)
    }

    ListContent(
        uiState = uiState,
        onItemClick = onItemClick,
        onBackClick = onBackClick,
        onRetry = viewModel::retry
    )
}

/** Stateless category grid. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListContent(
    uiState: ListUiState,
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.categoryName.ifBlank { stringResource(R.string.title_items) })
                },
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Dimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
        ) {
            if (uiState.isLoading) {
                fullWidthItem(key = "loading") { LoadingIndicator() }
            }

            if (uiState.isError) {
                fullWidthItem(key = "error") { ErrorState(onRetry = onRetry) }
            }

            if (uiState.isEmpty) {
                fullWidthItem(key = "empty") { EmptyState() }
            }

            items(uiState.items, key = { it.id }) { item ->
                ItemGridCard(item, onClick = { onItemClick(item.id) })
            }
        }
    }
}
