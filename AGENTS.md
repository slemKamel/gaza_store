# AGENTS.md - Gaza Store

## Project overview

Gaza Store is an Android mobile application built with Jetpack Compose. It uses a Material 3 theme (GazaStorTheme) with edge-to-edge support and a navigation graph made of a 5-tab bottom bar (Home, Favorites, Cart, Orders, Account) plus pushed routes (List, Detail, Search). Home, category and search results render as two-column product grids. Data is loaded through a Supabase-backed repository (postgrest) using MVVM (ViewModel + StateFlow). The package name is `com.slem.gazastor`.

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
│       │   │       ├── components/           # EssentialsShowcase, QuickOrdersShowcase, ItemGridCard, CategoryIcons, SectionTitle, PriceText, AmountText, QuantityStepper, QuickCartActions, AppButton, AppTextField, BrandMark, BrandLockup, BrandHeader, SignInRequired, PressScale, ShimmerSkeleton, Loading/Error/Empty
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
├── licenses/                       # Third-party font licenses (OFL: IBM Plex Sans Arabic, Amiri, Bodoni Moda, Inter, Playfair Display)
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
- **Design ("Quiet Luxury")**: fixed light-only theme, dynamic color disabled. Beige `#F5F1EA` backgrounds (60%), deep forest green `#1F3A32` primary (30%), muted copper `#A9744F` accent for prices/badges only (10%). Flat surfaces with hairline `#E0D8CB` borders - avoid gradients and heavy shadows. **Typography** ships four OFL families bundled in `res/font/` (offline) with the roles defined once in `ui/theme/Type.kt`: **IBM Plex Sans Arabic** for all Arabic UI (body, labels, buttons, tab bar, section headings), **Amiri** for the Arabic wordmark and the `display*` tier, **Bodoni Moda** for the letter-spaced Latin lockup, **Inter** for Latin text and figures. Compose has no `:lang(ar)` selector and no reliable glyph fallback across a `FontFamily`, so each script is applied explicitly. Never hardcode fonts or colors — read them from `MaterialTheme`. The brand mark (`ui/components/BrandMark.kt`, a Compose `ImageVector`) sits above the shared `BrandLockup` on the splash and the onboarding hero; the adaptive launcher icon uses `res/drawable/ic_launcher_foreground.xml` over a flat beige `ic_launcher_background`.
- **Product images**: the catalog intentionally ships **without photos**. `ui/components/ProductImage.kt` paints a `surfaceVariant` panel and, when the caller passes `fallbackIconKey = item.iconKey`, centers that product's category glyph (`categories.icon_key`, resolved by `ui/components/CategoryIcons.kt`) at 40% of the image box in `primary` at 35% alpha — so Home, List, Search, Favorites, Cart and Detail all fall back to the same branded panel, and the photo-less detail screen shows it large (a card with the product-card hairline, no pager) instead of an empty gallery. `ContentScale.Fit` is the product default; banners and legacy category artwork still pass `Crop`. **Adding real photos later needs no code change**: export 1:1, 1000–1200 px, WebP on a uniform `#EDE7DC` (`surfaceVariant`) background, then set `items.image_url` (single photo) and `items.images` (gallery) — `Fit` letterboxes the uniform background seamlessly over the panel.
- **Banners**: Home's banner pager draws its pages **in Compose**, never from photos. Each `banners` row carries `style` (green/beige/copper; blank/unknown renders green), `icon_key` (resolved by `ui/components/CategoryIcons.kt`; unknown renders the generic glyph), `badge` and `subtitle`, with the title in `titleLarge` and a low-alpha outlined glyph cropped at the end corner (mirrored in RTL). A banner with an `image_url` and **no title** falls back to the old photo behaviour; `link` stays unused (banners are not clickable).
- **Language**: Arabic-only. The default locale is Arabic and the UI is forced RTL (`LayoutDirection.Rtl` in `MainActivity`) regardless of the device language. Format prices with the `price_format` string (Shekel `₪`); bare amounts (totals, order lines) use `AmountText` + `currency_format` and `formatPriceValue` (Western digits, e.g. `1,500 ₪`).
- **Gradle**: Use version catalog aliases (`libs.*`)
- **Admin dashboard**: mirror the same design tokens, type roles and Arabic labels/formatting as the app (`web/src/app/globals.css`, `web/src/app/layout.tsx`, `web/src/lib/constants.ts`, `web/src/lib/format.ts`). Keep the UI Arabic + RTL and light-only.

