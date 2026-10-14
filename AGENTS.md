# AGENTS.md - Gaza Stor

## Project overview

Gaza Stor is an Android mobile application built with Jetpack Compose. It uses a Material 3 theme (GazaStorTheme) with edge-to-edge support and a navigation graph made of a 5-tab bottom bar (Home, Search, Cart, Orders, Account) plus pushed routes (List, Detail). Home, category and search results render as two-column product grids. Data is loaded through a Supabase-backed repository (postgrest) using MVVM (ViewModel + StateFlow). The package name is `com.slem.gazastor`.

The app works with Supabase as its backend. An **admin dashboard website** lives in `web/` (Next.js 16 + TypeScript + Tailwind + shadcn-style UI, Arabic/RTL, same "Quiet Luxury" design as the app) and manages categories, items, banners and orders. Access is restricted to `profiles.role = 'admin'` (server-side route guard + server-action guard + RLS).

## Tech stack

- **Language**: Kotlin 2.2.10 (declared in version catalog; Gradle reports Kotlin 2.3.20 at runtime)
- **UI Framework**: Jetpack Compose with Material 3
- **Android SDK**: `compileSdk` 37, `targetSdk` 37, `minSdk` 26
- **Build System**: Gradle 9.5.0 with Android Gradle Plugin (AGP) 9.3.3
- **Java Compatibility**: Java 11 (source and target compatibility)
- **Version Management**: Gradle Version Catalog (`gradle/libs.versions.toml`)
- **Testing**: JUnit 4 (unit and instrumented tests)
- **Admin dashboard** (`web/`): Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS v4, shadcn-style components on Radix UI, `@supabase/ssr`, Vitest, ESLint 9

## Project structure

```
.
├── app/
│   ├── build.gradle.kts           # App module configuration
│   └── src/
│       ├── androidTest/           # Instrumented tests (device/emulator)
│       │   └── java/com/slem/gazastor/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/slem/gazastor/
│       │   │   ├── GazaStorApp.kt            # Application class (registered in manifest)
│       │   │   ├── MainActivity.kt           # Main entry point (ComponentActivity)
│       │   │   ├── core/
│       │   │   │   ├── config/Constants.kt   # App constants (incl. DELIVERY_FEE)
│       │   │   │   ├── di/AppModule.kt       # Shared repositories (manual DI)
│       │   │   │   └── navigation/NavGraph.kt # Routes: Splash + onboarding + auth + 5 tabs + checkout
│       │   │   ├── data/
│       │   │   │   ├── model/                # CategoryDto, ItemDto, BannerDto, ProfileDto, OrderDto, CartLine
│       │   │   │   ├── preferences/          # OnboardingPreferences + AuthPreferences + UserPreferences (DataStore)
│       │   │   │   ├── remote/mapper/        # DTO <-> UI model mappers
│       │   │   │   ├── remote/supabase/      # SupabaseClientProvider
│       │   │   │   └── repository/           # ContentRepository, AuthRepository, CartRepository, OrderRepository
│       │   │   └── ui/
│       │   │       ├── model/                # CategoryUi, ItemUi, BannerUi, PriceFormat, DateFormats
│       │   │       ├── components/           # ItemGridCard, SectionTitle, PriceText, AmountText, QuantityStepper, SignInRequired, Loading/Error/Empty
│       │   │       ├── screens/              # splash, onboarding, auth, home, list, detail, search, cart, checkout, orders, account
│       │   │       └── theme/                # Color.kt (brand palette), Theme.kt, Type.kt
│       │   ├── keepRules/rules.keep          # R8/ProGuard rules
│       │   └── res/                          # strings.xml, themes.xml, colors.xml, font/
│       └── test/                   # Unit tests (local JVM)
│           └── java/com/slem/gazastor/       # + testing/ (MainDispatcherRule, fakes)
├── supabase/
│   └── schema.sql                 # Database schema (source of truth)
├── web/                            # Admin dashboard (Next.js App Router)
│   ├── src/app/(admin)/            # Authenticated routes: dashboard, categories, items, banners, orders
│   ├── src/app/login/              # Email/password sign-in (only public route)
│   ├── src/components/ui/          # shadcn-style primitives; components/admin/ - dashboard parts
│   ├── src/lib/                    # Supabase clients, auth guards, format/constants/query, DB types
│   ├── src/proxy.ts                # Session refresh + optimistic redirect (Next 16 proxy)
│   └── README.md                   # Dashboard docs (setup, commands, security)
├── licenses/                       # Third-party licenses (IBM Plex Sans Arabic, OFL)
├── gradle/
│   ├── libs.versions.toml         # Version catalog
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts               # Top-level build config
├── gradle.properties              # Gradle/Kotlin properties
├── gradlew, gradlew.bat           # Gradle wrapper (Unix/Windows)
├── local.properties               # Local SDK config (DO NOT commit)
├── settings.gradle.kts            # Project settings
└── AGENTS.md                      # This file
```

