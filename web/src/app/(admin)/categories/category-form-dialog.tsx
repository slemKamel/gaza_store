"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { Loader2, Pencil, Plus } from "lucide-react";
import type { ActionResult } from "@/lib/actions";
import type { CategoryInput } from "@/lib/types";
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

export type CategoryRow = {
  id: string;
  name: string;
  slug: string;
  image_url: string | null;
  sort_order: number;
  is_active: boolean;
};

export function CategoryFormDialog({
  category,
  action,
}: {
  category?: CategoryRow;
  action: (input: CategoryInput) => Promise<ActionResult>;
}) {
  const router = useRouter();
  const isEdit = Boolean(category);

  const [open, setOpen] = React.useState(false);
  const [name, setName] = React.useState(category?.name ?? "");
  const [slug, setSlug] = React.useState(category?.slug ?? "");
  const [imageUrl, setImageUrl] = React.useState<string | null>(
    category?.image_url ?? null,
  );
  const [sortOrder, setSortOrder] = React.useState(
    String(category?.sort_order ?? 0),
  );
  const [isActive, setIsActive] = React.useState(category?.is_active ?? true);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  function reset() {
    setName(category?.name ?? "");
    setSlug(category?.slug ?? "");
    setImageUrl(category?.image_url ?? null);
    setSortOrder(String(category?.sort_order ?? 0));
    setIsActive(category?.is_active ?? true);
    setError(null);
  }

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError(null);

    const result = await action({
      id: category?.id,
      name,
      slug,
      image_url: imageUrl,
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
            قسم جديد
          </Button>
        )}
      </DialogTrigger>

      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? "تعديل قسم" : "قسم جديد"}</DialogTitle>
        </DialogHeader>

        <form onSubmit={onSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="category-name">الاسم</Label>
            <Input
              id="category-name"
              value={name}
              onChange={(event) => setName(event.target.value)}
              required
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="category-slug">المعرّف (slug)</Label>
            <Input
              id="category-slug"
              value={slug}
              onChange={(event) => setSlug(event.target.value)}
              dir="ltr"
              placeholder="electronics"
              className="text-start"
              required
            />
            <span className="text-xs text-muted-foreground">
              أحرف إنجليزية صغيرة وأرقام وشرطات، ويجب أن يكون فريدًا.
            </span>
          </div>

          <ImageUploadField
            label="صورة القسم"
            value={imageUrl}
            onChange={setImageUrl}
            hint="اختياري — تُستخدم في تبويب الأقسام داخل التطبيق."
          />

          <div className="flex flex-col gap-2">
            <Label htmlFor="category-order">ترتيب العرض</Label>
            <Input
              id="category-order"
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
