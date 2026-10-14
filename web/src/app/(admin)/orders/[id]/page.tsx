import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowRight } from "lucide-react";
import { createClient } from "@/lib/supabase/server";
import { PageHeader } from "@/components/admin/page-header";
import { ErrorNotice } from "@/components/admin/error-notice";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { paymentMethodLabel } from "@/lib/constants";
import { OrderStatusForm } from "./order-status-form";
import { updateOrder } from "../actions";

export default async function OrderDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const supabase = await createClient();

  const [orderResult, itemsResult] = await Promise.all([
    supabase
      .from("orders")
      .select(
        "id, user_id, status, payment_method, payment_status, payment_ref, subtotal, delivery_fee, total, customer_name, phone, address, notes, created_at, updated_at",
      )
      .eq("id", id)
      .maybeSingle(),
    supabase
      .from("order_items")
      .select("id, title, unit_price, quantity, line_total")
      .eq("order_id", id)
      .order("created_at", { ascending: true }),
  ]);

  if (orderResult.error) return <ErrorNotice message={orderResult.error.message} />;
  const order = orderResult.data;
  if (!order) notFound();

  const lines = itemsResult.data ?? [];

  return (
    <>
      <PageHeader
        title={`طلب #${order.id.slice(0, 8)}`}
        description={formatDateTime(order.created_at)}
        action={
          <Link
            href="/orders"
            className="inline-flex h-9 items-center gap-1 rounded-md border border-border px-3 text-sm font-medium transition-colors hover:bg-accent"
          >
            <ArrowRight className="size-4" />
            كل الطلبات
          </Link>
        }
      />

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="flex flex-col gap-6 lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>المنتجات</CardTitle>
            </CardHeader>
            <CardContent className="px-0 pb-0">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>المنتج</TableHead>
                    <TableHead>سعر الوحدة</TableHead>
                    <TableHead>الكمية</TableHead>
                    <TableHead>الإجمالي</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {lines.length > 0 ? (
                    lines.map((line) => (
                      <TableRow key={line.id}>
                        <TableCell className="font-medium">
                          {line.title}
                        </TableCell>
                        <TableCell>{formatCurrency(line.unit_price)}</TableCell>
                        <TableCell>{line.quantity}</TableCell>
                        <TableCell>
                          {formatCurrency(line.line_total ?? 0)}
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell
                        colSpan={4}
                        className="py-8 text-center text-muted-foreground"
                      >
                        لا توجد بنود لهذا الطلب.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>

              <div className="flex flex-col gap-2 border-t border-border p-5 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">المجموع الفرعي</span>
                  <span>{formatCurrency(order.subtotal)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">رسوم التوصيل</span>
                  <span>{formatCurrency(order.delivery_fee)}</span>
                </div>
                <div className="flex justify-between border-t border-border pt-2 text-base font-semibold">
                  <span>الإجمالي</span>
                  <span className="text-brand-copper">
                    {formatCurrency(order.total)}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>بيانات التوصيل</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-3 text-sm">
              <Row label="الاسم" value={order.customer_name} />
              <Row
                label="الهاتف"
                value={order.phone || "—"}
                ltr
              />
              <Row label="العنوان" value={order.address || "—"} />
              <Row label="ملاحظات" value={order.notes || "—"} />
            </CardContent>
          </Card>
        </div>

        <div className="flex flex-col gap-6">
          <Card>
            <CardHeader>
              <CardTitle>تحديث الحالة</CardTitle>
            </CardHeader>
            <CardContent>
              <OrderStatusForm
                orderId={order.id}
                status={order.status}
                paymentStatus={order.payment_status}
                action={updateOrder}
              />
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>الدفع</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-3 text-sm">
              <Row
                label="الطريقة"
                value={paymentMethodLabel(order.payment_method)}
              />
              <Row label="المرجع" value={order.payment_ref || "—"} ltr />
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">المعرّف</span>
                <Badge variant="outline" className="font-mono" dir="ltr">
                  {order.user_id.slice(0, 8)}
                </Badge>
              </div>
              <Row
                label="آخر تحديث"
                value={formatDateTime(order.updated_at)}
              />
            </CardContent>
          </Card>
        </div>
      </div>
    </>
  );
}

function Row({
  label,
  value,
  ltr,
}: {
  label: string;
  value: string;
  ltr?: boolean;
}) {
  return (
    <div className="flex items-start justify-between gap-4">
      <span className="shrink-0 text-muted-foreground">{label}</span>
      <span className="text-left" dir={ltr ? "ltr" : undefined}>
        {value}
      </span>
    </div>
  );
}