## Commands

All commands from repository root (`C:\Users\HP\AndroidStudioProjects\GazaStor`).

```bash
# Setup
./gradlew --version

# Build
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew :app:clean

# Lint/Typecheck
./gradlew :app:lintDebug
./gradlew :app:lintFix
./gradlew :app:compileDebugKotlin
./gradlew :app:compileReleaseKotlin

# Tests
./gradlew :app:testDebugUnitTest
./gradlew :app:testDebugUnitTest --tests "com.slem.gazastor.ExampleUnitTest.addition_isCorrect"
./gradlew :app:connectedAndroidTest  # requires device/emulator

# Verify all
./gradlew check
```

### Admin dashboard (`web/`)

Run these from the `web/` directory (Node 24 / npm 11):

```bash
npm install          # install dependencies
npm run dev          # dev server on http://localhost:3000
npm run build        # production build (also type-checks)
npm run start        # serve the production build
npm run lint         # ESLint
npm run typecheck    # tsc --noEmit
npm run test         # Vitest
```

Config lives in `web/.env.local` (gitignored; copy `web/.env.example`).

## Code style and conventions

- **Kotlin code style**: `official` (via `kotlin.code.style=official`)
- **Package naming**: lowercase (e.g., `com.slem.gazastor`)
- **Class naming**: PascalCase
- **Function/variable naming**: camelCase
- **Compose**: Material 3, use `GazaStorTheme`; reuse shared pieces from `ui/components/`
- **Imports**: Explicit preferred
- **Resources**: `lowercase_underscore`
- **Strings**: Extract to `res/values/strings.xml`
- **Design ("Quiet Luxury")**: fixed light-only theme, dynamic color disabled. Beige `#F5F1EA` backgrounds (60%), deep forest green `#1F3A32` primary (30%), muted copper `#A9744F` accent for prices/badges only (10%). Flat surfaces with hairline `#E0D8CB` borders - avoid gradients and heavy shadows. Typography uses **IBM Plex Sans Arabic** (OFL, bundled in `res/font/`); never hardcode fonts or colors — read them from `MaterialTheme`.
- **Language**: Arabic-only. The default locale is Arabic and the UI is forced RTL (`LayoutDirection.Rtl` in `MainActivity`) regardless of the device language. Format prices with the `price_format` string (Shekel `₪`); bare amounts (totals, order lines) use `AmountText` + `currency_format` and `formatPriceValue` (Western digits, e.g. `1,500 ₪`).
- **Gradle**: Use version catalog aliases (`libs.*`)
- **Admin dashboard**: mirror the same design tokens and Arabic labels/formatting as the app (`web/src/app/globals.css`, `web/src/lib/constants.ts`, `web/src/lib/format.ts`). Keep the UI Arabic + RTL and light-only.

## Architecture notes

