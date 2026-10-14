"use server";

import { revalidatePath } from "next/cache";
import { assertAdmin } from "@/lib/auth";
import { createClient } from "@/lib/supabase/server";
import { fail, ok, toActionError, toErrorMessage, type ActionResult } from "@/lib/actions";
import type { BannerInput } from "@/lib/types";

export async function saveBanner(input: BannerInput): Promise<ActionResult> {
  try {
    await assertAdmin();

    const link = input.link?.trim() || null;
    if (link && !/^https?:\/\//i.test(link) && !link.startsWith("/")) {
      return fail("الرابط يجب أن يبدأ بـ http:// أو https:// أو /");
    }

    const payload = {
      title: input.title?.trim() || null,
      image_url: input.image_url?.trim() || null,
      link,
      sort_order: Number.isFinite(input.sort_order) ? Math.trunc(input.sort_order) : 0,
      is_active: input.is_active,
    };

    const supabase = await createClient();
    const { error } = input.id
      ? await supabase.from("banners").update(payload).eq("id", input.id)
      : await supabase.from("banners").insert(payload);

    if (error) return fail(toActionError(error));

    revalidatePath("/banners");
    revalidatePath("/dashboard");
    return ok();
  } catch (error) {
    return fail(toErrorMessage(error));
  }
}

export async function deleteBanner(id: string): Promise<ActionResult> {
  try {
    await assertAdmin();

    const supabase = await createClient();
    const { error } = await supabase.from("banners").delete().eq("id", id);
    if (error) return fail(toActionError(error));

    revalidatePath("/banners");
    revalidatePath("/dashboard");
    return ok();
  } catch (error) {
    return fail(toErrorMessage(error));
  }
}
