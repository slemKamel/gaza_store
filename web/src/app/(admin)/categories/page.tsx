import { ImageIcon } from "lucide-react";
import { createClient } from "@/lib/supabase/server";
import { PageHeader } from "@/components/admin/page-header";
import { ErrorNotice } from "@/components/admin/error-notice";
import { DeleteButton } from "@/components/admin/delete-button";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { CategoryFormDialog } from "./category-form-dialog";
import { deleteCategory, saveCategory } from "./actions";

export default async function CategoriesPage() {
  const supabase = await createClient();
  const { data: categories, error } = await supabase
    .from("categories")
    .select("id, name, slug, image_url, sort_order, is_active")
    .order("sort_order", { ascending: true })
    .order("name", { ascending: true });

  return (
    <>
      <PageHeader
        title="الأقسام"
        description="إدارة أقسام المتجر التي تظهر في التطبيق."
        action={<CategoryFormDialog action={saveCategory} />}
      />

      {error ? (
        <ErrorNotice message={error.message} />
      ) : (
        <div className="overflow-hidden rounded-xl border border-border bg-card">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-16">الصورة</TableHead>
                <TableHead>الاسم</TableHead>
                <TableHead>المعرّف</TableHead>
                <TableHead>الترتيب</TableHead>
                <TableHead>الحالة</TableHead>
                <TableHead className="w-24">إجراءات</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {categories && categories.length > 0 ? (
                categories.map((category) => (
                  <TableRow key={category.id}>
                    <TableCell>
                      <div className="flex size-10 items-center justify-center overflow-hidden rounded-md border border-border bg-brand-variant">
                        {category.image_url ? (
                          <img
                            src={category.image_url}
                            alt=""
                            className="size-full object-cover"
                          />
                        ) : (
                          <ImageIcon className="size-4 text-muted-foreground" />
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="font-medium">
                      {category.name}
                    </TableCell>
                    <TableCell className="text-muted-foreground" dir="ltr">
                      {category.slug}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {category.sort_order}
                    </TableCell>
                    <TableCell>
                      {category.is_active ? (
                        <Badge variant="success">مُفعّل</Badge>
                      ) : (
                        <Badge variant="outline">مخفي</Badge>
                      )}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <CategoryFormDialog
                          category={category}
                          action={saveCategory}
                        />
                        <DeleteButton
                          id={category.id}
                          action={deleteCategory}
                          title="حذف القسم"
                          description={`سيتم حذف «${category.name}» وكل المنتجات المرتبطة به. لا يمكن التراجع.`}
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
                    لا توجد أقسام بعد. أضف أول قسم من الزر أعلاه.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
      )}
    </>
  );
}
