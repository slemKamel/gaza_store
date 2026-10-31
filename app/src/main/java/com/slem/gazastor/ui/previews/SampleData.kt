package com.slem.gazastor.ui.previews

import com.slem.gazastor.ui.model.BannerUi
import com.slem.gazastor.ui.model.CategoryUi
import com.slem.gazastor.ui.model.ItemUi
import com.slem.gazastor.ui.screens.account.AccountUiState
import com.slem.gazastor.ui.screens.account.EditProfileForm
import com.slem.gazastor.ui.screens.auth.AuthError
import com.slem.gazastor.ui.screens.auth.AuthInfo
import com.slem.gazastor.ui.screens.auth.AuthMode
import com.slem.gazastor.ui.screens.auth.AuthUiState
import com.slem.gazastor.ui.screens.cart.CartLineUi
import com.slem.gazastor.ui.screens.cart.CartUiState
import com.slem.gazastor.ui.screens.checkout.CheckoutError
import com.slem.gazastor.ui.screens.checkout.CheckoutUiState
import com.slem.gazastor.ui.screens.detail.DetailUiState
import com.slem.gazastor.ui.screens.home.HomeUiState
import com.slem.gazastor.ui.screens.list.ListUiState
import com.slem.gazastor.ui.screens.orders.OrderLineUi
import com.slem.gazastor.ui.screens.orders.OrderUi
import com.slem.gazastor.ui.screens.orders.OrdersUiState
import com.slem.gazastor.ui.screens.payment.PaymentMethod
import com.slem.gazastor.ui.screens.payment.PaymentUiState
import com.slem.gazastor.ui.screens.payment.WalletProvider
import com.slem.gazastor.ui.screens.search.SearchUiState

/**
 * Fake, offline friendly content for the `@Preview` functions. Images point at
 * a bundled drawable (Coil loads `android.resource://` URIs in previews).
 */
private const val PREVIEW_IMAGE = "android.resource://com.slem.gazastor/drawable/preview_image"

object SampleData {

    private fun item(
        id: String,
        categoryId: String,
        title: String,
        description: String,
        price: String,
        priceValue: Double,
        iconKey: String = "",
        imageUrl: String = PREVIEW_IMAGE
    ) = ItemUi(
        id = id,
        categoryId = categoryId,
        title = title,
        description = description,
        // `price` is the bare formatted number; PriceText adds "السعر: … ₪".
        price = price,
        priceValue = priceValue,
        imageUrl = imageUrl,
        images = if (imageUrl.isBlank()) emptyList() else listOf(imageUrl, imageUrl, imageUrl),
        iconKey = iconKey
    )

    // "عطور" carries an icon key (glyph chip) and its product ships without a
    // photo, so the previews show both the photo and the glyph fallback the real
    // catalog now relies on (see the "Product images" note in AGENTS.md).
    val categories = listOf(
        CategoryUi("c1", "ساعات", "watches", PREVIEW_IMAGE),
        CategoryUi("c2", "حقائب", "bags", PREVIEW_IMAGE),
        CategoryUi("c3", "عطور", "perfumes", PREVIEW_IMAGE, "personal_care"),
        CategoryUi("c4", "نظارات", "glasses", PREVIEW_IMAGE)
    )

    // One banner per drawn style (green / beige / copper), mirroring the live rows.
    val banners = listOf(
        BannerUi(
            id = "b1",
            title = "تشكيلة الصيف",
            imageUrl = PREVIEW_IMAGE,
            link = "",
            style = "green",
            subtitle = "أسعار مخفضة على تشكيلة مختارة",
            badge = "جديد",
            iconKey = "fresh"
        ),
        BannerUi(
            id = "b2",
            title = "وصل حديثاً",
            imageUrl = PREVIEW_IMAGE,
            link = "",
            style = "beige",
            subtitle = "اكتشف أحدث المنتجات في المتجر",
            badge = "عرض خاص",
            iconKey = "offer"
        ),
        BannerUi(
            id = "b3",
            title = "توصيل سريع حتى باب بيتك",
            imageUrl = "",
            link = "",
            style = "copper",
            subtitle = "خدمة توصيل موثوقة في كل وقت",
            badge = "توصيل",
            iconKey = "delivery"
        )
    )

    val items = listOf(
        item("i1", "c1", "ساعة يد كلاسيكية", "ساعة أنيقة بحزام جلد طبيعي ومقاومة للماء.", "250", 250.0),
        item("i2", "c2", "حقيبة جلدية فاخرة", "حقيبة يد بجلد طبيعي وحواف مخيطة يدوياً.", "480", 480.0),
        item(
            "i3", "c3", "عطر شرقي", "عطر بثبات طويل ولمسة خشبية دافئة.", "320", 320.0,
            iconKey = "personal_care", imageUrl = ""
        ),
        item("i4", "c4", "نظارة شمسية", "نظارة بعدسات مستقطبة وحماية كاملة من الشمس.", "150", 150.0)
    )

