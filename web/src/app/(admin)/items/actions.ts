"use server";

import { revalidatePath } from "next/cache";
import { assertAdmin } from "@/lib/auth";
import { createClient } from "@/lib/supabase/server";
import { fail, ok, toActionError, toErrorMessage, type ActionResult } from "@/lib/actions";
import type { ItemInput } from "@/lib/types";

export async function saveItem(input: ItemInput): Promise<ActionResult> {
  try {
    await assertAdmin();

    const title = input.title.trim();
    if (!title) return fail("اسم المنتج مطلوب.");
    if (!input.category_id) return fail("القسم مطلوب.");

    const price = Number(input.price);
    if (!Number.isFinite(price) || price < 0) {
      return fail("السعر يجب أن يكون رقمًا غير سالب.");
    }

    const payload = {
      category_id: input.category_id,
      title,
      description: input.description?.trim() || null,
      price: Math.round(price * 100) / 100,
      image_url: input.image_url?.trim() || null,
      images: input.images.filter(Boolean),
      is_active: input.is_active,
    };

    const supabase = await createClient();
    const { error } = input.id
      ? await supabase.from("items").update(payload).eq("id", input.id)
      : await supabase.from("items").insert(payload);

    if (error) return fail(toActionError(error));

    revalidatePath("/items");
    revalidatePath("/dashboard");
    return ok();
  } catch (error) {
    return fail(toErrorMessage(error));
  }
}

export async function deleteItem(id: string): Promise<ActionResult> {
  try {
    await assertAdmin();

    const supabase = await createClient();
    const { error } = await supabase.from("items").delete().eq("id", id);
    if (error) return fail(toActionError(error));

    revalidatePath("/items");
    revalidatePath("/dashboard");
    return ok();
  } catch (error) {
    return fail(toErrorMessage(error));
  }
}
