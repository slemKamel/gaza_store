"use client";

import * as React from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  GalleryUploadField,
  ImageUploadField,
} from "@/components/admin/image-upload-field";
import { saveItem } from "./actions";

export type CategoryOption = { id: string; name: string };

export type ItemFormValues = {
  id: string;
  category_id: string;
  title: string;
  description: string | null;
  price: number;
  image_url: string | null;
  images: string[];
  is_active: boolean;
};

export function ItemForm({
  categories,
  item,
}: {
  categories: CategoryOption[];
  item?: ItemFormValues;
}) {
  const router = useRouter();
  const isEdit = Boolean(item);

  const [categoryId, setCategoryId] = React.useState(
    item?.category_id ?? categories[0]?.id ?? "",
  );
  const [title, setTitle] = React.useState(item?.title ?? "");
  const [description, setDescription] = React.useState(item?.description ?? "");
  const [price, setPrice] = React.useState(item ? String(item.price) : "");
  const [imageUrl, setImageUrl] = React.useState<string | null>(
    item?.image_url ?? null,
  );
  const [images, setImages] = React.useState<string[]>(item?.images ?? []);
  const [isActive, setIsActive] = React.useState(item?.is_active ?? true);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError(null);

    const result = await saveItem({
      id: item?.id,
      category_id: categoryId,
      title,
      description: description || null,
      price: Number(price),
      image_url: imageUrl,
      images,
      is_active: isActive,
    });

    if (result.ok) {
      router.push("/items");
      router.refresh();
      return;
    }

    setError(result.error);
    setBusy(false);
  }

  if (categories.length === 0) {
    return (
      <div className="rounded-xl border border-border bg-card p-6 text-sm">
        <p className="mb-3 text-muted-foreground">
          لا يمكن إضافة منتجات قبل إنشاء قسم واحد على الأقل.
        </p>
        <Button asChild>
          <Link href="/categories">إدارة الأقسام</Link>
        </Button>
      </div>
    );
  }

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-6">
      <Card>
        <CardHeader>
          <CardTitle>المعلومات الأساسية</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="item-title">اسم المنتج</Label>
            <Input
              id="item-title"
              value={title}
              onChange={(event) => setTitle(event.target.value)}
              required
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="item-description">الوصف</Label>
            <Textarea
              id="item-description"
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              rows={4}
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="flex flex-col gap-2">
              <Label htmlFor="item-category">القسم</Label>
              <Select value={categoryId} onValueChange={setCategoryId}>
                <SelectTrigger id="item-category" className="w-full">
                  <SelectValue placeholder="اختر قسمًا" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((category) => (
                    <SelectItem key={category.id} value={category.id}>
                      {category.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="item-price">السعر (₪)</Label>
              <Input
                id="item-price"
                type="number"
                min="0"
                step="0.01"
                inputMode="decimal"
                value={price}
                onChange={(event) => setPrice(event.target.value)}
                required
              />
            </div>
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
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>الصور</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-6">
          <ImageUploadField
            label="الصورة الرئيسية"
            value={imageUrl}
            onChange={setImageUrl}
            hint="تظهر في شبكة المنتجات داخل التطبيق."
          />
          <GalleryUploadField
            value={images}
            onChange={setImages}
            hint="تظهر في معرض صور صفحة المنتج."
          />
        </CardContent>
      </Card>

      {error ? (
        <p
          role="alert"
          className="rounded-md border border-destructive/30 bg-brand-error-container px-3 py-2 text-sm text-brand-error-on"
        >
          {error}
        </p>
      ) : null}

      <div className="flex gap-2">
        <Button type="submit" disabled={busy}>
          {busy ? <Loader2 className="size-4 animate-spin" /> : null}
          {isEdit ? "حفظ التعديلات" : "إضافة المنتج"}
        </Button>
        <Button
          type="button"
          variant="outline"
          onClick={() => router.push("/items")}
          disabled={busy}
        >
          إلغاء
        </Button>
      </div>
    </form>
  );
}
