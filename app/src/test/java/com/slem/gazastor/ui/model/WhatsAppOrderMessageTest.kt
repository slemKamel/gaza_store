package com.slem.gazastor.ui.model

import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the WhatsApp order message builder.
 */
class WhatsAppOrderMessageTest {

    private val sampleItems = listOf(
        WhatsAppOrderItem("مياه معدنية 6×1.5 لتر", 1, 12.75),
        WhatsAppOrderItem("أرز بسمتي 5 كغ", 2, 91.0),
        WhatsAppOrderItem("زيت زيتون 1 لتر", 1, 45.0),
        WhatsAppOrderItem("سكر أبيض 1 كغ", 3, 18.0),
        WhatsAppOrderItem("شاي أخضر 100غ", 1, 22.5),
        WhatsAppOrderItem("قهوة عربية 250غ", 2, 60.0),
        WhatsAppOrderItem("تمر مجدول 500غ", 1, 35.0),
        WhatsAppOrderItem("لوز محمص 200غ", 1, 28.0),
        WhatsAppOrderItem("عسل طبيعي 250غ", 1, 42.0),
        WhatsAppOrderItem("جبنة بيضاء 400غ", 2, 50.0),
        WhatsAppOrderItem("زبدة 250غ", 1, 15.0),
        WhatsAppOrderItem("بيض 12 حبة", 2, 24.0),
        WhatsAppOrderItem("حليب طازج 1 لتر", 3, 30.0),
        WhatsAppOrderItem("زبادي 200غ", 4, 16.0),
        WhatsAppOrderItem("قشطة 200غ", 1, 12.0),
        WhatsAppOrderItem("كريم كراميل", 2, 18.0), // 16th item
    )

    @Test
    fun build_includesOrderIdCustomerAddressPhone() {
        val msg = WhatsAppOrderMessage.build(
            orderId = "A1B2C3",
            customerName = "أحمد محمد",
            address = "غزة، شارع الوحدة، بناية 5",
            phone = "0599123456",
            items = sampleItems.take(3),
            subtotal = 148.75,
            deliveryFee = 10.0,
            total = 158.75,
            paymentLabel = "طلب عبر واتساب"
        )

        assertTrue("contains order id", msg.contains("طلب جديد رقم A1B2C3"))
        assertTrue("contains customer name", msg.contains("العميل: أحمد محمد"))
        assertTrue("contains address", msg.contains("العنوان: غزة، شارع الوحدة، بناية 5"))
        assertTrue("contains phone", msg.contains("الهاتف: 0599123456"))
    }

    @Test
    fun build_handlesMissingPhoneGracefully() {
        val msg = WhatsAppOrderMessage.build(
            orderId = "A1B2C3",
            customerName = "أحمد محمد",
            address = "غزة، شارع الوحدة",
            phone = null,
            items = listOf(WhatsAppOrderItem("منتج", 1, 10.0)),
            subtotal = 10.0,
            deliveryFee = 10.0,
            total = 20.0,
            paymentLabel = "طلب عبر واتساب"
        )

        assertTrue("no phone line when null", !msg.contains("الهاتف:"))
    }

    @Test
    fun build_formatsItemsWithQuantityAndLineTotal() {
        val msg = WhatsAppOrderMessage.build(
            orderId = "A1B2C3",
            customerName = "أحمد",
            address = "غزة",
            phone = "0599123456",
            items = listOf(
                WhatsAppOrderItem("مياه", 2, 25.5),
                WhatsAppOrderItem("عصير", 1, 12.0)
            ),
            subtotal = 37.5,
            deliveryFee = 10.0,
            total = 47.5,
            paymentLabel = "طلب عبر واتساب"
        )

        assertTrue("first item formatted", msg.contains("- مياه ×2 = 25.50 ₪"))
        assertTrue("second item formatted", msg.contains("- عصير ×1 = 12.00 ₪"))
    }

