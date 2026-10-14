import { describe, expect, it } from "vitest";
import {
  ORDER_STATUSES,
  orderStatusLabel,
  paymentMethodLabel,
  paymentStatusLabel,
} from "./constants";

describe("order statuses", () => {
  it("exposes the values allowed by the database", () => {
    expect(ORDER_STATUSES).toEqual([
      "pending",
      "confirmed",
      "shipped",
      "delivered",
      "cancelled",
    ]);
  });

  it("labels statuses in Arabic", () => {
    expect(orderStatusLabel("pending")).toBe("قيد المعالجة");
    expect(orderStatusLabel("shipped")).toBe("قيد التوصيل");
    expect(orderStatusLabel("delivered")).toBe("تم التسليم");
  });

  it("passes unknown statuses through", () => {
    expect(orderStatusLabel("unknown")).toBe("unknown");
  });
});

describe("payment labels", () => {
  it("labels payment status", () => {
    expect(paymentStatusLabel("paid")).toBe("مدفوع");
    expect(paymentStatusLabel("refunded")).toBe("مسترجع");
  });

  it("labels payment method", () => {
    expect(paymentMethodLabel("cod")).toBe("الدفع عند الاستلام");
  });
});