    val cartLines = listOf(
        CartLineUi(
            itemId = "i1",
            title = "ساعة يد كلاسيكية",
            imageUrl = PREVIEW_IMAGE,
            unitPrice = 250.0,
            quantity = 2,
            lineTotal = 500.0
        ),
        CartLineUi(
            itemId = "i2",
            title = "حقيبة جلدية فاخرة",
            imageUrl = PREVIEW_IMAGE,
            unitPrice = 480.0,
            quantity = 1,
            lineTotal = 480.0
        )
    )

    private val orderLines = listOf(
        OrderLineUi(title = "ساعة يد كلاسيكية", quantity = 1, lineTotal = 250.0),
        OrderLineUi(title = "عطر شرقي", quantity = 2, lineTotal = 640.0)
    )

    val orderList = listOf(
        OrderUi("ord-00000001", "00000001", "pending", 900.0, "2026-10-10", orderLines),
        OrderUi("ord-00000002", "00000002", "confirmed", 480.0, "2026-10-08", orderLines.take(1)),
        OrderUi("ord-00000003", "00000003", "shipped", 320.0, "2026-10-05", orderLines.take(1)),
        OrderUi("ord-00000004", "00000004", "delivered", 150.0, "2026-09-28", orderLines.take(1)),
        OrderUi("ord-00000005", "00000005", "cancelled", 1_130.0, "2026-09-20", emptyList())
    )

    // --- Screen states -----------------------------------------------------

    val home = HomeUiState(
        categories = categories,
        banners = banners,
        recentItems = items
    )

    val list = ListUiState(categoryName = "ساعات", items = items)

    val search = SearchUiState(
        query = "ساعة",
        hasSearched = true,
        results = items
    )

    val detail = DetailUiState(item = items.first())

    val cart = CartUiState(
        isLoading = false,
        lines = cartLines,
        itemCount = 3,
        subtotal = 980.0,
        deliveryFee = 10.0,
        total = 990.0,
        isSignedIn = true
    )

    val checkout = CheckoutUiState(
        customerName = "أحمد محمود",
        phone = "0599000000",
        address = "غزة - شارع عمر المختار، عمارة رقم 5",
        notes = "الرجاء التوصيل بعد العصر",
        isCartEmpty = false,
        isSignedIn = true,
        subtotal = 980.0,
        deliveryFee = 10.0,
        total = 990.0,
        itemCount = 3
    )

    val orders = OrdersUiState(isSignedIn = true, orders = orderList)

    val payment = PaymentUiState()

    val paymentCard = PaymentUiState(
        method = PaymentMethod.Card,
        cardNumber = "4111111111111111",
        cardExpiry = "1229",
        cardCvv = "123",
        cardHolder = "أحمد محمود"
    )

    val paymentWallet = PaymentUiState(
        method = PaymentMethod.Wallet,
        wallet = WalletProvider.JawwalPay,
        walletPhone = "0599000000"
    )

    val account = AccountUiState(
        isSignedIn = true,
        email = "admin@gazastor.ps",
        displayName = "أحمد محمود",
        phone = "0599123456",
        memberSince = "2026/03",
        role = "admin",
        ordersCount = 3,
        favoritesCount = 5,
        cartCount = 2
    )

    val accountEditForm = EditProfileForm(
        fullName = account.displayName.orEmpty(),
        phone = account.phone.orEmpty()
    )

    val auth = AuthUiState(
        mode = AuthMode.SignIn,
        email = "user@example.com",
        password = "secret123"
    )

    // --- State variants ----------------------------------------------------

    val emptyHome = HomeUiState()
    val errorHome = HomeUiState(isError = true)
    val loadingHome = HomeUiState(isLoading = true)

    val emptyCart = CartUiState(isLoading = false)
    val guestCart = cart.copy(isSignedIn = false)

    val guestOrders = OrdersUiState(isSignedIn = false)
    val emptyOrders = OrdersUiState(isSignedIn = true)
    val errorOrders = OrdersUiState(isSignedIn = true, isError = true)

    val customerAccount = account.copy(role = "customer")

    val authError = AuthUiState(
        mode = AuthMode.SignIn,
        email = "not-an-email",
        error = AuthError.InvalidEmail
    )
    val authInfo = AuthUiState(
        mode = AuthMode.SignUp,
        email = "user@example.com",
        info = AuthInfo.ConfirmationEmailSent
    )
    val checkoutError = checkout.copy(error = CheckoutError.PhoneRequired)
}
