"use client";

import * as React from "react";
import { ImagePlus, Loader2, X } from "lucide-react";
import { createClient } from "@/lib/supabase/client";
import { MAX_IMAGE_BYTES, PRODUCT_IMAGES_BUCKET } from "@/lib/constants";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

async function uploadImage(file: File): Promise<string> {
  const supabase = createClient();
  const ext = file.name.includes(".")
    ? file.name.split(".").pop()!.toLowerCase()
    : "jpg";
  const path = `${new Date().getFullYear()}/${crypto.randomUUID()}.${ext}`;

  const { error } = await supabase.storage
    .from(PRODUCT_IMAGES_BUCKET)
    .upload(path, file, {
      cacheControl: "31536000",
      upsert: false,
      contentType: file.type || undefined,
    });
  if (error) throw new Error(error.message);

  return supabase.storage.from(PRODUCT_IMAGES_BUCKET).getPublicUrl(path).data
    .publicUrl;
}

function validateImage(file: File): string | null {
  if (!file.type.startsWith("image/")) return "الملف المختار ليس صورة.";
  if (file.size > MAX_IMAGE_BYTES) return "حجم الصورة يتجاوز 5 ميجابايت.";
  return null;
}

function useUploader(onUploaded: (url: string) => void) {
  const inputRef = React.useRef<HTMLInputElement>(null);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  async function pick(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;

    const problem = validateImage(file);
    if (problem) {
      setError(problem);
      return;
    }

    setError(null);
    setBusy(true);
    try {
      onUploaded(await uploadImage(file));
    } catch {
      setError("تعذّر رفع الصورة. حاول مرة أخرى.");
    } finally {
      setBusy(false);
    }
  }

  return { inputRef, busy, error, pick };
}

/** Single image: upload/replace/remove thumbnails, category and banner images. */
export function ImageUploadField({
  value,
  onChange,
  label,
  hint,
}: {
  value: string | null;
  onChange: (url: string | null) => void;
  label: string;
  hint?: string;
}) {
  const { inputRef, busy, error, pick } = useUploader((url) => onChange(url));

  return (
    <div className="flex flex-col gap-2">
      <span className="text-sm font-medium">{label}</span>
      <div className="flex items-start gap-3">
        <div className="relative flex size-20 shrink-0 items-center justify-center overflow-hidden rounded-md border border-border bg-brand-variant">
          {value ? (
            <img src={value} alt="" className="size-full object-cover" />
          ) : (
            <ImagePlus className="size-5 text-muted-foreground" />
          )}
        </div>
        <div className="flex flex-col gap-2">
          <div className="flex flex-wrap gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              disabled={busy}
              onClick={() => inputRef.current?.click()}
            >
              {busy ? (
                <Loader2 className="size-4 animate-spin" />
              ) : (
                <ImagePlus className="size-4" />
              )}
              {value ? "تغيير الصورة" : "اختيار صورة"}
            </Button>
            {value ? (
              <Button
                type="button"
                variant="ghost"
                size="sm"
                disabled={busy}
                onClick={() => onChange(null)}
              >
                <X className="size-4" />
                إزالة
              </Button>
            ) : null}
          </div>
          {hint ? (
            <span className="text-xs text-muted-foreground">{hint}</span>
          ) : null}
          {error ? (
            <span className="text-xs text-destructive">{error}</span>
          ) : null}
        </div>
      </div>
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={pick}
      />
    </div>
  );
}

/** Multiple images: the item gallery (`items.images`). */
export function GalleryUploadField({
  value,
  onChange,
  label = "صور المعرض",
  hint,
}: {
  value: string[];
  onChange: (urls: string[]) => void;
  label?: string;
  hint?: string;
}) {
  const { inputRef, busy, error, pick } = useUploader((url) =>
    onChange([...value, url]),
  );

  function removeAt(index: number) {
    onChange(value.filter((_, i) => i !== index));
  }

  return (
    <div className="flex flex-col gap-2">
      <span className="text-sm font-medium">{label}</span>
      <div className="flex flex-wrap gap-3">
        {value.map((url, index) => (
          <div
            key={`${url}-${index}`}
            className="relative size-20 overflow-hidden rounded-md border border-border bg-brand-variant"
          >
            <img src={url} alt="" className="size-full object-cover" />
            <button
              type="button"
              onClick={() => removeAt(index)}
              aria-label="إزالة الصورة"
              className="absolute end-1 top-1 flex size-5 items-center justify-center rounded-full bg-black/60 text-white transition-colors hover:bg-black/80"
            >
              <X className="size-3" />
            </button>
          </div>
        ))}
        <button
          type="button"
          onClick={() => inputRef.current?.click()}
          disabled={busy}
          className={cn(
            "flex size-20 items-center justify-center rounded-md border border-dashed border-border text-muted-foreground transition-colors hover:border-brand-copper hover:text-brand-copper",
            busy && "opacity-50",
          )}
          aria-label="إضافة صورة"
        >
          {busy ? (
            <Loader2 className="size-5 animate-spin" />
          ) : (
            <ImagePlus className="size-5" />
          )}
        </button>
      </div>
      {hint ? <span className="text-xs text-muted-foreground">{hint}</span> : null}
      {error ? <span className="text-xs text-destructive">{error}</span> : null}
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={pick}
      />
    </div>
  );
}
