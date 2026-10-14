/**
 * Input shapes shared by the admin forms and their server actions. Kept out of
 * the "use server" modules (which may only export async functions).
 */

export type CategoryInput = {
  id?: string;
  name: string;
  slug: string;
  image_url: string | null;
  sort_order: number;
  is_active: boolean;
};

export type ItemInput = {
  id?: string;
  category_id: string;
  title: string;
  description: string | null;
  price: number;
  image_url: string | null;
  images: string[];
  is_active: boolean;
};

export type BannerInput = {
  id?: string;
  title: string | null;
  image_url: string | null;
  link: string | null;
  sort_order: number;
  is_active: boolean;
};

export type OrderUpdateInput = {
  id: string;
  status: string;
  payment_status: string;
};
