import { NextResponse, type NextRequest } from "next/server";
import { createServerClient } from "@supabase/ssr";
import { SUPABASE_ANON_KEY, SUPABASE_URL } from "./env";

/**
 * Refreshes the Supabase auth cookies during a request and resolves the current
 * user from the session cookie. `getSession()` decodes the cookie locally with
 * no network round trip, which keeps navigations fast; it refreshes expired
 * tokens automatically when a refresh token is present.
 *
 * This is deliberately only an OPTIMISTIC gate: it decides whether to bounce
 * to /login, nothing security-critical. The authoritative checks are the
 * admin layout's `requireAdmin()` (which validates the user with the Auth
 * server), every server action's `assertAdmin()`, and Row Level Security —
 * a forged or revoked cookie still fails all three.
 */
export async function updateSession(request: NextRequest) {
  let response = NextResponse.next({ request });

  const supabase = createServerClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    cookies: {
      getAll() {
        return request.cookies.getAll();
      },
      setAll(cookiesToSet) {
        for (const { name, value } of cookiesToSet) {
          request.cookies.set(name, value);
        }
        response = NextResponse.next({ request });
        for (const { name, value, options } of cookiesToSet) {
          response.cookies.set(name, value, options);
        }
      },
    },
  });

  const {
    data: { session },
  } = await supabase.auth.getSession();

  return { response, user: session?.user ?? null };
}
