import Link from "next/link";
import { ArrowUpLeft } from "lucide-react";
import { createClient } from "@/lib/supabase/server";
import { PageHeader } from "@/components/admin/page-header";
import { StatCard } from "@/components/admin/stat-card";
import { OrderStatusBadge } from "@/components/admin/order-status-badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { formatCurrency, formatOrderDate } from "@/lib/format";

export default async function DashboardPage() {
  const supabase = await createClient();

  const [items, categories, banners, orders, pendingOrders, recentOrders] =
    await Promise.all([
      supabase.from("items").select("id", { count: "exact", head: true }),
      supabase.from("categories").select("id", { count: "exact", head: true }),
      supabase.from("banners").select("id", { count: "exact", head: true }),
      supabase.from("orders").select("id", { count: "exact", head: true }),
      supabase
        .from("orders")
        .select("id", { count: "exact", head: true })
        .eq("status", "pending"),
      supabase
        .from("orders")
        .select("id, customer_name, total, status, created_at")
        .order("created_at", { ascending: false })
        .limit(5),
    ]);

  return (
    <>
      <PageHeader
        title="لوحة التحكم"
        description="نظرة عامة على محتوى المتجر وأحدث الطلبات."
      />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="المنتجات" value={items.count ?? 0} />
        <StatCard label="الأقسام" value={categories.count ?? 0} />
        <StatCard label="البانرات" value={banners.count ?? 0} />
        <StatCard
          label="الطلبات"
          value={orders.count ?? 0}
          hint={
            pendingOrders.count
              ? `${pendingOrders.count} قيد المعالجة`
              : undefined
          }
        />
      </div>

      <section className="mt-8">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-base font-semibold">أحدث الطلبات</h2>
          <Link
            href="/orders"
            className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
          >
            عرض الكل
            <ArrowUpLeft className="size-4" />
          </Link>
        </div>

        <div className="overflow-hidden rounded-xl border border-border bg-card">
          {recentOrders.data && recentOrders.data.length > 0 ? (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>العميل</TableHead>
                  <TableHead>التاريخ</TableHead>
                  <TableHead>الإجمالي</TableHead>
                  <TableHead>الحالة</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {recentOrders.data.map((order) => (
                  <TableRow key={order.id}>
                    <TableCell>
                      <Link
                        href={`/orders/${order.id}`}
                        className="font-medium hover:underline"
                      >
                        {order.customer_name}
                      </Link>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formatOrderDate(order.created_at)}
                    </TableCell>
                    <TableCell>{formatCurrency(order.total)}</TableCell>
                    <TableCell>
                      <OrderStatusBadge status={order.status} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          ) : (
            <p className="p-6 text-sm text-muted-foreground">
              لا توجد طلبات بعد.
            </p>
          )}
        </div>
      </section>
    </>
  );
}
