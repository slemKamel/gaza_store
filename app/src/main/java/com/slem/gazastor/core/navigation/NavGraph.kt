package com.slem.gazastor.core.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.slem.gazastor.R
import com.slem.gazastor.ui.screens.account.AccountScreen
import com.slem.gazastor.ui.screens.auth.AuthScreen
import com.slem.gazastor.ui.screens.cart.CartScreen
import com.slem.gazastor.ui.screens.checkout.CheckoutScreen
import com.slem.gazastor.ui.screens.detail.DetailScreen
import com.slem.gazastor.ui.screens.home.HomeScreen
import com.slem.gazastor.ui.screens.list.ListScreen
import com.slem.gazastor.ui.screens.onboarding.OnboardingScreen
import com.slem.gazastor.ui.screens.orders.OrdersScreen
import com.slem.gazastor.ui.screens.search.SearchScreen
import com.slem.gazastor.ui.screens.splash.SplashScreen
import com.slem.gazastor.ui.screens.splash.StartDestination

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Cart : Screen("cart")
    data object Orders : Screen("orders")
    data object Account : Screen("account")
    data object Checkout : Screen("checkout")
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
    val icon: ImageVector
) {
    Home(Screen.Home.route, R.string.tab_home, Icons.Filled.Home),
    Search(Screen.Search.route, R.string.tab_search, Icons.Filled.Search),
    Cart(Screen.Cart.route, R.string.tab_cart, Icons.Filled.ShoppingCart),
    Orders(Screen.Orders.route, R.string.tab_orders, Icons.AutoMirrored.Filled.List),
    Account(Screen.Account.route, R.string.tab_account, Icons.Filled.Person)
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

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
                                if (!selected) {
                                    navController.navigateToTab(tab.route)
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
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
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onItemClick = { itemId ->
                        navController.navigate(Screen.Detail.createRoute(itemId))
                    }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onBrowseClick = { navController.navigateToTab(Screen.Home.route) },
                    onCheckoutClick = { navController.navigate(Screen.Checkout.route) },
                    onSignInClick = { navController.navigateToTab(Screen.Account.route) }
                )
            }

            composable(Screen.Orders.route) {
                OrdersScreen(
                    onSignInClick = { navController.navigateToTab(Screen.Account.route) }
                )
            }

            composable(Screen.Account.route) {
                AccountScreen(
                    onGuestClick = { navController.navigateToTab(Screen.Home.route) }
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onOrderPlaced = {
                        navController.navigateToTab(Screen.Orders.route)
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
                    onBackClick = { navController.popBackStack() }
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

/** Switches to a bottom-bar tab, preserving each tab's own back stack state. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
