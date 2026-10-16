import Link from "next/link";

export default function NotFound() {
  return (
    <main className="flex flex-1 flex-col items-center justify-center gap-4 px-4 py-24 text-center">
      <p className="font-latin text-4xl font-bold text-primary">404</p>
      <h1 className="text-lg font-semibold">الصفحة غير موجودة</h1>
      <p className="max-w-sm text-sm text-muted-foreground">
        الرابط الذي تحاول الوصول إليه غير متاح أو تم نقله.
      </p>
      <Link
        href="/dashboard"
        className="mt-2 inline-flex h-9 items-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground transition-colors hover:bg-brand-green-dark"
      >
        العودة إلى لوحة التحكم
      </Link>
    </main>
  );
}
