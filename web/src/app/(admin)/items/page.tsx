import Link from "next/link";
import { ImageIcon, Pencil, Plus } from "lucide-react";
import type { Metadata } from "next";
import { createClient } from "@/lib/supabase/server";
import { PageHeader } from "@/components/admin/page-header";
import { ErrorNotice } from "@/components/admin/error-notice";
import { DeleteButton } from "@/components/admin/delete-button";
import { Pagination } from "@/components/admin/pagination";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { formatCurrency } from "@/lib/format";
import {
  escapeLike,
  pageRange,
  parsePage,
  queryString,
} from "@/lib/query";
import { deleteItem } from "./actions";

export const metadata: Metadata = { title: "المنتجات" };

const SELECT = "id, title, price, image_url, is_active, category_id";

export default async function ItemsPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const q = queryString(params.q).trim();
  const categoryId = queryString(params.category);
  const page = parsePage(params.page);
  const { from, to } = pageRange(page);

  const supabase = await createClient();

  const [categoriesResult, itemsResult] = await Promise.all([
    supabase.from("categories").select("id, name").order("name"),
    (() => {
      let query = supabase
        .from("items")
        .select(SELECT, { count: "exact" })
        .order("created_at", { ascending: false })
        .range(from, to);
      if (q) query = query.ilike("title", `%${escapeLike(q)}%`);
      if (categoryId) query = query.eq("category_id", categoryId);
      return query;
    })(),
  ]);

  const categories = categoriesResult.data ?? [];
  const categoryName = new Map(categories.map((c) => [c.id, c.name]));
  const items = itemsResult.data ?? [];
  const total = itemsResult.count ?? 0;
  const hasNext = total > to + 1;

  const filters: Record<string, string> = {};
  if (q) filters.q = q;
  if (categoryId) filters.category = categoryId;

  return (
    <>
      <PageHeader
        title="المنتجات"
        description="إدارة منتجات المتجر، أسعارها وصورها."
        action={
          <Link
            href="/items/new"
            className="inline-flex h-9 items-center gap-2 rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground transition-colors hover:bg-brand-green-dark"
          >
            <Plus className="size-4" />
            منتج جديد
          </Link>
        }
      />

      <form
        action="/items"
        className="mb-4 flex flex-wrap items-center gap-2"
      >
        <input
          type="search"
          name="q"
          defaultValue={q}
          placeholder="ابحث باسم المنتج…"
          className="h-9 w-full max-w-xs rounded-md border border-input bg-card px-3 text-sm outline-none focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30"
        />
        <select
          name="category"
          defaultValue={categoryId}
          className="h-9 rounded-md border border-input bg-card px-3 text-sm outline-none focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30"
        >
          <option value="">كل الأقسام</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        <button
          type="submit"
          className="h-9 rounded-md border border-border px-4 text-sm font-medium transition-colors hover:bg-accent"
        >
          بحث
        </button>
        {q || categoryId ? (
          <Link
            href="/items"
            className="h-9 rounded-md px-3 text-sm leading-9 text-muted-foreground hover:underline"
          >
            مسح
          </Link>
        ) : null}
      </form>

      {itemsResult.error ? (
        <ErrorNotice message={itemsResult.error.message} />
      ) : (
        <>
          <div className="overflow-hidden rounded-xl border border-border bg-card">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-16">الصورة</TableHead>
                  <TableHead>الاسم</TableHead>
                  <TableHead>القسم</TableHead>
                  <TableHead>السعر</TableHead>
                  <TableHead>الحالة</TableHead>
                  <TableHead className="w-24">إجراءات</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {items.length > 0 ? (
                  items.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>
                        <div className="flex size-10 items-center justify-center overflow-hidden rounded-md border border-border bg-brand-variant">
                          {item.image_url ? (
                            <img
                              src={item.image_url}
                              alt=""
                              className="size-full object-cover"
                            />
                          ) : (
                            <ImageIcon className="size-4 text-muted-foreground" />
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="font-medium">
                        {item.title}
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {categoryName.get(item.category_id) ?? "—"}
                      </TableCell>
                      <TableCell>{formatCurrency(item.price)}</TableCell>
                      <TableCell>
                        {item.is_active ? (
                          <Badge variant="success">مُفعّل</Badge>
                        ) : (
                          <Badge variant="outline">مخفي</Badge>
                        )}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1">
                          <Link
                            href={`/items/${item.id}`}
                            aria-label="تعديل"
                            className="inline-flex size-9 items-center justify-center rounded-md text-foreground transition-colors hover:bg-accent"
                          >
                            <Pencil className="size-4" />
                          </Link>
                          <DeleteButton
                            id={item.id}
                            action={deleteItem}
                            title="حذف المنتج"
                            description={`سيتم حذف «${item.title}». لا يمكن التراجع.`}
                          />
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell
                      colSpan={6}
                      className="py-10 text-center text-muted-foreground"
                    >
                      {q || categoryId
                        ? "لا توجد نتائج مطابقة للبحث."
                        : "لا توجد منتجات بعد."}
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
                    pathname: "/items",
                    query: { ...filters, page: String(page - 1) },
                  }
                : null
            }
            nextHref={
              hasNext
                ? {
                    pathname: "/items",
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
