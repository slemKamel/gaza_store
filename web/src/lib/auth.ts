import { cache } from "react";
import { redirect } from "next/navigation";
import { createClient } from "@/lib/supabase/server";

export type AdminSession = {
  id: string;
  email: string | null;
  fullName: string;
};

/** Current auth user, or null. Deduped per request. */
export const getSessionUser = cache(async () => {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();
  return user;
});

/**
 * True when the given user's `profiles.role` is 'admin'. This is the
 * application-level guard; Row Level Security is the real enforcement.
 */
export async function isAdmin(userId: string): Promise<boolean> {
  const supabase = await createClient();
  const { data } = await supabase
    .from("profiles")
    .select("role")
    .eq("id", userId)
    .maybeSingle();
  return data?.role === "admin";
}

/**
 * Guard for admin pages. Redirects signed-out users to /login and signed-in
 * non-admins to /access-denied (never back to /login, which would loop).
 */
export const requireAdmin = cache(async (): Promise<AdminSession> => {
  const user = await getSessionUser();
  if (!user) redirect("/login");

  const supabase = await createClient();
  const { data: profile } = await supabase
    .from("profiles")
    .select("full_name, role")
    .eq("id", user.id)
    .maybeSingle();

  if (!profile || profile.role !== "admin") redirect("/access-denied");

  return {
    id: user.id,
    email: user.email ?? null,
    fullName: profile.full_name || user.email || "مدير",
  };
});

/**
 * Guard for server actions: like {@link requireAdmin} but throws instead of
 * redirecting, so the caller can surface a message. Server actions are
 * directly callable over the wire, so every write must pass through here —
 * and RLS still rejects non-admin writes at the database.
 */
export async function assertAdmin(): Promise<AdminSession> {
  const user = await getSessionUser();
  if (!user) throw new Error("يجب تسجيل الدخول للمتابعة.");

  const supabase = await createClient();
  const { data: profile } = await supabase
    .from("profiles")
    .select("full_name, role")
    .eq("id", user.id)
    .maybeSingle();

  if (profile?.role !== "admin") {
    throw new Error("لا تملك صلاحية تنفيذ هذا الإجراء.");
  }

  return {
    id: user.id,
    email: user.email ?? null,
    fullName: profile.full_name || user.email || "مدير",
  };
}

