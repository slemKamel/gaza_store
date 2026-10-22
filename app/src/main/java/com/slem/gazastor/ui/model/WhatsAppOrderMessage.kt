package com.slem.gazastor.ui.model

import java.util.Locale

/**
 * Builds the pre-filled WhatsApp message for a placed order.
 *
 * The message is in Arabic, uses ₪ for currency and Western digits (the
 * format locale is pinned, so an Arabic device locale cannot swap in
 * Arabic-Indic digits), truncates to the first 15 items, and keeps the text
 * under ~2000 characters so the `wa.me` link stays manageable.
 */
data class WhatsAppOrderItem(
    val title: String,
    val quantity: Int,
    val lineTotal: Double
)

object WhatsAppOrderMessage {

    private const val MAX_ITEMS = 15
    private const val MAX_MESSAGE_LENGTH = 2000

    private const val HEADER_TEMPLATE = "طلب جديد رقم %s\n"
    private const val CUSTOMER_TEMPLATE = "العميل: %s\n"
    private const val ADDRESS_TEMPLATE = "العنوان: %s\n"
    private const val PHONE_TEMPLATE = "الهاتف: %s\n"
    private const val ITEMS_HEADER = "المنتجات:\n"
    private const val ITEM_TEMPLATE = "- %s ×%d = %.2f ₪\n"
    private const val OTHER_ITEMS_TEMPLATE = "و %d أصناف أخرى\n"
    private const val SUBTOTAL_TEMPLATE = "المجموع الفرعي: %.2f ₪\n"
    private const val DELIVERY_TEMPLATE = "رسوم التوصيل: %.2f ₪\n"
    private const val TOTAL_TEMPLATE = "الإجمالي: %.2f ₪\n"
    private const val PAYMENT_TEMPLATE = "طريقة الدفع: %s"

    /** Formats a template with [Locale.US] so digits stay Western everywhere. */
    private fun String.template(vararg args: Any?): String =
        String.format(Locale.US, this, *args)

    /**
     * Builds the WhatsApp message text.
     *
     * @param orderId order number or null
     * @param customerName customer full name
     * @param address delivery address
     * @param phone customer phone or null
     * @param items list of order items (title, quantity, lineTotal)
     * @param subtotal order subtotal
     * @param deliveryFee delivery fee
     * @param total order total
     * @param paymentLabel payment method label (e.g., "طلب عبر واتساب")
     * @return formatted message ready for URI encoding
     */
    fun build(
        orderId: String?,
        customerName: String,
        address: String,
        phone: String?,
        items: List<WhatsAppOrderItem>,
        subtotal: Double,
        deliveryFee: Double,
        total: Double,
        paymentLabel: String
    ): String {
        val sb = StringBuilder()

        // Header with order number
        sb.append(HEADER_TEMPLATE.template(orderId ?: "—"))

        // Customer info
        sb.append(CUSTOMER_TEMPLATE.template(customerName))
        sb.append(ADDRESS_TEMPLATE.template(address))
        if (phone != null && phone.isNotBlank()) {
            sb.append(PHONE_TEMPLATE.template(phone))
        }

        sb.append(ITEMS_HEADER)

        // Items (truncated to MAX_ITEMS)
        val displayed = items.take(MAX_ITEMS)
        for (item in displayed) {
            sb.append(ITEM_TEMPLATE.template(
                item.title,
                item.quantity,
                item.lineTotal
            ))
        }

        if (items.size > MAX_ITEMS) {
            sb.append(OTHER_ITEMS_TEMPLATE.template(items.size - MAX_ITEMS))
        }

        // Totals - formatted as numbers so they never depend on the locale
        sb.append(SUBTOTAL_TEMPLATE.template(subtotal))
        sb.append(DELIVERY_TEMPLATE.template(deliveryFee))
        sb.append(TOTAL_TEMPLATE.template(total))

        // Payment method
        sb.append(PAYMENT_TEMPLATE.template(paymentLabel))

        val message = sb.toString().trim()

        // Safety: keep the pre-filled text short enough for a wa.me link
        if (message.length > MAX_MESSAGE_LENGTH) {
            return truncateMessage(items, subtotal, deliveryFee, total, paymentLabel)
        }

        return message
    }

    /**
     * Fallback truncation if the full message exceeds URL limits.
     * Progressively reduces displayed items until under limit.
     */
    private fun truncateMessage(
        items: List<WhatsAppOrderItem>,
        subtotal: Double,
        deliveryFee: Double,
        total: Double,
        paymentLabel: String
    ): String {
        var itemCount = minOf(items.size, MAX_ITEMS)
        while (itemCount > 0) {
            val sb = StringBuilder()
            sb.append(HEADER_TEMPLATE.template("—"))
            sb.append(ITEMS_HEADER)
            items.take(itemCount).forEach { item ->
                sb.append(ITEM_TEMPLATE.template(item.title, item.quantity, item.lineTotal))
            }
            if (itemCount < items.size) {
                sb.append(OTHER_ITEMS_TEMPLATE.template(items.size - itemCount))
            }
            sb.append(SUBTOTAL_TEMPLATE.template(subtotal))
            sb.append(DELIVERY_TEMPLATE.template(deliveryFee))
            sb.append(TOTAL_TEMPLATE.template(total))
            sb.append(PAYMENT_TEMPLATE.template(paymentLabel))
            val truncated = sb.toString().trim()
            if (truncated.length <= MAX_MESSAGE_LENGTH) return truncated
            itemCount--
        }
        // Absolute fallback: just order summary
        return "طلب جديد\nالمجموع الفرعي: %.2f ₪\nرسوم التوصيل: %.2f ₪\nالإجمالي: %.2f ₪\nطريقة الدفع: %s"
            .template(subtotal, deliveryFee, total, paymentLabel)
    }
}