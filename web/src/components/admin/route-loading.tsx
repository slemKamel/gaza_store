import { Loader2 } from "lucide-react";

/**
 * Full-page loading indicator shown while a route's server data is being
 * fetched. Used by the root and admin `loading.tsx` files so navigation
 * switches instantly instead of freezing on the previous page.
 */
export function RouteLoading() {
  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <div className="flex flex-col items-center gap-3 text-muted-foreground">
        <Loader2 className="size-8 animate-spin" />
        <p className="text-sm">جارٍ التحميل...</p>
      </div>
    </div>
  );
}
