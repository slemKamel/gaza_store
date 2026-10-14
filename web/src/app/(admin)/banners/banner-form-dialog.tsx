"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { Loader2, Pencil, Plus } from "lucide-react";
import type { ActionResult } from "@/lib/actions";
import type { BannerInput } from "@/lib/types";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ImageUploadField } from "@/components/admin/image-upload-field";

export type BannerRow = {
  id: string;
  title: string | null;
  image_url: string | null;
  link: string | null;
  sort_order: number;
  is_active: boolean;
};

export function BannerFormDialog({
  banner,
  action,
}: {
  banner?: BannerRow;
  action: (input: BannerInput) => Promise<ActionResult>;
}) {
  const router = useRouter();
  const isEdit = Boolean(banner);

  const [open, setOpen] = React.useState(false);
  const [title, setTitle] = React.useState(banner?.title ?? "");
  const [imageUrl, setImageUrl] = React.useState<string | null>(
    banner?.image_url ?? null,
  );
  const [link, setLink] = React.useState(banner?.link ?? "");
  const [sortOrder, setSortOrder] = React.useState(
    String(banner?.sort_order ?? 0),
  );
  const [isActive, setIsActive] = React.useState(banner?.is_active ?? true);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  function reset() {
    setTitle(banner?.title ?? "");
    setImageUrl(banner?.image_url ?? null);
    setLink(banner?.link ?? "");
    setSortOrder(String(banner?.sort_order ?? 0));
    setIsActive(banner?.is_active ?? true);
    setError(null);
  }

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError(null);

    const result = await action({
      id: banner?.id,
      title: title || null,
      image_url: imageUrl,
      link: link || null,
      sort_order: Number.parseInt(sortOrder, 10) || 0,
      is_active: isActive,
    });

    if (result.ok) {
      setOpen(false);
      reset();
      router.refresh();
    } else {
      setError(result.error);
    }
    setBusy(false);
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        setOpen(next);
        if (next) reset();
      }}
    >
      <DialogTrigger asChild>
        {isEdit ? (
          <Button variant="ghost" size="icon" aria-label="تعديل">
            <Pencil className="size-4" />
          </Button>
        ) : (
          <Button>
            <Plus className="size-4" />
            بانر جديد
          </Button>
        )}
      </DialogTrigger>

      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? "تعديل بانر" : "بانر جديد"}</DialogTitle>
        </DialogHeader>

        <form onSubmit={onSubmit} className="flex flex-col gap-4">
          <ImageUploadField
            label="صورة البانر"
            value={imageUrl}
            onChange={setImageUrl}
            hint="يُفضّل صورة عريضة (نسبة 16:6 تقريبًا)."
          />

          <div className="flex flex-col gap-2">
            <Label htmlFor="banner-title">العنوان</Label>
            <Input
              id="banner-title"
              value={title}
              onChange={(event) => setTitle(event.target.value)}
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="banner-link">الرابط</Label>
            <Input
              id="banner-link"
              value={link}
              onChange={(event) => setLink(event.target.value)}
              dir="ltr"
              placeholder="https://…"
              className="text-start"
            />
            <span className="text-xs text-muted-foreground">اختياري.</span>
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="banner-order">ترتيب العرض</Label>
            <Input
              id="banner-order"
              type="number"
              value={sortOrder}
              onChange={(event) => setSortOrder(event.target.value)}
            />
          </div>

          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={isActive}
              onChange={(event) => setIsActive(event.target.checked)}
              className="size-4 accent-primary"
            />
            مُفعّل (يظهر في التطبيق)
          </label>

          {error ? (
            <p
              role="alert"
              className="rounded-md border border-destructive/30 bg-brand-error-container px-3 py-2 text-sm text-brand-error-on"
            >
              {error}
            </p>
          ) : null}

          <DialogFooter>
            <Button type="submit" disabled={busy}>
              {busy ? <Loader2 className="size-4 animate-spin" /> : null}
              حفظ
            </Button>
            <Button
              type="button"
              variant="outline"
              onClick={() => setOpen(false)}
              disabled={busy}
            >
              إلغاء
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
