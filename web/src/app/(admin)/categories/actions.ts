"use server";

import { revalidatePath } from "next/cache";
import { assertAdmin } from "@/lib/auth";
import { createClient } from "@/lib/supabase/server";
import { fail, ok, toActionError, toErrorMessage, type ActionResult } from "@/lib/actions";
import type { CategoryInput } from "@/lib/types";

const SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

export async function saveCategory(input: CategoryInput): Promise<ActionResult> {
  try {
    await assertAdmin();

    const name = input.name.trim();
    const slug = input.slug.trim().toLowerCase();

    if (!name) return fail("اسم القسم مطلوب.");
    if (!slug) return fail("المعرّف (slug) مطلوب.");
    if (!SLUG_PATTERN.test(slug)) {
      return fail("المعرّف يقبل الأحرف الإنجليزية الصغيرة والأرقام والشرطات فقط.");
    }

    const payload = {
      name,
      slug,
      image_url: input.image_url?.trim() || null,
      sort_order: Number.isFinite(input.sort_order) ? Math.trunc(input.sort_order) : 0,
      is_active: input.is_active,
    };

    const supabase = await createClient();
    const { error } = input.id
      ? await supabase.from("categories").update(payload).eq("id", input.id)
      : await supabase.from("categories").insert(payload);

    if (error) return fail(toActionError(error));

    revalidatePath("/categories");
    revalidatePath("/dashboard");
    return ok();
  } catch (error) {
    return fail(toErrorMessage(error));
  }
}

export async function deleteCategory(id: string): Promise<ActionResult> {
  try {
    await assertAdmin();

    const supabase = await createClient();
    const { error } = await supabase.from("categories").delete().eq("id", id);
    if (error) return fail(toActionError(error));

    revalidatePath("/categories");
    revalidatePath("/dashboard");
    return ok();
  } catch (error) {
    return fail(toErrorMessage(error));
  }
}
