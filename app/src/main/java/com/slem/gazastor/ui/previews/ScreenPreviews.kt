package com.slem.gazastor.ui.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.slem.gazastor.ui.components.EssentialsShowcase
import com.slem.gazastor.ui.screens.account.ProfileContent
import com.slem.gazastor.ui.screens.auth.AuthForm
import com.slem.gazastor.ui.screens.cart.CartContent
import com.slem.gazastor.ui.screens.checkout.CheckoutContent
import com.slem.gazastor.ui.screens.detail.DetailContent
import com.slem.gazastor.ui.screens.home.HomeContent
import com.slem.gazastor.ui.screens.list.ListContent
import com.slem.gazastor.ui.screens.onboarding.OnboardingContent
import com.slem.gazastor.ui.screens.orders.OrdersContent
import com.slem.gazastor.ui.screens.search.SearchContent
import com.slem.gazastor.ui.screens.splash.SplashContent
import com.slem.gazastor.ui.theme.GazaStorTheme

/**
 * Preview gallery for every screen, driven by [SampleData]. Rendered in RTL with
 * the brand theme so the previews match the real Arabic app.
 *
 * Run them from Android Studio's "Preview" tool window (split view), or use the
 * "Preview" icon on each function.
 */

/** Wraps preview content in the brand theme + forced RTL, like `MainActivity`. */
@Composable
internal fun AppPreview(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        GazaStorTheme { content() }
    }
}

internal const val BRAND_BACKGROUND = 0xFFF5F1EA

// --- Splash & onboarding ---------------------------------------------------

@Preview(name = "Splash", locale = "ar", showBackground = true, backgroundColor = BRAND_BACKGROUND)
@Composable
internal fun SplashPreview() {
    AppPreview { SplashContent() }
}

