import Link from "next/link";
import type { Metadata } from "next";
import { createClient } from "@/lib/supabase/server";
import { PageHeader } from "@/components/admin/page-header";
import { ErrorNotice } from "@/components/admin/error-notice";
import { Pagination } from "@/components/admin/pagination";
import {
  OrderStatusBadge,
  PaymentStatusBadge,
} from "@/components/admin/order-status-badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { formatCurrency, formatOrderDate } from "@/lib/format";
import { ORDER_STATUSES, orderStatusLabel } from "@/lib/constants";
import { pageRange, parsePage, queryString } from "@/lib/query";

export const metadata: Metadata = { title: "الطلبات" };

const SELECT =
  "id, customer_name, phone, total, status, payment_status, created_at";

/** Free-text search across customer name and phone (comma/paren-safe). */
function sanitizeTerm(value: string): string {
  return value.replace(/[,()%_\\]/g, "").trim();
}

export default async function OrdersPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const q = queryString(params.q).trim();
  const status = queryString(params.status);
  const page = parsePage(params.page);
  const { from, to } = pageRange(page);

  const supabase = await createClient();

  let query = supabase
    .from("orders")
    .select(SELECT, { count: "exact" })
    .order("created_at", { ascending: false })
    .range(from, to);

  if (status) query = query.eq("status", status);
  const term = sanitizeTerm(q);
  if (term) {
    query = query.or(
      `customer_name.ilike.%${term}%,phone.ilike.%${term}%`,
    );
  }

  const { data: orders, count, error } = await query;

  const total = count ?? 0;
  const hasNext = total > to + 1;

  const filters: Record<string, string> = {};
  if (q) filters.q = q;
  if (status) filters.status = status;

  return (
    <>
      <PageHeader
        title="الطلبات"
        description="متابعة الطلبات وتحديث حالتها."
      />

      <form action="/orders" className="mb-4 flex flex-wrap items-center gap-2">
        <input
          type="search"
          name="q"
          defaultValue={q}
          placeholder="ابحث باسم العميل أو الهاتف…"
          className="h-9 w-full max-w-xs rounded-md border border-input bg-card px-3 text-sm outline-none focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30"
        />
        <select
          name="status"
          defaultValue={status}
          className="h-9 rounded-md border border-input bg-card px-3 text-sm outline-none focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30"
        >
          <option value="">كل الحالات</option>
          {ORDER_STATUSES.map((value) => (
            <option key={value} value={value}>
              {orderStatusLabel(value)}
            </option>
          ))}
        </select>
        <button
          type="submit"
          className="h-9 rounded-md border border-border px-4 text-sm font-medium transition-colors hover:bg-accent"
        >
          بحث
        </button>
        {q || status ? (
          <Link
            href="/orders"
            className="h-9 rounded-md px-3 text-sm leading-9 text-muted-foreground hover:underline"
          >
            مسح
          </Link>
        ) : null}
      </form>

      {error ? (
        <ErrorNotice message={error.message} />
      ) : (
        <>
          <div className="overflow-hidden rounded-xl border border-border bg-card">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>الطلب</TableHead>
                  <TableHead>العميل</TableHead>
                  <TableHead>الهاتف</TableHead>
                  <TableHead>التاريخ</TableHead>
                  <TableHead>الإجمالي</TableHead>
                  <TableHead>الحالة</TableHead>
                  <TableHead>الدفع</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {orders && orders.length > 0 ? (
                  orders.map((order) => (
                    <TableRow key={order.id}>
                      <TableCell>
                        <Link
                          href={`/orders/${order.id}`}
                          className="font-mono text-xs font-medium hover:underline"
                          dir="ltr"
                        >
                          #{order.id.slice(0, 8)}
                        </Link>
                      </TableCell>
                      <TableCell className="font-medium">
                        {order.customer_name}
                      </TableCell>
                      <TableCell className="text-muted-foreground" dir="ltr">
                        {order.phone || "—"}
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {formatOrderDate(order.created_at)}
                      </TableCell>
                      <TableCell>{formatCurrency(order.total)}</TableCell>
                      <TableCell>
                        <OrderStatusBadge status={order.status} />
                      </TableCell>
                      <TableCell>
                        <PaymentStatusBadge status={order.payment_status} />
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell
                      colSpan={7}
                      className="py-10 text-center text-muted-foreground"
                    >
                      {q || status
                        ? "لا توجد نتائج مطابقة."
                        : "لا توجد طلبات بعد."}
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>

          <Pagination
            page={page}
            prevHref={
              page > 1
                ? {
                    pathname: "/orders",
                    query: { ...filters, page: String(page - 1) },
                  }
                : null
            }
            nextHref={
              hasNext
                ? {
                    pathname: "/orders",
                    query: { ...filters, page: String(page + 1) },
                  }
                : null
            }
          />
        </>
      )}
    </>
  );
}