## Architecture notes

- Single-activity Jetpack Compose app (`MainActivity` extends `ComponentActivity`, sets content with `GazaStorTheme`)
- `enableEdgeToEdge()` enabled, with light system-bar icons forced (the UI is light-only)
- **Theme:** light-only "Quiet Luxury" brand theme (`GazaStorTheme`); dynamic color is disabled. Typography roles (IBM Plex Sans Arabic, Amiri, Bodoni Moda, Inter) are defined in `ui/theme/Type.kt` and bundled in `res/font/`
- **Screens:** every data-driven screen consumes a `UiState` with `isLoading`/`isError`/`isEmpty` and renders shared `ui/components/` pieces for loading, error+retry, and empty states
- **Navigation:** `core/navigation/NavGraph.kt` owns the `Screen` route constants and shows the bottom bar only on its 5 destinations. Tabs navigate through `navigateToTab` (`popUpTo(Home) { saveState = true }` + `launchSingleTop` + `restoreState`), so each tab is a single entry whose `NavBackStackEntry` — and with it its ViewModels, `rememberSaveable` state and scroll position — is saved when you leave it and put back when you return, and no tab re-runs its initial load. **Home is the popUpTo anchor, not `graph.findStartDestination()`**: the start destination is Splash, which pops itself on first launch, and Navigation silently ignores a `popUpTo` target that is not on the back stack, so nothing was saved or restored and every tab tap pushed a fresh entry with a fresh ViewModelStore. Back from a tab therefore pops to Home instead of walking the visit history. `saveCurrentState = false` is used where a restorable entry would be stale (Checkout once its order is placed). Overlays like Detail hide the bar. Each tab carries an outlined icon for the unselected state and its filled twin when selected (green pill indicator unchanged); the Cart tab wears a copper count badge (white 11sp, hidden at 0, "9+" past nine) fed by the activity-scoped `CartViewModel`, which the tab screens (Home, Favorites, Cart, Search, List) also share instead of each creating their own. Re-tapping the already-selected Home tab animates its (nav-scoped) `LazyGridState` back to the top, and is a no-op when already there; a restored Home tab skips the staggered entrance (a `rememberSaveable` "already revealed" flag read once per appearance) so only its first appearance animates. Flow is Splash -> Onboarding -> Auth (both first-launch only) -> Home, then the tabs
- **Onboarding:** `SplashViewModel` reads the local flags (`OnboardingPreferences`/`AuthPreferences`, DataStore-backed `UserPreferences`) and routes to `onboarding`, `auth` or `home`; `OnboardingScreen` is a 3-page skippable `HorizontalPager` and `OnboardingViewModel.complete()` persists the flag *before* navigating so it can't reappear. Its second page renders `ui/components/EssentialsShowcase.kt`, a Compose transcription of a Stitch SVG export
- **Auth:** `AuthRepository` wraps supabase-kt Auth (email/password) behind `Result`; `AuthViewModel` owns the shared `AuthContent` form (used full-screen after onboarding and inside the Account tab). Sign-up adds a confirm-password field that must match (validated in `AuthViewModel`, `AuthError.PasswordMismatch`); sign-in ignores it. Choosing "متابعة كزائر" sets `auth_prompt_seen` and lets guests use the whole catalog; `AccountViewModel` mirrors `sessionState` and loads the `profiles` row
- **Auth visuals:** `AuthScreen`/`AuthContent` open with the shared `BrandHeader` (brand mark + lockup + copper rule, animated on the full-screen entry) above a hairline `Surface` card, with a segmented sign-in/sign-up switch, leading mail/lock icons and inline `AuthBanner` notices. Every CTA goes through `AppButton` (primary/outlined, `Dimens.ButtonHeight`); prefer it over raw Material `Button`/`OutlinedButton` in new screens
- **Account hub:** signed in, the Account tab shows an initials avatar, name/email/`phone`/"عضو منذ" from the `profiles` row, a live counts strip (orders / favorites / cart, tapping a cell switches tabs via `navigateToTab`) and a menu (orders, favorites, cart, WhatsApp support). The orange/copper `PillBadge` only appears for `role = 'admin'`. The hero also has a compact **تعديل** action that swaps the hub for an inline **edit form** (`EditProfileContent`) instead of pushing a route: name is required, the phone is optional but must pass `isValidPhone` (9–15 digits) and is written as SQL NULL when cleared, and the system Back gesture cancels. Save goes through `AuthRepository.updateProfile(fullName, phone)`, which sends **only** `full_name`/`phone` (never `role`, `id` or `created_at`) filtered by `auth.uid()`; the payload is `setToNull("phone")` for a cleared phone so the column is NULL, not the string "null" or "". Only success closes the form and emits `AccountEvent.ProfileSaved` for a snackbar; failure keeps the typed values with an inline copper message. Profile loading draws a `SkeletonLine` and a failed load keeps the email plus an inline retry. Sign out goes through an M3 `AlertDialog`; the button uses `AppButton(loading = isSigningOut)` and a failure emits `AccountEvent.SignOutFailed` (one-shot channel) for a snackbar. The orders stat is fetched by `refreshOrderCount()`, driven by `LifecycleResumeEffect(uiState.isSignedIn)` on the Account destination, so returning to the tab after placing an order updates it (an in-flight request is never duplicated; guests never fetch). The WhatsApp support row resolves and validates the configured number (digits 11-15) through `ui/util/WhatsAppLauncher`, with the same fallback as the payment flow; the numbers are never logged. Guests get the brand header + sign-in card
- **Local storage:** device-scoped flags live in `data/preferences` behind interfaces (fakeable in tests). `AppModule.init(context)` is called from `GazaStorApp` to give it an application context. Supabase session persistence uses supabase-kt's default settings-backed session manager (no extra wiring needed)
- **Grids:** catalog screens use `LazyVerticalGrid` (2 columns) with `fullWidthItem` for full-bleed sections; search debounces ~300 ms in `SearchViewModel` and queries Postgrest with an escaped `ilike` OR-filter. Cell chrome lives in `ItemGridCard` (hairline `Surface`, square image, 2-line title inside a `GridCardTextMinHeight` text block, copper price, favorite disc, and a quick-add `+` that morphs into a stepper). Loading and first-load errors share the same placeholder scaffolding: `ShimmerSkeleton`'s `SkeletonSection`/`SkeletonCard` mirror the real layout and pulse while loading, and stay **frozen** (alpha 1) behind the `ErrorState` card when a first load fails, so a failed request never leaves blank space. The Cart list keeps a plain `LoadingIndicator` (rows, not cards)
- **Home design:** the header is fixed, never scrolls: a solid-beige compact brand bar (32dp outlined mark + Amiri lockup, Scaffold `paddingValues` for status-bar insets — no hairline of its own; the scroll-faded separator below the pill is the only divider) sits above a pinned fully-rounded 46dp search pill, both outside the `LazyVerticalGrid` in a `Column` (`Modifier.weight(1f)` on the grid), so content passes beneath with no overlap. Tapping the pill pushes the Search screen, which focuses its field (`FocusRequester`) so the keyboard opens immediately; there is no recent-searches store. A 1dp separator at the top of the grid area fades in (200ms) once `firstVisibleItemIndex`/`offset` is past zero. Banners are a 150dp auto-advancing `HorizontalPager` (paused while dragging) with copper dots; categories are 72dp circular chips with a scroll-position-based edge fade (alpha, never a `Brush`) — each chip is a beige `surfaceVariant` disc with a hairline ring and a centered 32dp outlined green glyph picked by `categories.icon_key` through `ui/components/CategoryIcons.kt` (Material Icons Outlined, bundled offline as `res/drawable/ic_category_*.xml`); a key the app does not know falls back to a generic glyph, and a category that has an `image_url` but no key keeps the old image chip; product cells are the shared `ItemGridCard`. Sections/cells stagger in via `AnimatedVisibility` after the `ShimmerSkeleton` placeholders swap out. Pull-to-refresh (`PullToRefreshBox`, copper-on-beige indicator) calls `HomeViewModel.loadData()`, which flips `isRefreshing` instead of `isLoading` when content already exists — so the grid is **never** replaced by the skeleton on refresh, and a first-load error keeps the placeholders visible (frozen, no pulse) behind the retry card. `HomeContent(animateEntrance = false)` freezes the entrances for previews
- **Detail:** image gallery is a `HorizontalPager` over `ItemUi.images` with dot indicators. Detail receives the activity-scoped `CartViewModel` and displays the current cart quantity, matching Home. The shared `QuantityStepper` increments/decrements the cart atomically (decreasing to zero removes the line); at zero, an `AppButton` adds one item and shows a snackbar.
- **Cart:** device-local via `CartRepository`/`DataStoreCartRepository` (JSON array in its own DataStore file `gaza_stor_cart`, separate from `UserPreferences`' `gaza_stor_preferences` — two `preferencesDataStore` delegates must never share a file name). Lines merge per item, are removed at quantity 0, and guests can fill the cart. `CartRepository.increment(itemId, delta)` is the single atomic mutation used by both the Cart screen and the grid cards' quick-add control. `CartViewModel` merges cart lines with the auth session (`isSignedIn`) to decide whether checkout is allowed, and is activity-scoped so the tab badge and every quick-add share one owner
- **Checkout:** `CheckoutViewModel` requires a signed-in user, prefills name/phone from the `profiles` row, validates the delivery details and offers cash on delivery only. `OrderRepository.placeOrder` inserts the `orders` header with `user_id = auth.uid()` (required by the RLS insert policy) then the `order_items` lines; the cart is cleared only after both are accepted. Success emits a one-shot `CheckoutEvent.OrderPlaced` that navigates to Orders
- **Payment screen:** the checkout flow is a three-step bar (`ui/components/CheckoutStepIndicator.kt`: العنوان / الدفع / التأكيد — done = green dot, current = copper, upcoming = a hairline ring). The address form (`ui/screens/checkout`) is step 1 and ends with **متابعة الدفع**, which pushes a separate `Screen.Payment` route (step 2: `ui/screens/payment`) — a separate screen, not a section of the checkout, so the indicator stays honest. The payment route reuses the **checkout** `ViewModel` (`viewModel(viewModelStoreOwner = navController.getBackStackEntry(Screen.Checkout.route))`) for the totals and for the unchanged `placeOrder` flow, while `PaymentViewModel` (payment-scoped, no `SavedStateHandle`) owns only the form state. The four methods (cash / card / wallet / bank transfer) are radio cards whose selected one expands its form (`AnimatedVisibility`, 200 ms `FastOutSlowInEasing`); inline validation lives in **copper**, not red, and only appears after the field lost focus. The card number, expiry, CVV and wallet phone are **never persisted, never saved to a `SavedStateHandle` and never logged** — a process death simply drops them. The sticky bottom bar (outside the scroll area, 1 dp top hairline, `navigationBarsPadding().imePadding()`) carries "تأكيد الطلب • ₪ {total}" and stays disabled until the method's fields are valid (cash always is). **The chosen method is not written to the order**: `NewOrder` has no payment field, so every order still lands with `payment_method = 'cod'` in the dashboard. The DB check constraint is `payment_method in ('cod','online')`, so the only schema-legal mapping would be cash → `cod` and card/wallet/bank → `online` (a 2-line repository change, no schema change) — not done yet.
- **Orders:** `OrdersViewModel` loads on session changes; `getOrders` selects `*, order_items(*)` newest-first and maps statuses to Arabic labels. Guests see a `SignInRequired` prompt instead of an empty list
- **Delivery fee:** flat `Constants.DELIVERY_FEE` (10.0 ₪) added to every non-empty cart; a placeholder decision kept in one place so it can be changed or made remote later
- **Repository:** `ContentRepository` returns `Result` (never throws away errors); it does not swallow exceptions, so failures surface in the UI instead of showing an empty store
- **DI:** `core/di/AppModule` exposes app-wide singletons (`supabaseClient`, `contentRepository`, `authRepository`, `cartRepository`, `orderRepository`, `userPreferences`) and is initialised (`init(context)`) from `GazaStorApp`. ViewModels take them as default constructor args, so `viewModel()` resolves and tests can still pass fakes. Repositories are singletons so state shared across screens (auth session, cart) has one owner
- Current state: bottom navigation (5 tabs), splash + first-launch onboarding, Supabase email/password auth with a guest escape hatch and the Account hub (profile summary + editable name/phone + counts + shortcuts + support + confirmed sign out), two-column grids, image gallery, search, and the full cart -> checkout -> order-history flow are wired on top of the Supabase data layer. Schema is applied (`supabase/schema.sql`) with demo catalog rows, so Home/List/Detail/Search render real content once `SUPABASE_URL`/`SUPABASE_ANON_KEY` are set. COD only; no payment gateway yet in the app. The admin dashboard (`web/`) covers the full catalog (categories/items/banners, including Storage image upload) plus order management.
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
- **Showcase animation**: `ui/components/EssentialsShowcase.kt` keeps the Stitch export's 400x400 coordinate space so its numbers can be checked against the source SVG. CSS applies `animation-timing-function` to *each* keyframe segment, and Compose's `keyframes` spec defaults to a per-segment linear ramp, so `keyframesValue()` applies `CubicBezierEasing(0.42, 0, 0.58, 1)` (CSS `ease-in-out`) per segment — using `FastOutSlowInEasing` instead would drift from the original
- **WhatsApp order (Phase 1)**: new "الطلب عبر واتساب" payment method placed second in the Payment screen. On confirm, `CheckoutViewModel.submitForWhatsApp()` places the order through the same repository call (`payment_method` stays `cod`) and returns a `PlacedOrder` snapshot (order id + name/phone/address/lines/totals captured **before** the cart is cleared). `PaymentScreen` then builds the message and opens `https://wa.me/{number}?text={...}` via `ACTION_VIEW`. **Do not** move this hand-over back into a `LaunchedEffect` keyed on mutable state: an effect that flips its own keys is cancelled at the next suspension and silently drops the intent (this is exactly what broke the first version). The message is built by `WhatsAppOrderMessage.build()` (Arabic, ₪, Western digits via `Locale.US`, first 15 items + "و N أصناف أخرى", pre-filled text kept under 2000 chars) and the lines come from the snapshot, not from the insert response (which carries no `order_items`). If no app accepts the intent, `ActivityNotFoundException` shows the snackbar "تعذّر فتح واتساب، تم تسجيل طلبك" for 2 s and still navigates to Orders. No card data touched, no logging of phone/address/number. Non-WhatsApp methods keep using the `OrderPlaced` event channel.
- **app_settings table**: `public.app_settings(key PK, value, updated_at)` with RLS. Public SELECT only for exact key `whatsapp_number` (no LIKE). Admin write via `public.is_admin()` (mirrors banners policy). Seeded to `972567982362`. `SettingsRepository.getWhatsAppNumber()` caches in memory, validates digits 11-15, and falls back to `FALLBACK_WHATSAPP_NUMBER` (declared in `SettingsRepository.kt`, shared with the payment screen). Change later with `UPDATE public.app_settings SET value = 'NEW_NUMBER' WHERE key = 'whatsapp_number'`.

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
- [x] Email/password auth with a "continue as guest" escape hatch, account hub (profile summary + counts + shortcuts + support + confirmed sign out) and Supabase session persistence
- [x] Implement cart, checkout and order history screens
- [x] Create admin dashboard (location/tech TBD; document in README if separate repo)
- [x] Promote first admin (`update public.profiles set role='admin' where id=...`)
- [x] Add Storage upload flow for product images
- [ ] Define app purpose/features fully
- [ ] Establish git workflow if using git
