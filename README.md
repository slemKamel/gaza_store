# Gaza Store

Gaza Store is an Android mobile application built with Jetpack Compose. It is **Arabic-only with a forced RTL layout** (independent of the device language) and Material 3 theming with edge-to-edge support.

Navigation is a single navigation graph: a splash, then a **3-page onboarding** flow on first launch, then a **sign in / sign up screen** (skippable via "متابعة كزائر"), then a **5-tab bottom bar** (Home, Search, Cart, Orders, Account) with detail routes pushed on top (`list/{categoryId}`, `detail/{itemId}`). The bottom bar is only shown on its own destinations. Onboarding and the sign-in prompt are each remembered locally (DataStore), so returning users land straight on Home; guests get the full catalog without an account. Home, category lists and search results are **two-column product grids**.

Shopping is a three-step flow: **Cart** (local, works for guests too) → **Checkout** (sign-in required) → **Orders** (order history). Details screens let you pick a quantity and add to the cart; the cart keeps quantities per item and shows a subtotal, a flat delivery fee and a total.

Content is loaded from Supabase through a repository layer using MVVM (manual DI in `core/di`).

An **admin dashboard website** lives in [`web/`](./web) (Next.js 16 + TypeScript + Tailwind + shadcn-style UI). It signs in with the same Supabase email/password accounts and lets admins manage categories, items, banners and orders. Access is restricted to `profiles.role = 'admin'` (server-side route guard plus Row Level Security).

