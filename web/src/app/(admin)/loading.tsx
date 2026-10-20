import { RouteLoading } from "@/components/admin/route-loading";

/**
 * Admin loading boundary: shown right after the admin layout resolves while
 * the page's own data (items, orders, ...) is still being fetched.
 */
export default function AdminLoading() {
  return <RouteLoading />;
}
