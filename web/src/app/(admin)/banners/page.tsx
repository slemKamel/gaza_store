import { ImageIcon } from "lucide-react";
import type { Metadata } from "next";
import { createClient } from "@/lib/supabase/server";
import { PageHeader } from "@/components/admin/page-header";
import { ErrorNotice } from "@/components/admin/error-notice";
import { DeleteButton } from "@/components/admin/delete-button";
import { Badge } from "@/components/ui/badge";
import { BannerFormDialog } from "./banner-form-dialog";
import { deleteBanner, saveBanner } from "./actions";

export const metadata: Metadata = { title: "البانرات" };

export default async function BannersPage() {
  const supabase = await createClient();
  const { data: banners, error } = await supabase
    .from("banners")
    .select("id, title, image_url, link, sort_order, is_active")
    .order("sort_order", { ascending: true })
    .order("created_at", { ascending: false });

  return (
    <>
      <PageHeader
        title="البانرات"
        description="الصور الترويجية التي تظهر في الصفحة الرئيسية للتطبيق."
        action={<BannerFormDialog action={saveBanner} />}
      />

      {error ? (
        <ErrorNotice message={error.message} />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {banners && banners.length > 0 ? (
            banners.map((banner) => (
              <div
                key={banner.id}
                className="overflow-hidden rounded-xl border border-border bg-card"
              >
                <div className="flex h-32 items-center justify-center bg-brand-variant">
                  {banner.image_url ? (
                    <img
                      src={banner.image_url}
                      alt=""
                      className="size-full object-cover"
                    />
                  ) : (
                    <ImageIcon className="size-6 text-muted-foreground" />
                  )}
                </div>
                <div className="flex items-start justify-between gap-2 p-4">
                  <div className="min-w-0">
                    <p className="truncate font-medium">
                      {banner.title || "بدون عنوان"}
                    </p>
                    <div className="mt-1 flex items-center gap-2">
                      {banner.is_active ? (
                        <Badge variant="success">مُفعّل</Badge>
                      ) : (
                        <Badge variant="outline">مخفي</Badge>
                      )}
                      <span className="text-xs text-muted-foreground">
                        ترتيب {banner.sort_order}
                      </span>
                    </div>
                    {banner.link ? (
                      <p
                        className="mt-2 truncate text-xs text-muted-foreground"
                        dir="ltr"
                      >
                        {banner.link}
                      </p>
                    ) : null}
                  </div>
                  <div className="flex shrink-0 items-center gap-1">
                    <BannerFormDialog banner={banner} action={saveBanner} />
                    <DeleteButton
                      id={banner.id}
                      action={deleteBanner}
                      title="حذف البانر"
                      description="سيتم حذف هذا البانر نهائيًا. لا يمكن التراجع."
                    />
                  </div>
                </div>
              </div>
            ))
          ) : (
            <p className="rounded-xl border border-border bg-card p-6 text-sm text-muted-foreground">
              لا توجد بانرات بعد. أضف أول بانر من الزر أعلاه.
            </p>
          )}
        </div>
      )}
    </>
  );
}