- Single-activity Jetpack Compose app (`MainActivity` extends `ComponentActivity`, sets content with `GazaStorTheme`)
- `enableEdgeToEdge()` enabled, with light system-bar icons forced (the UI is light-only)
- **Theme:** light-only "Quiet Luxury" brand theme (`GazaStorTheme`); dynamic color is disabled. Arabic typography uses the bundled IBM Plex Sans Arabic font
- **Screens:** every data-driven screen consumes a `UiState` with `isLoading`/`isError`/`isEmpty` and renders shared `ui/components/` pieces for loading, error+retry, and empty states
- **Navigation:** `core/navigation/NavGraph.kt` owns the `Screen` route constants and shows the bottom bar only on its 5 destinations. Tabs navigate with `launchSingleTop` + `restoreState`/`saveState`; overlays like Detail hide the bar. Flow is Splash -> Onboarding -> Auth (both first-launch only) -> Home, then the tabs
- **Onboarding:** `SplashViewModel` reads the local flags (`OnboardingPreferences`/`AuthPreferences`, DataStore-backed `UserPreferences`) and routes to `onboarding`, `auth` or `home`; `OnboardingScreen` is a 3-page skippable `HorizontalPager` and `OnboardingViewModel.complete()` persists the flag *before* navigating so it can't reappear
- **Auth:** `AuthRepository` wraps supabase-kt Auth (email/password) behind `Result`; `AuthViewModel` owns the shared `AuthContent` form (used full-screen after onboarding and inside the Account tab). Choosing "متابعة كزائر" sets `auth_prompt_seen` and lets guests use the whole catalog; `AccountViewModel` mirrors `sessionState` and loads the `profiles` row
- **Local storage:** device-scoped flags live in `data/preferences` behind interfaces (fakeable in tests). `AppModule.init(context)` is called from `GazaStorApp` to give it an application context. Supabase session persistence uses supabase-kt's default settings-backed session manager (no extra wiring needed)
- **Grids:** catalog screens use `LazyVerticalGrid` (2 columns) with `fullWidthItem` for full-bleed sections; search debounces ~300 ms in `SearchViewModel` and queries Postgrest with an escaped `ilike` OR-filter
- **Detail:** image gallery is a `HorizontalPager` over `ItemUi.images` with dot indicators; a shared `QuantityStepper` picks how many to add, and add-to-cart shows a snackbar
- **Cart:** device-local via `CartRepository`/`DataStoreCartRepository` (JSON array in its own DataStore file `gaza_stor_cart`, separate from `UserPreferences`' `gaza_stor_preferences` — two `preferencesDataStore` delegates must never share a file name). Lines merge per item, are removed at quantity 0, and guests can fill the cart. `CartViewModel` merges cart lines with the auth session (`isSignedIn`) to decide whether checkout is allowed
- **Checkout:** `CheckoutViewModel` requires a signed-in user, prefills name/phone from the `profiles` row, validates the delivery details and offers cash on delivery only. `OrderRepository.placeOrder` inserts the `orders` header with `user_id = auth.uid()` (required by the RLS insert policy) then the `order_items` lines; the cart is cleared only after both are accepted. Success emits a one-shot `CheckoutEvent.OrderPlaced` that navigates to Orders
- **Orders:** `OrdersViewModel` loads on session changes; `getOrders` selects `*, order_items(*)` newest-first and maps statuses to Arabic labels. Guests see a `SignInRequired` prompt instead of an empty list
- **Delivery fee:** flat `Constants.DELIVERY_FEE` (10.0 ₪) added to every non-empty cart; a placeholder decision kept in one place so it can be changed or made remote later
- **Repository:** `ContentRepository` returns `Result` (never throws away errors); it does not swallow exceptions, so failures surface in the UI instead of showing an empty store
- **DI:** `core/di/AppModule` exposes app-wide singletons (`supabaseClient`, `contentRepository`, `authRepository`, `cartRepository`, `orderRepository`, `userPreferences`) and is initialised (`init(context)`) from `GazaStorApp`. ViewModels take them as default constructor args, so `viewModel()` resolves and tests can still pass fakes. Repositories are singletons so state shared across screens (auth session, cart) has one owner
- Current state: bottom navigation (5 tabs), splash + first-launch onboarding, Supabase email/password auth with a guest escape hatch and the Account screen (profile + sign out), two-column grids, image gallery, search, and the full cart -> checkout -> order-history flow are wired on top of the Supabase data layer. Schema is applied (`supabase/schema.sql`) with demo catalog rows, so Home/List/Detail/Search render real content once `SUPABASE_URL`/`SUPABASE_ANON_KEY` are set. COD only; no payment gateway yet in the app. The admin dashboard (`web/`) covers the full catalog (categories/items/banners, including Storage image upload) plus order management.
- **Architecture:** MVVM (ViewModel + StateFlow/UiState), manual DI in `core/di`, Kotlin Coroutines + Flow
- **Supabase:** supabase-kt (Auth/Postgrest/Storage/Realtime); `SUPABASE_URL` and `SUPABASE_ANON_KEY` stored in `local.properties` (never commit) and exposed via `BuildConfig`. Schema + RLS live in `supabase/schema.sql` (applied via Supabase MCP). Catalog is public read; writes are admin-only; customers see only their own orders.
- **Admin dashboard (`web/`):** Next.js 16 App Router + React 19 + Tailwind v4 with shadcn-style components (hand-written on Radix; the shadcn CLI was not used). `@supabase/ssr` cookie sessions; `src/proxy.ts` (Next 16's renamed middleware) refreshes the session and redirects unauthenticated users. The `(admin)` route-group layout calls `requireAdmin()` (server guard) and every server action calls `assertAdmin()`; **RLS remains the real gate**. Dashboard pages mirror the app's palette/labels/formatting (`src/lib/constants.ts`, `src/lib/format.ts`). Cache Components are intentionally disabled because the whole dashboard is auth-gated and per-user. Next 16 notes: `middleware.ts` is now `proxy.ts`, typed routes are on, and `cookies()` is async.

## Testing rules

- **Unit tests**: JUnit 4 in `app/src/test/java/com/slem/gazastor/`
- **Instrumented tests**: JUnit 4 with `AndroidJUnitRunner` in `app/src/androidTest/java/com/slem/gazastor/`
- Add tests for new features; critical flows should be tested appropriately
- No mocking framework configured - prefer hand-written fakes in `app/src/test/java/com/slem/gazastor/testing/` (e.g. `FakeOnboardingPreferences`, `FakeAuthRepository`, `FakeCartRepository`, `FakeOrderRepository`) and `MainDispatcherRule` for ViewModel coroutine tests (`kotlinx-coroutines-test`)
- **Admin dashboard tests**: Vitest in `web/src/**/*.test.ts` (run with `npm run test`), covering the pure helpers (formatting, query/pagination, label constants)

## Security and safety

- **Never commit secrets** - Supabase keys, API tokens, credentials must not be committed
- Store secrets in `local.properties` or environment variables; read via `BuildConfig` for Android
- `local.properties` is local-only and gitignored
- **Dashboard secrets**: `web/.env.local` (gitignored; `web/.env.example` is committed). Never use the Supabase **service role** key in the dashboard - it uses the anon key + the signed-in admin's session so RLS still applies
- **RLS (Row Level Security)**: Enforce Supabase RLS policies. Treat anon as read-only for public data; writes restricted to authenticated admin users only. Dashboard/UI guards must not be the only protection (the dashboard guards routes/actions *and* relies on RLS)
- Never edit: `gradlew`/`gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`, `local.properties`, `.gitignore` without explicit approval
- R8 keep rules in `app/src/main/keepRules/rules.keep` - modify only with justification

## Definition of done

1. Code follows conventions and matches existing style
2. Kotlin compiles successfully (`./gradlew :app:compileDebugKotlin`)
3. Lint passes with no new errors (`./gradlew :app:lintDebug`)
4. Unit tests pass (`./gradlew :app:testDebugUnitTest`)
5. No unused code, hardcoded secrets, or dead code
6. Version catalog aliases used for dependencies
7. README.md and AGENTS.md updated if commands/structure/conventions change
8. Build succeeds (`./gradlew :app:assembleDebug`) - document env issues if blocked
9. **Supabase**: secrets only via BuildConfig/local env; RLS policies considered/documented; admin access guarded
10. **Admin dashboard**: access restricted to admin role (UI route guard + backend/RLS); `npm run lint`, `npm run typecheck`, `npm run test` and `npm run build` all pass from `web/`

## Gotchas

- **Build compatibility**: Fixed - `compileSdk`/`targetSdk` updated to 37 to satisfy `androidx.core:core-ktx:1.19.0`
- **Configuration cache**: Enabled (`org.gradle.configuration-cache=true`). Build script changes may require cache invalidation
- **Not a git repo**: Workspace not initialized as git
- **Static analysis**: Only Android Lint configured (no ktlint/detekt)
- **Promoting the first admin**: a plain `update public.profiles set role='admin' ...` is blocked by the `prevent_role_escalation` trigger ("Only admins can change the role"). The very first admin needs a one-time trigger bypass (see README.md). Later promotions work directly.
- **Next 16 dashboard**: middleware is now `proxy.ts` (exports `proxy`); typed routes are on; `cookies()` is async. Cache Components are disabled on purpose (see `web/README.md`)

## TODOs

- [x] Add README.md
- [x] Implement UI content (Splash/Home/List/Detail + navigation)
- [x] Add Supabase dependencies (supabase-kt, kotlinx-serialization, Coil, Navigation Compose)
- [x] Set up Supabase client with BuildConfig for keys
- [x] Provide real `SUPABASE_URL`/`SUPABASE_ANON_KEY` in `local.properties`
- [x] Design + apply database schema (`supabase/schema.sql`: categories, items, banners, profiles with roles, orders, order_items) and RLS policies
- [x] Implement data layer (models, repository) and basic UI (home/list/detail)
- [x] Arabic/RTL UI (Arabic `strings.xml`, `supportsRtl`, force `LayoutDirection.Rtl`); remove hardcoded English strings
- [x] Design foundation ("Quiet Luxury" beige/green/copper palette, IBM Plex Sans Arabic, shared components, visible error/empty states)
- [x] Implement bottom navigation (Home/Search/Cart/Orders/Account), two-column grids, detail image gallery and catalog search
- [x] First-launch onboarding (3 skippable pages) with a splash that routes to onboarding or home, remembered via DataStore
- [x] Email/password auth with a "continue as guest" escape hatch, account screen (profile + sign out) and Supabase session persistence
- [x] Implement cart, checkout and order history screens
- [x] Create admin dashboard (location/tech TBD; document in README if separate repo)
- [x] Promote first admin (`update public.profiles set role='admin' where id=...`)
- [x] Add Storage upload flow for product images
- [ ] Define app purpose/features fully
- [ ] Establish git workflow if using git