## Tech stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Build**: Gradle 9.5.0, AGP 9.3.3
- **Min/Target/Compile SDK**: 26 / 37 / 37
- **Backend**: Supabase (Postgres, Auth, Storage) via [supabase-kt](https://github.com/supabase-community/supabase-kt)
- **Admin dashboard** (`web/`): Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS v4, shadcn-style components on Radix UI, `@supabase/ssr`, Vitest

## Getting started

### Prerequisites
- Android SDK (API 37 available; `sdk.dir` configured in `local.properties`)
- JDK 11+ (project targets Java 11)

### Build and run

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Install and run on connected device/emulator (via Android Studio or ADB)
./gradlew :app:installDebug
```

### Development commands

```bash
# Lint
./gradlew :app:lintDebug
./gradlew :app:lintFix

# Type check (Kotlin compile)
./gradlew :app:compileDebugKotlin

# Unit tests
./gradlew :app:testDebugUnitTest

# All checks
./gradlew check
```

## Project structure

- `app/src/main/java/com/slem/gazastor/`
  - `MainActivity.kt`, `GazaStorApp.kt` - Entry points
  - `core/` - Constants (incl. `DELIVERY_FEE`), manual DI, navigation graph
  - `data/` - DTOs, DTO mappers, Supabase client, local DataStore (preferences + cart), repositories (`ContentRepository`, `AuthRepository`, `CartRepository`, `OrderRepository`, all return `Result`)
  - `ui/` - UI models, screens (splash/onboarding/auth/home/list/detail/search/cart/checkout/orders/account + ViewModels), theme, shared `components/`
- `app/src/main/res/` - Android resources (incl. `font/` with the bundled OFL fonts)
- `licenses/` - Third-party font licenses (OFL)
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumented tests
- `gradle/libs.versions.toml` - Version catalog
- `supabase/schema.sql` - Database schema (source of truth)
- `web/` - Admin dashboard website (Next.js App Router; see [`web/README.md`](./web/README.md))
  - `src/app/(admin)/` - Authenticated admin routes (`dashboard`, `categories`, `items`, `banners`, `orders`)
  - `src/app/login/` - Email/password sign-in
  - `src/components/ui/` - shadcn-style primitives; `src/components/admin/` - dashboard building blocks
  - `src/lib/` - Supabase clients, auth guards, formatting, domain constants, generated DB types
  - `src/proxy.ts` - Session refresh + optimistic redirect (Next 16 "proxy", formerly middleware)

## Design system ("Quiet Luxury")

Fixed light-only brand theme (dynamic color is disabled so branding is consistent on every device):

| Role | Color |
| --- | --- |
| Background (60%) | soft beige `#F5F1EA` |
| Surface / cards | `#FBF9F5` with a hairline `#E0D8CB` border, no shadow |
| Primary (30%) | deep forest green `#1F3A32` |
| Accent (10%) | muted copper `#A9744F` — prices, badges, links only |
| Text | near-black `#1C1B17`; headings in forest green |

- **Typography** uses four OFL families, all bundled in `app/src/main/res/font/` so they work offline (licenses in `licenses/`). The roles live in one place, `ui/theme/Type.kt`:
  - **IBM Plex Sans Arabic** — every Arabic UI string: body, labels, buttons, tab bar, section headings.
  - **Amiri** — the Arabic wordmark "متجر غزة" and the large decorative `display*` tier.
  - **Bodoni Moda** — the Latin half of the brand lockup ("GAZA STORE"), letter-spaced.
  - **Inter** — Latin text and figures (prices, totals, quantities).
  Compose has no `:lang(ar)` selector and no reliable glyph fallback across a `FontFamily`, so each script is applied explicitly. Never hardcode fonts or colors — read them from `MaterialTheme`.
- The brand mark is a Compose `ImageVector` in `app/src/main/java/com/slem/gazastor/ui/components/BrandMark.kt` (copper gradient disc + cream rounded square + shopping bag). It shows on the splash above the shared `BrandLockup` — "متجر غزة" in Amiri over a letter-spaced "GAZA STORE" in Bodoni Moda — and the adaptive launcher icon uses `ic_launcher_foreground` on a flat beige `ic_launcher_background`.
- Shapes are small (6–16 dp); gradients and heavy shadows are avoided on surfaces per the brand rules (the one exception is the onboarding showcase's vector illustration, which needs them for the copper trim and the warm aura).
- The second onboarding page shows `ui/components/EssentialsShowcase.kt` — a Compose transcription of a Stitch SVG export. Four category badges (أزياء / منزل / جمال / تقنية) orbit the luxury bag, descend into a horizontal row on a glowing shelf line, settle and repeat on a 9.5 s loop. It is drawn on a `Canvas` in the export's own 400 × 400 coordinate space, and the loop only starts once the page is on screen.
- Shared UI pieces live in `ui/components/` (`EssentialsShowcase`, `ItemGridCard`, `SectionTitle`, `PriceText`, `AmountText`, `QuantityStepper`, `SignInRequired`, `LoadingIndicator`, `ErrorState`, `EmptyState`, `fullWidthItem`).
- Product detail shows a swipeable gallery (`HorizontalPager`) built from `items.images`, with a single image as a fallback.
- Data failures are surfaced (error + retry) instead of showing a silently empty store; the repository returns `Result` instead of swallowing exceptions.

## Supabase setup

- The Supabase client is wired (`data/remote/supabase/SupabaseClientProvider.kt`). Add real credentials to `local.properties` (never commit):
  ```
  SUPABASE_URL=https://<project>.supabase.co
  SUPABASE_ANON_KEY=<anon-key>
  ```
  They are exposed to Android via `BuildConfig` (fields `SUPABASE_URL`, `SUPABASE_ANON_KEY`). Without them the screens stay empty/loading.
- The database schema lives in [`supabase/schema.sql`](./supabase/schema.sql) and is already applied to the project. It defines:
  - `categories` (`icon_key` picks the outlined glyph the app draws in its Home chip row; unknown/blank falls back to a generic icon), `items` (`price numeric(10,2)`, `images text[]`), `banners` - catalog content read by the app.
  - `profiles` (linked 1:1 to `auth.users`, with `role` = `customer` | `admin`), `orders`, `order_items` - accounts and checkout (cash on delivery now; `payment_method`/`payment_status`/`payment_ref` are ready for online payment later).
  - Row Level Security on every table: public read of active catalog rows; all writes admin-only (`is_admin()`); customers can only see/create their own orders.
  - A `product-images` public Storage bucket with admin-only writes.
- A new sign-up automatically gets a `profiles` row (`handle_new_user` trigger). The first admin must be promoted manually. A `prevent_role_escalation` trigger blocks non-admins from changing roles, so the very first promotion needs a one-time trigger bypass (run as the project owner):
  ```sql
  do $$
  begin
    execute 'alter table public.profiles disable trigger profiles_prevent_role_escalation';
    update public.profiles set role = 'admin' where id = '<auth-user-uuid>';
    execute 'alter table public.profiles enable trigger profiles_prevent_role_escalation';
  end $$;
  ```
  A plain `update ... set role = 'admin'` fails with *"Only admins can change the role"* until at least one admin already exists.
- After that, additional admins can be promoted by an existing admin directly:
  ```sql
  update public.profiles set role = 'admin' where id = '<auth-user-uuid>';
  ```
- **Accounts:** the app signs in with Supabase email/password (`AuthRepository`). Guests can browse the whole catalog without an account; the first-launch sign-in screen is skippable and the choice is stored in DataStore (`auth_prompt_seen`). Session persistence uses supabase-kt's default settings-backed session manager (the Android context comes from the `multiplatform-settings-no-arg` startup initializer), so a signed-in user stays signed in across launches.
- If **email confirmation** is enabled in the Supabase project, sign-up returns no session until the user confirms; the UI shows a "check your email" notice instead of signing them in.

## Cart, checkout and orders

- **Cart** (`CartRepository` / `DataStoreCartRepository`) is device-local: lines are stored as a JSON array in their own DataStore file (`gaza_stor_cart`) so guests can shop without an account and the cart survives process death. Quantities are merged per item and a line is removed when its quantity reaches zero.
- **Checkout** (`CheckoutScreen` / `CheckoutViewModel`) requires a signed-in user (guests get a sign-in prompt). It prefills name and phone from the `profiles` row, validates the delivery details, and offers **cash on delivery** only for now.
- **Delivery fee**: a flat `Constants.DELIVERY_FEE` (10.0 ₪) is added to every non-empty cart. This is a placeholder product decision, kept in one place so it can be changed or made remote later.
- **Placing an order** (`OrderRepository.placeOrder`) inserts one `orders` header (with `user_id` set to the current auth user so the RLS insert policy passes) and then its `order_items` lines. The local cart is cleared only after the backend accepts both. Order history (`getOrders`) selects the customer's orders newest-first with their lines embedded (`*, order_items(*)`), relying on RLS to scope rows to their owner.

## Admin dashboard (`web/`)

A Next.js 16 web app that controls the content shown in the mobile app. It reuses the app's **"Quiet Luxury"** design (same beige/forest-green/copper palette, the same self-hosted OFL families via `next/font/local` — plus Playfair Display for Latin headings — Arabic + RTL, light-only).

**Scope**

- **Dashboard** - content/order counts and the latest orders.
- **Categories** - create/edit/delete (name, slug, image, sort order, active).
- **Items** - create/edit/delete with category, price, description, thumbnail and a multi-image gallery; search by title and filter by category; paginated.
- **Banners** - create/edit/delete with image, link, sort order and active flag.
- **Orders** - search by customer/phone, filter by status, paginated; detail view with line items, totals, delivery details, and status / payment-status updates.
- Image uploads go to the public `product-images` Storage bucket.

**Auth & security**

- Sign-in uses Supabase email/password. The `(admin)` route group guards every page server-side with `requireAdmin()`; non-admins are sent to `/access-denied`.
- Every server action re-checks the admin role (`assertAdmin()`), and Row Level Security is the real gate - non-admin writes are rejected by the database.
- `/login` is the only public route; the proxy refreshes the session and redirects unauthenticated users to `/login?next=…`.

**Commands** (from `web/`)

```bash
npm install          # install dependencies
npm run dev          # dev server on http://localhost:3000
npm run build        # production build (also type-checks)
npm run lint         # ESLint
npm run typecheck    # tsc --noEmit
npm run test         # Vitest
```

**Setup**: create `web/.env.local` (gitignored) from `web/.env.example`:

```
NEXT_PUBLIC_SUPABASE_URL=https://<project>.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=<anon-key>
```

Sign in with an account whose `profiles.role` is `admin` (see the promotion notes above).

## Contributing

- Follow conventions in [AGENTS.md](./AGENTS.md)
- Use version catalog aliases (`libs.*`) in Gradle files
- Never commit secrets or `local.properties` (dashboard secrets live in `web/.env.local`)
- Ensure `./gradlew :app:compileDebugKotlin`, `./gradlew :app:lintDebug`, `./gradlew :app:testDebugUnitTest`, `./gradlew :app:assembleDebug` all pass before finishing work
- For the dashboard, ensure `npm run lint`, `npm run typecheck`, `npm run test` and `npm run build` all pass (from `web/`)

## License

TODO: Add license if needed