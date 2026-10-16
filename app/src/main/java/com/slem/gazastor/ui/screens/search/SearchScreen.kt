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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.slem.gazastor.ui.components.LoadingIndicator
import com.slem.gazastor.ui.components.SectionTitle
import com.slem.gazastor.ui.components.fullWidthItem
import com.slem.gazastor.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onItemClick = onItemClick,
        onRetry = viewModel::retry
    )
}

/** Stateless search box + result states. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onItemClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_search)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.ScreenPadding),
                        contentPadding = PaddingValues(bottom = Dimens.ScreenPadding),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
                        verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
                    ) {
                        fullWidthItem(key = "results-title") {
                            SectionTitle(stringResource(R.string.search_results_title))
                        }
                        items(uiState.results, key = { it.id }) { item ->
                            ItemGridCard(item, onClick = { onItemClick(item.id) })
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            uiState.isLoading -> LoadingIndicator()

                            uiState.isError -> ErrorState(onRetry = onRetry)

                            uiState.isEmpty -> EmptyState(
                                message = stringResource(R.string.search_no_results)
                            )

                            else -> Text(
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
