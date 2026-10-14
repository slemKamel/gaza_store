import type { PostgrestError } from "@supabase/supabase-js";

/** Result returned by every server action, so forms can show inline errors. */
export type ActionResult = { ok: true } | { ok: false; error: string };

export function ok(): ActionResult {
  return { ok: true };
}

export function fail(error: string): ActionResult {
  return { ok: false, error };
}

/** Maps a Postgres/PostgREST error to a friendly Arabic message. */
export function toActionError(error: PostgrestError | null): string {
  if (!error) return "حدث خطأ غير متوقع.";

  switch (error.code) {
    case "23505":
      return "هذه القيمة مستخدمة مسبقًا.";
    case "23503":
      return "لا يمكن تنفيذ العملية لوجود بيانات مرتبطة.";
    case "23514":
      return "القيمة المدخلة غير صالحة.";
    case "42501":
      return "لا تملك صلاحية تنفيذ هذه العملية.";
    default:
      return error.message || "حدث خطأ غير متوقع.";
  }
}

/** Normalizes any thrown value into a user-facing message. */
export function toErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) return error.message;
  return "حدث خطأ غير متوقع.";
}
