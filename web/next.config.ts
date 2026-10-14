import type { NextConfig } from "next";

/**
 * Cache Components (PPR + static shells) is intentionally disabled: the admin
 * dashboard is fully auth-gated and every screen renders per-user data from
 * Supabase, so prerendering buys nothing while forcing a `<Suspense>` boundary
 * around every `cookies()`/data read. Classic dynamic rendering is simpler and
 * removes any risk of cross-user caching. See web/README.md.
 */
const nextConfig: NextConfig = {
  cacheComponents: false,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
