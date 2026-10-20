import { RouteLoading } from "@/components/admin/route-loading";

/**
 * Root loading boundary: on navigation the router switches to this instantly,
 * so blocking work above the pages (the admin layout's `requireAdmin` check)
 * never freezes the previous page on screen.
 */
export default function RootLoading() {
  return <RouteLoading />;
}