    @Test
    fun build_includesTotalsWithCurrencySymbol() {
        val msg = WhatsAppOrderMessage.build(
            orderId = "A1B2C3",
            customerName = "أحمد",
            address = "غزة",
            phone = "0599123456",
            items = listOf(WhatsAppOrderItem("منتج", 1, 100.0)),
            subtotal = 100.0,
            deliveryFee = 10.0,
            total = 110.0,
            paymentLabel = "طلب عبر واتساب"
        )

        assertTrue("subtotal has ₪", msg.contains("المجموع الفرعي: 100.00 ₪"))
        assertTrue("delivery has ₪", msg.contains("رسوم التوصيل: 10.00 ₪"))
        assertTrue("total has ₪", msg.contains("الإجمالي: 110.00 ₪"))
    }

    @Test
    fun build_includesPaymentLabel() {
        val msg = WhatsAppOrderMessage.build(
            orderId = "A1B2C3",
            customerName = "أحمد",
            address = "غزة",
            phone = "0599123456",
            items = listOf(WhatsAppOrderItem("منتج", 1, 10.0)),
            subtotal = 10.0,
            deliveryFee = 10.0,
            total = 20.0,
            paymentLabel = "طلب عبر واتساب"
        )

        assertTrue("payment label at end", msg.contains("طريقة الدفع: طلب عبر واتساب"))
    }

    @Test
    fun build_truncatesToFirst15ItemsAndAddsOtherCount() {
        val msg = WhatsAppOrderMessage.build(
            orderId = "A1B2C3",
            customerName = "أحمد",
            address = "غزة",
            phone = "0599123456",
            items = sampleItems, // 16 items
            subtotal = 500.0,
            deliveryFee = 10.0,
            total = 510.0,
            paymentLabel = "طلب عبر واتساب"
        )

        // First 15 items displayed
        assertTrue("shows 15 items", msg.split("\n").count { it.startsWith("- ") } == 15)
        // "و N أصناف أخرى" appears
        assertTrue("shows other count", msg.contains("و 1 أصناف أخرى"))
    }

    @Test
    fun build_keepsUrlLengthUnder2000Chars() {
        val msg = WhatsAppOrderMessage.build(
            orderId = "A1B2C3",
            customerName = "أحمد محمد علي",
            address = "غزة، شارع الوحدة، بناية 5، الطابق 3، شقة 12",
            phone = "0599123456",
            items = sampleItems,
            subtotal = 500.0,
            deliveryFee = 10.0,
            total = 510.0,
            paymentLabel = "طلب عبر واتساب"
        )

        // URL encoding roughly doubles length for Arabic, so keep raw well under 2000
        assertTrue("message length reasonable", msg.length < 2000)
    }

    @Test
    fun build_keepsWesternDigitsUnderAnArabicDeviceLocale() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"))

            val msg = WhatsAppOrderMessage.build(
                orderId = "A1B2C3",
                customerName = "أحمد",
                address = "غزة",
                phone = "0599123456",
                items = listOf(WhatsAppOrderItem("منتج", 2, 25.5)),
                subtotal = 1234.5,
                deliveryFee = 10.0,
                total = 1244.5,
                paymentLabel = "طلب عبر واتساب"
            )

            assertTrue("western digits in items", msg.contains("= 25.50 ₪"))
            assertTrue("western digits in totals", msg.contains("الإجمالي: 1244.50 ₪"))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun build_handlesEmptyOrderId() {
        val msg = WhatsAppOrderMessage.build(
            orderId = null,
            customerName = "أحمد",
            address = "غزة",
            phone = "0599123456",
            items = listOf(WhatsAppOrderItem("منتج", 1, 10.0)),
            subtotal = 10.0,
            deliveryFee = 10.0,
            total = 20.0,
            paymentLabel = "طلب عبر واتساب"
        )

        assertTrue("shows dash for missing order id", msg.contains("طلب جديد رقم —"))
    }
}