@Preview(name = "Onboarding", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun OnboardingPreview() {
    AppPreview { OnboardingContent(onComplete = {}) }
}

/**
 * The showcase alone, pinned to a frame where the badges have landed in their
 * row. A preview only renders a single frame, and frame 0 is the scattered
 * orbit, so [EssentialsShowcase] takes a progress override for exactly this.
 */
@Preview(name = "Onboarding - Showcase", locale = "ar", widthDp = 393, heightDp = 520)
@Composable
internal fun EssentialsShowcasePreview() {
    AppPreview {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EssentialsShowcase(
                visible = true,
                progressOverride = 0.70f,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }
    }
}

// --- Auth ------------------------------------------------------------------

@Preview(name = "Auth - Sign in", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun AuthSignInPreview() {
    AppPreview { AuthFormPreview(showHeader = true) }
}

@Preview(name = "Auth - Sign up", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun AuthSignUpPreview() {
    AppPreview {
        AuthFormPreview(
            showHeader = true,
            state = SampleData.auth.copy(mode = com.slem.gazastor.ui.screens.auth.AuthMode.SignUp)
        )
    }
}

@Preview(name = "Auth - Error", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun AuthErrorPreview() {
    AppPreview { AuthFormPreview(showHeader = true, state = SampleData.authError) }
}

@Preview(name = "Auth - Info", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun AuthInfoPreview() {
    AppPreview { AuthFormPreview(showHeader = true, state = SampleData.authInfo) }
}

@Composable
internal fun AuthFormPreview(
    state: com.slem.gazastor.ui.screens.auth.AuthUiState = SampleData.auth,
    showHeader: Boolean = false
) {
    AuthForm(
        uiState = state,
        onModeChange = {},
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onTogglePasswordVisibility = {},
        onToggleConfirmPasswordVisibility = {},
        onSubmit = {},
        onGuest = {},
        showHeader = showHeader
    )
}

// --- Home ------------------------------------------------------------------

@Preview(name = "Home", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun HomePreview() {
    AppPreview {
        HomeContent(
            uiState = SampleData.home,
            onCategoryClick = {},
            onItemClick = {},
            onRetry = {},
            animateEntrance = false
        )
    }
}

@Preview(name = "Home - Loading", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun HomeLoadingPreview() {
    AppPreview {
        HomeContent(
            uiState = SampleData.loadingHome,
            onCategoryClick = {},
            onItemClick = {},
            onRetry = {},
            animateEntrance = false
        )
    }
}

@Preview(name = "Home - Empty", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun HomeEmptyPreview() {
    AppPreview {
        HomeContent(
            uiState = SampleData.emptyHome,
            onCategoryClick = {},
            onItemClick = {},
            onRetry = {},
            animateEntrance = false
        )
    }
}

@Preview(name = "Home - Error", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun HomeErrorPreview() {
    AppPreview {
        HomeContent(
            uiState = SampleData.errorHome,
            onCategoryClick = {},
            onItemClick = {},
            onRetry = {},
            animateEntrance = false
        )
    }
}

// --- Category list ---------------------------------------------------------

@Preview(name = "List", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun ListPreview() {
    AppPreview {
        ListContent(
            uiState = SampleData.list,
            onItemClick = {},
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "List - Empty", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun ListEmptyPreview() {
    AppPreview {
        ListContent(
            uiState = SampleData.list.copy(items = emptyList()),
            onItemClick = {},
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "List - Loading", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun ListLoadingPreview() {
    AppPreview {
        ListContent(
            uiState = SampleData.list.copy(isLoading = true, items = emptyList()),
            onItemClick = {},
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "List - Error", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun ListErrorPreview() {
    AppPreview {
        ListContent(
            uiState = SampleData.list.copy(isError = true, items = emptyList()),
            onItemClick = {},
            onBackClick = {},
            onRetry = {}
        )
    }
}

// --- Search ----------------------------------------------------------------

@Preview(name = "Search - Prompt", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun SearchPromptPreview() {
    AppPreview {
        SearchContent(
            uiState = SampleData.search.copy(query = "", hasSearched = false, results = emptyList()),
            onQueryChange = {},
            onItemClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Search - Results", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun SearchResultsPreview() {
    AppPreview {
        SearchContent(
            uiState = SampleData.search,
            onQueryChange = {},
            onItemClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Search - No results", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun SearchEmptyPreview() {
    AppPreview {
        SearchContent(
            uiState = SampleData.search.copy(results = emptyList()),
            onQueryChange = {},
            onItemClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Search - Loading", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun SearchLoadingPreview() {
    AppPreview {
        SearchContent(
            uiState = SampleData.search.copy(isLoading = true),
            onQueryChange = {},
            onItemClick = {},
            onRetry = {}
        )
    }
}

// --- Detail ----------------------------------------------------------------

@Preview(name = "Detail", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun DetailPreview() {
    AppPreview {
        DetailContent(
            uiState = SampleData.detail,
            quantity = 1,
            onQuantityChange = {},
            onAddToCart = { _, _ -> },
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Detail - Loading", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun DetailLoadingPreview() {
    AppPreview {
        DetailContent(
            uiState = SampleData.detail.copy(isLoading = true, item = null),
            quantity = 1,
            onQuantityChange = {},
            onAddToCart = { _, _ -> },
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Detail - Not found", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun DetailNotFoundPreview() {
    AppPreview {
        DetailContent(
            uiState = SampleData.detail.copy(item = null),
            quantity = 1,
            onQuantityChange = {},
            onAddToCart = { _, _ -> },
            onBackClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Detail - Error", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun DetailErrorPreview() {
    AppPreview {
        DetailContent(
            uiState = SampleData.detail.copy(isError = true, item = null),
            quantity = 1,
            onQuantityChange = {},
            onAddToCart = { _, _ -> },
            onBackClick = {},
            onRetry = {}
        )
    }
}

// --- Cart ------------------------------------------------------------------

@Preview(name = "Cart", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun CartPreview() {
    AppPreview {
        CartContent(
            uiState = SampleData.cart,
            onBrowseClick = {},
            onCheckoutClick = {},
            onSignInClick = {},
            onIncrease = {},
            onDecrease = {},
            onRemove = {}
        )
    }
}

@Preview(name = "Cart - Guest", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun CartGuestPreview() {
    AppPreview {
        CartContent(
            uiState = SampleData.guestCart,
            onBrowseClick = {},
            onCheckoutClick = {},
            onSignInClick = {},
            onIncrease = {},
            onDecrease = {},
            onRemove = {}
        )
    }
}

@Preview(name = "Cart - Empty", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun CartEmptyPreview() {
    AppPreview {
        CartContent(
            uiState = SampleData.emptyCart,
            onBrowseClick = {},
            onCheckoutClick = {},
            onSignInClick = {},
            onIncrease = {},
            onDecrease = {},
            onRemove = {}
        )
    }
}

// --- Checkout --------------------------------------------------------------

@Preview(name = "Checkout", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun CheckoutPreview() {
    AppPreview {
        CheckoutContent(
            uiState = SampleData.checkout,
            onBackClick = {},
            onCustomerNameChange = {},
            onPhoneChange = {},
            onAddressChange = {},
            onNotesChange = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "Checkout - Error", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun CheckoutErrorPreview() {
    AppPreview {
        CheckoutContent(
            uiState = SampleData.checkoutError,
            onBackClick = {},
            onCustomerNameChange = {},
            onPhoneChange = {},
            onAddressChange = {},
            onNotesChange = {},
            onSubmit = {}
        )
    }
}

@Preview(name = "Checkout - Empty cart", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun CheckoutEmptyPreview() {
    AppPreview {
        CheckoutContent(
            uiState = SampleData.checkout.copy(isCartEmpty = true),
            onBackClick = {},
            onCustomerNameChange = {},
            onPhoneChange = {},
            onAddressChange = {},
            onNotesChange = {},
            onSubmit = {}
        )
    }
}

// --- Orders ----------------------------------------------------------------

@Preview(name = "Orders", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun OrdersPreview() {
    AppPreview {
        OrdersContent(
            uiState = SampleData.orders,
            onSignInClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Orders - Guest", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun OrdersGuestPreview() {
    AppPreview {
        OrdersContent(
            uiState = SampleData.guestOrders,
            onSignInClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Orders - Empty", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun OrdersEmptyPreview() {
    AppPreview {
        OrdersContent(
            uiState = SampleData.emptyOrders,
            onSignInClick = {},
            onRetry = {}
        )
    }
}

@Preview(name = "Orders - Error", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun OrdersErrorPreview() {
    AppPreview {
        OrdersContent(
            uiState = SampleData.errorOrders,
            onSignInClick = {},
            onRetry = {}
        )
    }
}

// --- Account ---------------------------------------------------------------

@Preview(name = "Account - Admin", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun AccountAdminPreview() {
    AppPreview { ProfileContent(uiState = SampleData.account, onSignOut = {}) }
}

@Preview(name = "Account - Customer", locale = "ar", showSystemUi = true, widthDp = 393, heightDp = 851)
@Composable
internal fun AccountCustomerPreview() {
    AppPreview { ProfileContent(uiState = SampleData.customerAccount, onSignOut = {}) }
}
