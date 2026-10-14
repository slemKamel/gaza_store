/**
 * Shared domain constants, mirroring the Android app and
 * `supabase/schema.sql`. Keep in sync with the database CHECK constraints.
 */

export const ORDER_STATUSES = [
  "pending",
  "confirmed",
  "shipped",
  "delivered",
  "cancelled",
] as const;

const ORDER_STATUS_LABELS: Record<string, string> = {
  pending: "قيد المعالجة",
  confirmed: "مؤكد",
  shipped: "قيد التوصيل",
  delivered: "تم التسليم",
  cancelled: "ملغي",
};

export function orderStatusLabel(status: string): string {
  return ORDER_STATUS_LABELS[status] ?? status;
}

export const PAYMENT_STATUSES = ["unpaid", "paid", "refunded"] as const;

const PAYMENT_STATUS_LABELS: Record<string, string> = {
  unpaid: "غير مدفوع",
  paid: "مدفوع",
  refunded: "مسترجع",
};

export function paymentStatusLabel(status: string): string {
  return PAYMENT_STATUS_LABELS[status] ?? status;
}

export const PAYMENT_METHOD_LABELS: Record<string, string> = {
  cod: "الدفع عند الاستلام",
  online: "دفع إلكتروني",
};

export function paymentMethodLabel(method: string): string {
  return PAYMENT_METHOD_LABELS[method] ?? method;
}

/** Public Supabase Storage bucket that holds product/banner/category images. */
export const PRODUCT_IMAGES_BUCKET = "product-images";

/** Max upload size accepted by the dashboard image uploader (5 MB). */
export const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
