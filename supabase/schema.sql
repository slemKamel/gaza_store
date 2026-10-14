-- ============================================================
-- Gaza Stor :: database schema (source of truth)
-- ------------------------------------------------------------
-- Applied to project `foowyawahaivprdjzfwv` via the Supabase MCP
-- server as migrations:
--   1. initial_schema
--   2. rls_policies_and_storage
--   3. harden_trigger_search_path_and_execute
-- Keep this file in sync when the schema changes.
-- ============================================================

-- ---------- helpers ----------
create or replace function public.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

-- ---------- categories ----------
create table public.categories (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  slug text not null unique,
  image_url text,
  sort_order integer not null default 0,
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

-- ---------- items ----------
create table public.items (
  id uuid primary key default gen_random_uuid(),
  category_id uuid not null references public.categories(id) on delete cascade,
  title text not null,
  description text,
  price numeric(10,2) not null default 0 check (price >= 0),
  image_url text,
  images text[] not null default '{}',
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index items_category_id_idx on public.items(category_id);
create trigger items_set_updated_at
  before update on public.items
  for each row execute function public.set_updated_at();

-- ---------- banners ----------
create table public.banners (
  id uuid primary key default gen_random_uuid(),
  title text,
  image_url text,
  link text,
  sort_order integer not null default 0,
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

-- ---------- profiles (1:1 with auth.users) ----------
create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null default '',
  phone text,
  role text not null default 'customer' check (role in ('customer','admin')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create trigger profiles_set_updated_at
  before update on public.profiles
  for each row execute function public.set_updated_at();

-- is_admin(): used by RLS policies; SECURITY DEFINER so it can read
-- profiles without recursing into its own RLS policy.
create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.profiles
    where id = auth.uid() and role = 'admin'
  );
$$;

-- block role escalation by non-admins
create or replace function public.prevent_role_escalation()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if new.role is distinct from old.role and not public.is_admin() then
    raise exception 'Only admins can change the role';
  end if;
  return new;
end;
$$;
create trigger profiles_prevent_role_escalation
  before update on public.profiles
  for each row execute function public.prevent_role_escalation();

-- auto-create a profile row on sign-up
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, full_name, phone)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'full_name', ''),
    new.raw_user_meta_data->>'phone'
  )
  on conflict (id) do nothing;
  return new;
end;
$$;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- trigger-only functions must not be callable over the API
revoke execute on function public.handle_new_user() from public, anon, authenticated;
revoke execute on function public.prevent_role_escalation() from public, anon, authenticated;

-- ---------- orders ----------
create table public.orders (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete restrict,
  status text not null default 'pending'
    check (status in ('pending','confirmed','shipped','delivered','cancelled')),
  payment_method text not null default 'cod' check (payment_method in ('cod','online')),
  payment_status text not null default 'unpaid' check (payment_status in ('unpaid','paid','refunded')),
  payment_ref text,
  subtotal numeric(10,2) not null default 0,
  delivery_fee numeric(10,2) not null default 0,
  total numeric(10,2) not null default 0,
  customer_name text not null default '',
  phone text,
  address text,
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index orders_user_id_idx on public.orders(user_id);
create index orders_status_idx on public.orders(status);
create trigger orders_set_updated_at
  before update on public.orders
  for each row execute function public.set_updated_at();

-- ---------- order_items ----------
create table public.order_items (
  id uuid primary key default gen_random_uuid(),
  order_id uuid not null references public.orders(id) on delete cascade,
  item_id uuid references public.items(id) on delete set null,
  title text not null,                          -- snapshot at purchase time
  unit_price numeric(10,2) not null check (unit_price >= 0),
  quantity integer not null default 1 check (quantity > 0),
  line_total numeric(10,2) generated always as (unit_price * quantity) stored,
  created_at timestamptz not null default now()
);
create index order_items_order_id_idx on public.order_items(order_id);

-- ============================================================
-- Row Level Security
-- ============================================================
alter table public.categories  enable row level security;
alter table public.items       enable row level security;
alter table public.banners     enable row level security;
alter table public.profiles    enable row level security;
alter table public.orders      enable row level security;
alter table public.order_items enable row level security;

-- catalog: public read (active only), admin write
create policy categories_public_read on public.categories
  for select using (is_active or public.is_admin());
create policy categories_admin_insert on public.categories
  for insert with check (public.is_admin());
create policy categories_admin_update on public.categories
  for update using (public.is_admin()) with check (public.is_admin());
create policy categories_admin_delete on public.categories
  for delete using (public.is_admin());

create policy items_public_read on public.items
  for select using (is_active or public.is_admin());
create policy items_admin_insert on public.items
  for insert with check (public.is_admin());
create policy items_admin_update on public.items
  for update using (public.is_admin()) with check (public.is_admin());
create policy items_admin_delete on public.items
  for delete using (public.is_admin());

create policy banners_public_read on public.banners
  for select using (is_active or public.is_admin());
create policy banners_admin_insert on public.banners
  for insert with check (public.is_admin());
create policy banners_admin_update on public.banners
  for update using (public.is_admin()) with check (public.is_admin());
create policy banners_admin_delete on public.banners
  for delete using (public.is_admin());

-- profiles
create policy profiles_select_own_or_admin on public.profiles
  for select using (id = auth.uid() or public.is_admin());
create policy profiles_update_own_or_admin on public.profiles
  for update using (id = auth.uid() or public.is_admin())
  with check (id = auth.uid() or public.is_admin());
create policy profiles_admin_insert on public.profiles
  for insert with check (public.is_admin());
create policy profiles_admin_delete on public.profiles
  for delete using (public.is_admin());

-- orders
create policy orders_insert_own on public.orders
  for insert with check (user_id = auth.uid());
create policy orders_select_own_or_admin on public.orders
  for select using (user_id = auth.uid() or public.is_admin());
create policy orders_admin_update on public.orders
  for update using (public.is_admin()) with check (public.is_admin());
create policy orders_admin_delete on public.orders
  for delete using (public.is_admin());

-- order_items
create policy order_items_select on public.order_items
  for select using (
    exists (
      select 1 from public.orders o
      where o.id = order_items.order_id
        and (o.user_id = auth.uid() or public.is_admin())
    )
  );
create policy order_items_insert on public.order_items
  for insert with check (
    exists (
      select 1 from public.orders o
      where o.id = order_items.order_id and o.user_id = auth.uid()
    )
  );
create policy order_items_admin_update on public.order_items
  for update using (public.is_admin()) with check (public.is_admin());
create policy order_items_admin_delete on public.order_items
  for delete using (public.is_admin());

-- grants
grant usage on schema public to anon, authenticated;
grant select on public.categories, public.items, public.banners to anon;
grant select, insert, update, delete on public.categories, public.items, public.banners to authenticated;
grant select, insert, update, delete on public.profiles to authenticated;
grant select, insert, update on public.orders to authenticated;
grant select, insert, update, delete on public.order_items to authenticated;

-- ============================================================
-- Storage
-- ============================================================
insert into storage.buckets (id, name, public)
values ('product-images', 'product-images', true)
on conflict (id) do nothing;

create policy product_images_public_read on storage.objects
  for select using (bucket_id = 'product-images');
create policy product_images_admin_insert on storage.objects
  for insert with check (bucket_id = 'product-images' and public.is_admin());
create policy product_images_admin_update on storage.objects
  for update using (bucket_id = 'product-images' and public.is_admin());
create policy product_images_admin_delete on storage.objects
  for delete using (bucket_id = 'product-images' and public.is_admin());
