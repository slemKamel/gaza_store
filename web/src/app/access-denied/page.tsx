import { redirect } from "next/navigation";
import { ShieldAlert } from "lucide-react";
import { getSessionUser } from "@/lib/auth";
import { SignOutButton } from "@/components/admin/sign-out-button";

export default async function AccessDeniedPage() {
  const user = await getSessionUser();
  if (!user) redirect("/login");

  return (
    <main className="flex flex-1 items-center justify-center px-4 py-10">
      <div className="w-full max-w-md rounded-xl border border-border bg-card p-6 text-center">
        <div className="mx-auto mb-4 flex size-11 items-center justify-center rounded-full bg-brand-error-container text-brand-error-on">
          <ShieldAlert className="size-5" />
        </div>
        <h1 className="text-lg font-semibold">لا تملك صلاحية الوصول</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          هذا الحساب مسجّل الدخول لكنه ليس مشرفًا. تواصل مع مسؤول النظام لمنحك
          الصلاحية.
        </p>
        <p className="mt-3 text-xs text-muted-foreground" dir="ltr">
          {user.email}
        </p>
        <div className="mt-6 flex justify-center">
          <SignOutButton variant="outline" className="w-full justify-center" />
        </div>
      </div>
    </main>
  );
}
