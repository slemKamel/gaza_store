# Gaza Store — Admin Dashboard

Admin website for the Gaza Store mobile app. Built with **Next.js 16** (App Router), **React 19**, **TypeScript**, **Tailwind CSS v4** and **shadcn-style** components on top of **Radix UI**. It talks to the same Supabase project as the Android app using `@supabase/ssr` (cookie-based sessions).

The dashboard mirrors the app's **"Quiet Luxury"** design: beige `#F5F1EA` background, deep forest green `#1F3A32` primary, copper `#A9744F` accents, hairline `#E0D8CB` borders, flat surfaces, Arabic + RTL, light-only. Five OFL families are self-hosted via `next/font/local` from `src/app/fonts/` (the same TTFs the app bundles in `app/src/main/res/font/`, plus Playfair Display for Latin headings).

## Getting started

```bash
# from the repository root
cd web

# 1. Configure environment (never commit .env.local)
copy .env.example .env.local   # then fill in the values

# 2. Install dependencies
npm install

# 3. Run the dev server
npm run dev                    # http://localhost:3000
```

`web/.env.local`:

```
NEXT_PUBLIC_SUPABASE_URL=https://<project>.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=<anon-key>
```

Sign in with a Supabase account whose `profiles.role` is `admin`. See the root [`README.md`](../README.md) for how to promote the first admin (a `prevent_role_escalation` trigger requires a one-time bypass for the very first promotion).

## Commands

```bash
npm run dev        # dev server
npm run build      # production build (also runs the TypeScript check)
npm run start      # serve the production build
npm run lint       # ESLint (flat config)
npm run typecheck  # tsc --noEmit
npm run test       # Vitest (unit tests for pure helpers)
```

## Routes

| Route | Description |
| --- | --- |
| `/login` | Email/password sign-in (public) |
| `/access-denied` | Shown to signed-in non-admins |
| `/dashboard` | Counts + latest orders |
| `/categories` | Category CRUD |
| `/items`, `/items/new`, `/items/[id]` | Item CRUD with search/filter/gallery |
| `/banners` | Banner CRUD |
| `/orders`, `/orders/[id]` | Order list (search/filter/paginate) and detail with status updates |

## Structure

```
src/
├── app/
│   ├── (admin)/            # authenticated route group (layout runs the admin guard)
│   │   ├── dashboard/ categories/ items/ banners/ orders/
│   ├── login/              # page + client form
│   ├── access-denied/
│   ├── fonts/              # self-hosted OFL TTFs (five families)
│   ├── layout.tsx          # <html lang="ar" dir="rtl"> + next/font variables
│   ├── page.tsx            # redirects to /dashboard or /login
│   ├── not-found.tsx
│   └── globals.css         # brand tokens + shadcn theme variables
├── components/
│   ├── admin/              # shell, sidebar, forms, uploaders, table helpers
│   └── ui/                 # shadcn-style primitives (button, input, dialog, select, …)
├── lib/
│   ├── supabase/           # env, browser client, server client, proxy helper
│   ├── auth.ts             # getSessionUser / requireAdmin / assertAdmin
│   ├── constants.ts        # statuses, labels, delivery fee, bucket name
│   ├── format.ts           # price/date formatting (mirrors the Android app)
│   ├── query.ts            # pagination + ilike escaping helpers
│   ├── actions.ts          # server-action result helpers
│   ├── types.ts            # shared form input types
│   └── database.types.ts   # generated from the Supabase schema
└── proxy.ts                # session refresh + optimistic auth redirect
```

## Security model

- **Route guard (server):** the `(admin)` layout calls `requireAdmin()`, which redirects signed-out users to `/login` and signed-in non-admins to `/access-denied`.
- **Server actions:** every write calls `assertAdmin()` before touching the database, so actions can't be invoked by non-admins even directly over the wire.
- **Row Level Security (the real gate):** catalog writes require an admin (`is_admin()`), customers only see their own orders, and the `product-images` bucket is publicly readable but admin-writable. Even if the app-level guards were bypassed, the database would reject the write.
- The only public route is `/login`; everything else requires a session.

## Implementation notes

- **Next 16:** middleware is now `proxy.ts` (exporting `proxy`), and typed routes are enabled. `cookies()` is async, so the server client is created with `await cookies()`.
- **Cache Components are disabled** (`cacheComponents: false` in `next.config.ts`). The dashboard is entirely auth-gated and per-user, so classic dynamic rendering is simpler and avoids accidentally caching one admin's data for another.
- **Formatting matches the app:** amounts use Western digits with thousands separators (`1,500 ₪`), dates render as `yyyy/MM/dd`, and order statuses use the same Arabic labels as the Android UI (`src/lib/constants.ts` / `src/lib/format.ts`).
- **Typography** (`src/app/layout.tsx` + `src/app/globals.css`): five self-hosted families are registered with `next/font/local` and composed into five roles on `:root` — `--font-logo` (Bodoni Moda → Amiri), `--font-heading` (Playfair Display → Amiri), `--font-body` (Inter → IBM Plex Sans Arabic), `--font-body-ar` (IBM Plex Sans Arabic) and `--font-logo-ar` (Amiri). Because each role is a per-glyph fallback chain, one line may safely mix scripts. `:lang(ar)` / `[dir="rtl"]` selects the Arabic-first body stack and removes tracking (Arabic letter joining); Latin and figures opt back in with the script-fixed `.font-latin`, `.font-logo` and `.font-logo-ar` utilities. Arabic line heights are raised to 1.6–1.8 on the `--text-*--line-height` tokens. Licenses live in `../licenses/`.
- **Images** use plain `<img>` (the `@next/next/no-img-element` rule is disabled) because admins may paste arbitrary image URLs that aren't allow-listed for `next/image`.
- `src/lib/database.types.ts` is generated from the Supabase schema; regenerate it after schema changes.
