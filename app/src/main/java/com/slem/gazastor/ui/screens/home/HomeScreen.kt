package com.slem.gazastor.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slem.gazastor.R
import com.slem.gazastor.ui.components.EmptyState
import com.slem.gazastor.ui.components.ErrorState
import com.slem.gazastor.ui.components.ItemGridCard
import com.slem.gazastor.ui.components.LoadingIndicator
import com.slem.gazastor.ui.components.ProductImage
import com.slem.gazastor.ui.components.SectionTitle
import com.slem.gazastor.ui.components.fullWidthItem
import com.slem.gazastor.ui.model.BannerUi
import com.slem.gazastor.ui.model.CategoryUi
import com.slem.gazastor.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onCategoryClick: (String) -> Unit,
    onItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = uiState,
        onCategoryClick = onCategoryClick,
        onItemClick = onItemClick,
        onRetry = viewModel::loadData
    )
}

/** Stateless home grid: banners, categories and the recent product grid. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onCategoryClick: (String) -> Unit,
    onItemClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
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
            if (uiState.isLoading) {
                fullWidthItem(key = "loading") { LoadingIndicator() }
            }

            if (uiState.isError) {
                fullWidthItem(key = "error") { ErrorState(onRetry = onRetry) }
            }

            if (uiState.isEmpty) {
                fullWidthItem(key = "empty") { EmptyState() }
            }

            if (uiState.banners.isNotEmpty()) {
                fullWidthItem(key = "banners") {
                    SectionTitle(stringResource(R.string.home_banners))
                    Spacer(modifier = Modifier.height(Dimens.SectionSpacing))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)) {
                        items(uiState.banners, key = { it.id }) { banner ->
                            BannerItem(banner)
                        }
                    }
                }
            }

            if (uiState.categories.isNotEmpty()) {
                fullWidthItem(key = "categories") {
                    SectionTitle(stringResource(R.string.home_categories))
                    Spacer(modifier = Modifier.height(Dimens.SectionSpacing))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)) {
                        items(uiState.categories, key = { it.id }) { category ->
                            CategoryItem(category) { onCategoryClick(category.id) }
                        }
                    }
                }
            }

            if (uiState.recentItems.isNotEmpty()) {
                fullWidthItem(key = "recent-title") {
                    SectionTitle(stringResource(R.string.home_recent_items))
                }
                items(uiState.recentItems, key = { it.id }) { item ->
                    ItemGridCard(item, onClick = { onItemClick(item.id) })
                }
            }
        }
    }
}

/** Banner with a solid green label bar (no gradient), per the brand rules. */
@Composable
private fun BannerItem(banner: BannerUi) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(150.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ProductImage(
                model = banner.imageUrl,
                contentDescription = banner.title,
                modifier = Modifier.fillMaxSize()
            )
            if (banner.title.isNotBlank()) {
                Text(
                    text = banner.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

/** Category tile: image on top, name below on beige — always readable. */
@Composable
private fun CategoryItem(category: CategoryUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimens.Border, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            ProductImage(
                model = category.imageUrl,
                contentDescription = category.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}
