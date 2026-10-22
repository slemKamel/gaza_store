-- Migration: Add app_settings table for runtime configuration
-- Date: 2026-10-22
--
-- Applied to the remote project on 2026-10-22 and mirrored in
-- supabase/schema.sql. Kept idempotent so it can be re-applied safely.

-- Create app_settings table for runtime configuration
CREATE TABLE IF NOT EXISTS public.app_settings (
  key text PRIMARY KEY,
  value text NOT NULL,
  updated_at timestamptz NOT NULL DEFAULT now()
);

-- Keep updated_at honest on every update (shared helper from schema.sql)
DROP TRIGGER IF EXISTS app_settings_set_updated_at ON public.app_settings;
CREATE TRIGGER app_settings_set_updated_at
  BEFORE UPDATE ON public.app_settings
  FOR EACH ROW
  EXECUTE FUNCTION public.set_updated_at();

-- Enable RLS
ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;

-- Public read-only for EXACT whitelisted key (no LIKE to avoid future key exposure)
DROP POLICY IF EXISTS "Public can read whatsapp number" ON public.app_settings;
CREATE POLICY "Public can read whatsapp number"
  ON public.app_settings
  FOR SELECT
  TO anon, authenticated
  USING (key = 'whatsapp_number');

-- Admin writes only (mirror banners pattern using existing is_admin())
DROP POLICY IF EXISTS "Admins can manage app settings" ON public.app_settings;
CREATE POLICY "Admins can manage app settings"
  ON public.app_settings
  FOR ALL
  TO authenticated
  USING (public.is_admin())
  WITH CHECK (public.is_admin());

-- Seed the WhatsApp number
INSERT INTO public.app_settings (key, value)
VALUES ('whatsapp_number', '972567982362')
ON CONFLICT (key) DO NOTHING;
