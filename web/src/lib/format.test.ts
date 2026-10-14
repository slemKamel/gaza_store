import { describe, expect, it } from "vitest";
import {
  formatCurrency,
  formatDateTime,
  formatOrderDate,
  formatPriceValue,
} from "./format";

describe("formatPriceValue", () => {
  it("adds thousands separators", () => {
    expect(formatPriceValue(1500)).toBe("1,500");
  });

  it("keeps decimals without trailing zeros", () => {
    expect(formatPriceValue(12.5)).toBe("12.5");
    expect(formatPriceValue(12)).toBe("12");
  });

  it("rounds to two decimals", () => {
    expect(formatPriceValue(1234.567)).toBe("1,234.57");
  });

  it("falls back to 0 for non-finite values", () => {
    expect(formatPriceValue(Number.NaN)).toBe("0");
    expect(formatPriceValue(Number.POSITIVE_INFINITY)).toBe("0");
  });
});

describe("formatCurrency", () => {
  it("appends the shekel symbol", () => {
    expect(formatCurrency(10)).toBe("10 ₪");
    expect(formatCurrency(1500)).toBe("1,500 ₪");
  });
});

describe("formatOrderDate", () => {
  it("formats an ISO timestamp as yyyy/MM/dd", () => {
    expect(formatOrderDate("2026-10-14T22:30:00+00:00")).toBe("2026/10/14");
  });

  it("returns an empty string for blank input", () => {
    expect(formatOrderDate(null)).toBe("");
    expect(formatOrderDate("")).toBe("");
  });

  it("falls back to the raw prefix when unparseable", () => {
    expect(formatOrderDate("not-a-date")).toBe("not-a-date");
  });
});

describe("formatDateTime", () => {
  it("appends the stored time", () => {
    expect(formatDateTime("2026-10-14T22:30:00+00:00")).toBe(
      "2026/10/14 22:30",
    );
  });

  it("returns just the date when there is no time", () => {
    expect(formatDateTime("2026-10-14")).toBe("2026/10/14");
  });
});
