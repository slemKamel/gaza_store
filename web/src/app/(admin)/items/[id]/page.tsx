import { notFound } from "next/navigation";
import { createClient } from "@/lib/supabase/server";
import { PageHeader } from "@/components/admin/page-header";
import { ItemForm } from "../item-form";

export default async function EditItemPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const supabase = await createClient();

  const [itemResult, categoriesResult] = await Promise.all([
    supabase
      .from("items")
      .select(
        "id, category_id, title, description, price, image_url, images, is_active",
      )
      .eq("id", id)
      .maybeSingle(),
    supabase.from("categories").select("id, name").order("name"),
  ]);

  if (itemResult.error) throw new Error(itemResult.error.message);
  if (!itemResult.data) notFound();

  return (
    <>
      <PageHeader
        title="تعديل منتج"
        description={itemResult.data.title}
      />
      <div className="max-w-3xl">
        <ItemForm
          categories={categoriesResult.data ?? []}
          item={itemResult.data}
        />
      </div>
    </>
  );
}
