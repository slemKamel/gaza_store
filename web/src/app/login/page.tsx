import { redirect } from "next/navigation";
import { getSessionUser, isAdmin } from "@/lib/auth";
import { LoginForm } from "./login-form";

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string | string[] }>;
}) {
  const user = await getSessionUser();
  if (user) {
    redirect((await isAdmin(user.id)) ? "/dashboard" : "/access-denied");
  }

  const params = await searchParams;
  const rawNext = Array.isArray(params.next) ? params.next[0] : params.next;
  const next =
    rawNext && rawNext.startsWith("/") && !rawNext.startsWith("//")
      ? rawNext
      : "/dashboard";

  return (
    <main className="flex flex-1 items-center justify-center px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="mb-8 flex flex-col items-center gap-1 text-center">
          <span className="font-logo-ar text-3xl font-bold text-primary">متجر غزة</span>
          <span className="font-logo text-[0.7rem] tracking-[0.28em] text-muted-foreground">
            GAZA STORE
          </span>
          <span className="mt-1 text-sm text-muted-foreground">لوحة التحكم</span>
        </div>

        <div className="rounded-xl border border-border bg-card p-6">
          <h1 className="text-lg font-semibold">تسجيل الدخول</h1>
          <p className="mb-6 mt-1 text-sm text-muted-foreground">
            الوصول مخصّص للمشرفين.
          </p>
          <LoginForm next={next} />
        </div>
      </div>
    </main>
  );
}
