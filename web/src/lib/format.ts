const priceFormatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 2,
});

/**
 * Formats an amount the way the Android app does (`formatPriceValue`):
 * Western digits with thousands separators and no trailing zeros —
 * 1500 -> "1,500", 12.5 -> "12.5".
 */
export function formatPriceValue(value: number): string {
  if (!Number.isFinite(value)) return "0";
  return priceFormatter.format(value);
}

/** Amount with the Shekel suffix, e.g. "1,500 ₪". */
export function formatCurrency(value: number): string {
  return `${formatPriceValue(value)} ₪`;
}

/**
 * Formats a PostgREST timestamp as `yyyy/MM/dd`, mirroring
 * `com.slem.gazastor.ui.model.formatOrderDate`. The date is taken from the
 * stored offset so it never shifts with the viewer's timezone.
 */
export function formatOrderDate(raw: string | null | undefined): string {
  if (!raw) return "";
  const date = raw.slice(0, 10);
  return /^\d{4}-\d{2}-\d{2}$/.test(date) ? date.replace(/-/g, "/") : date;
}

/** `yyyy/MM/dd HH:mm` for order detail views. */
export function formatDateTime(raw: string | null | undefined): string {
  if (!raw) return "";
  const date = formatOrderDate(raw);
  const time = raw.slice(11, 16);
  return /^\d{2}:\d{2}$/.test(time) ? `${date} ${time}` : date;
}
