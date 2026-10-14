import { describe, expect, it } from "vitest";
import {
  PAGE_SIZE,
  escapeLike,
  pageRange,
  parsePage,
  queryString,
} from "./query";

describe("escapeLike", () => {
  it("escapes PostgREST ilike wildcards", () => {
    expect(escapeLike("50%_off")).toBe("50\\%\\_off");
  });

  it("escapes backslashes first", () => {
    expect(escapeLike("a\\b")).toBe("a\\\\b");
  });

  it("leaves plain text untouched", () => {
    expect(escapeLike("هاتف")).toBe("هاتف");
  });
});

describe("parsePage", () => {
  it("defaults to page 1", () => {
    expect(parsePage(undefined)).toBe(1);
    expect(parsePage("")).toBe(1);
  });

  it("parses valid page numbers", () => {
    expect(parsePage("3")).toBe(3);
    expect(parsePage(["5"])).toBe(5);
  });

  it("rejects zero, negatives and garbage", () => {
    expect(parsePage("0")).toBe(1);
    expect(parsePage("-2")).toBe(1);
    expect(parsePage("abc")).toBe(1);
  });
});

describe("pageRange", () => {
  it("computes inclusive bounds", () => {
    expect(pageRange(1)).toEqual({ from: 0, to: PAGE_SIZE - 1 });
    expect(pageRange(2)).toEqual({ from: PAGE_SIZE, to: PAGE_SIZE * 2 - 1 });
  });
});

describe("queryString", () => {
  it("normalizes query values", () => {
    expect(queryString(undefined)).toBe("");
    expect(queryString("a")).toBe("a");
    expect(queryString(["b", "c"])).toBe("b");
  });
});
