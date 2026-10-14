"use server";

import { revalidatePath } from "next/cache";
import { assertAdmin } from "@/lib/auth";
import { createClient } from "@/lib/supabase/server";
import { fail, ok, toActionError, toErrorMessage, type ActionResult } from "@/lib/actions";
import { ORDER_STATUSES, PAYMENT_STATUSES } from "@/lib/constants";
import type { OrderUpdateInput } from "@/lib/types";

export async function updateOrder(input: OrderUpdateInput): Promise<ActionResult> {
  try {
    await assertAdmin();

    if (!(ORDER_STATUSES as readonly string[]).includes(input.status)) {
      return fail("حالة الطلب غير صالحة.");
    }
    if (!(PAYMENT_STATUSES as readonly string[]).includes(input.payment_status)) {
      return fail("حالة الدفع غير صالحة.");
    }

    const supabase = await createClient();
    const { error } = await supabase
      .from("orders")
      .update({ status: input.status, payment_status: input.payment_status })
      .eq("id", input.id);

    if (error) return fail(toActionError(error));

    revalidatePath("/orders");
    revalidatePath(`/orders/${input.id}`);
    revalidatePath("/dashboard");
    return ok();
  } catch (error) {
    return fail(toErrorMessage(error));
  }
}
