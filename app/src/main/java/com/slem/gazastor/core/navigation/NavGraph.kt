package com.slem.gazastor.core.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.slem.gazastor.R
import com.slem.gazastor.ui.screens.account.AccountScreen
import com.slem.gazastor.ui.screens.auth.AuthScreen
import com.slem.gazastor.ui.screens.cart.CartScreen
import com.slem.gazastor.ui.screens.cart.CartViewModel
import com.slem.gazastor.ui.screens.checkout.CheckoutScreen
import com.slem.gazastor.ui.screens.checkout.CheckoutViewModel
import com.slem.gazastor.ui.screens.detail.DetailScreen
import com.slem.gazastor.ui.screens.favorites.FavoritesScreen
import com.slem.gazastor.ui.screens.home.HomeScreen
import com.slem.gazastor.ui.screens.list.ListScreen
import com.slem.gazastor.ui.screens.onboarding.OnboardingScreen
import com.slem.gazastor.ui.screens.orders.OrdersScreen
import com.slem.gazastor.ui.screens.payment.PaymentScreen
import com.slem.gazastor.ui.screens.search.SearchScreen
import com.slem.gazastor.ui.screens.splash.SplashScreen
import com.slem.gazastor.ui.screens.splash.StartDestination
import com.slem.gazastor.ui.theme.LatinFontFamily
import kotlinx.coroutines.launch

/** Count past which the cart badge collapses to "9+". */
private const val BadgeMaxCount = 9

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Favorites : Screen("favorites")
    data object Cart : Screen("cart")
    data object Orders : Screen("orders")
    data object Account : Screen("account")
    data object Checkout : Screen("checkout")
    data object Payment : Screen("payment")
    data object List : Screen("list/{categoryId}") {
        fun createRoute(categoryId: String) = "list/$categoryId"
    }
    data object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }
}

