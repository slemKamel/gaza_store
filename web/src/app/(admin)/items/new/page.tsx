import { createClient } from "@/lib/supabase/server";
import { PageHeader } from "@/components/admin/page-header";
import { ItemForm } from "../item-form";

export default async function NewItemPage() {
  const supabase = await createClient();
  const { data: categories } = await supabase
    .from("categories")
    .select("id, name")
    .order("name");

  return (
    <>
      <PageHeader title="منتج جديد" description="إضافة منتج إلى كتالوج المتجر." />
      <div className="max-w-3xl">
        <ItemForm categories={categories ?? []} />
      </div>
    </>
  );
}