/** The destinations shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    /** Shown while the tab is selected. */
    val selectedIcon: ImageVector,
    /** Outlined counterpart shown while the tab is not selected. */
    val unselectedIcon: ImageVector
) {
    Home(
        Screen.Home.route,
        R.string.tab_home,
        Icons.Filled.Home,
        Icons.Outlined.Home
    ),
    Favorites(
        Screen.Favorites.route,
        R.string.tab_favorites,
        Icons.Filled.Favorite,
        Icons.Outlined.FavoriteBorder
    ),
    Cart(
        Screen.Cart.route,
        R.string.tab_cart,
        Icons.Filled.ShoppingCart,
        Icons.Outlined.ShoppingCart
    ),
    Orders(
        Screen.Orders.route,
        R.string.tab_orders,
        Icons.AutoMirrored.Filled.List,
        Icons.AutoMirrored.Outlined.List
    ),
    Account(
        Screen.Account.route,
        R.string.tab_account,
        Icons.Filled.Person,
        Icons.Outlined.Person
    )
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

    // Activity-scoped: the same cart the screens write to, so the badge tracks
    // every add/remove (including the quick-add buttons on the grids).
    val cartViewModel: CartViewModel = viewModel()
    val cartState by cartViewModel.uiState.collectAsState()

    // Owned here (above the NavHost) so it survives tab switches and the Home
    // tab can scroll it back to the top when it is tapped while already shown.
    val homeGridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // The screens own their top-app-bar insets; here we only manage the bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    TopLevelDestination.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                when {
                                    !selected -> navController.navigateToTab(tab.route)
                                    // Re-tapping Home returns the grid to the top.
                                    tab == TopLevelDestination.Home -> {
                                        val atTop = homeGridState.firstVisibleItemIndex == 0 &&
                                            homeGridState.firstVisibleItemScrollOffset == 0
                                        if (!atTop) {
                                            scope.launch { homeGridState.animateScrollToItem(0) }
                                        }
                                    }
                                }
                            },
                            icon = {
                                BottomBarIcon(
                                    icon = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    showBadge = tab == TopLevelDestination.Cart && cartState.itemCount > 0,
                                    itemCount = cartState.itemCount
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onFinished = { destination ->
                    val target = when (destination) {
                        StartDestination.Onboarding -> Screen.Onboarding.route
                        StartDestination.Auth -> Screen.Auth.route
                        StartDestination.Home -> Screen.Home.route
                    }
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(onFinish = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Auth.route) {
                AuthScreen(onFinished = {
                    // launchSingleTop so signing in from the Account tab returns
                    // to the Home entry that is already there instead of pushing
                    // a second Home onto the tab stack.
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate(Screen.List.createRoute(categoryId))
                    },
                    onItemClick = { itemId ->
                        navController.navigate(Screen.Detail.createRoute(itemId))
                    },
                    // Search is a pushed screen now (the tab is favorites).
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                    gridState = homeGridState,
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    cartViewModel = cartViewModel,
                    onItemClick = { itemId ->
                        navController.navigate(Screen.Detail.createRoute(itemId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onItemClick = { itemId ->
                        navController.navigate(Screen.Detail.createRoute(itemId))
                    },
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onBrowseClick = { navController.navigateToTab(Screen.Home.route) },
                    onCheckoutClick = { navController.navigate(Screen.Checkout.route) },
                    onSignInClick = { navController.navigateToTab(Screen.Account.route) },
                    viewModel = cartViewModel
                )
            }

            composable(Screen.Orders.route) {
                OrdersScreen(
                    onSignInClick = { navController.navigateToTab(Screen.Account.route) }
                )
            }

            composable(Screen.Account.route) {
                AccountScreen(
                    onGuestClick = { navController.navigate(Screen.Auth.route) }
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onContinueToPayment = { navController.navigate(Screen.Payment.route) },
                    onOrderPlaced = {
                        // The placed order makes the checkout form stale, so drop
                        // it instead of saving it as a restorable entry.
                        navController.navigateToTab(
                            Screen.Orders.route,
                            saveCurrentState = false
                        )
                    }
                )
            }

            composable(Screen.Payment.route) {
                // The payment step continues the checkout form, so it shares the
                // checkout view model (and its order totals) instead of keeping
                // a second copy of the delivery details.
                val checkoutEntry = runCatching {
                    navController.getBackStackEntry(Screen.Checkout.route)
                }.getOrNull()
                val checkoutViewModel: CheckoutViewModel = if (checkoutEntry != null) {
                    viewModel(viewModelStoreOwner = checkoutEntry)
                } else {
                    viewModel()
                }
                PaymentScreen(
                    checkoutViewModel = checkoutViewModel,
                    onBackClick = { navController.popBackStack() },
                    onOrderPlaced = {
                        navController.navigateToTab(
                            Screen.Orders.route,
                            saveCurrentState = false
                        )
                    }
                )
            }

            composable(Screen.List.route) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty()
                ListScreen(
                    categoryId = categoryId,
                    onItemClick = { itemId ->
                        navController.navigate(Screen.Detail.createRoute(itemId))
                    },
                    onBackClick = { navController.popBackStack() },
                    cartViewModel = cartViewModel
                )
            }

            composable(Screen.Detail.route) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId").orEmpty()
                DetailScreen(
                    itemId = itemId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Switches to a bottom-bar tab, keeping the tab you leave alive: its
 * `NavBackStackEntry` (and with it its ViewModels, `rememberSaveable` state and
 * `LazyGridState`) is saved and put back when the tab is tapped again, so no
 * screen re-runs its initial load or loses its scroll position.
 *
 * [Screen.Home] is the anchor of the tab stack. It deliberately is *not*
 * `graph.findStartDestination()`: that is Splash, which pops itself off the
 * back stack on first launch, and Navigation silently ignores a `popUpTo`
 * target that is not on the back stack - nothing would be popped, saved or
 * restored, and every tab tap would push a fresh entry with a fresh
 * ViewModelStore.
 *
 * [saveCurrentState] is false for flows that must not leave a restorable entry
 * behind, such as Checkout once its order has been placed.
 */
private fun NavHostController.navigateToTab(
    route: String,
    saveCurrentState: Boolean = true
) {
    navigate(route) {
        popUpTo(Screen.Home.route) { saveState = saveCurrentState }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * A bottom-bar icon, with the copper item-count badge on the cart tab. The
 * badge only exists while the cart has items and collapses to "9+" past
 * [BadgeMaxCount].
 */
@Composable
private fun BottomBarIcon(
    icon: ImageVector,
    showBadge: Boolean,
    itemCount: Int
) {
    if (!showBadge) {
        Icon(imageVector = icon, contentDescription = null)
        return
    }
    BadgedBox(
        badge = {
            Badge(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Text(
                    text = if (itemCount > BadgeMaxCount) {
                        stringResource(R.string.cart_badge_overflow)
                    } else {
                        itemCount.toString()
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = LatinFontFamily)
                )
            }
        }
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}


