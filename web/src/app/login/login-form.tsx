"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { Loader2 } from "lucide-react";
import { createClient } from "@/lib/supabase/client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export function LoginForm({ next }: { next: string }) {
  const router = useRouter();
  const [email, setEmail] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [error, setError] = React.useState<string | null>(null);
  const [pending, startTransition] = React.useTransition();
  const [submitting, setSubmitting] = React.useState(false);

  const busy = submitting || pending;

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    const supabase = createClient();
    const { error: signInError } = await supabase.auth.signInWithPassword({
      email: email.trim(),
      password,
    });

    if (signInError) {
      setError("تعذّر تسجيل الدخول. تأكد من البريد الإلكتروني وكلمة المرور.");
      setSubmitting(false);
      return;
    }

    startTransition(() => {
      router.replace(next);
      router.refresh();
    });
  }

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-5">
      <div className="flex flex-col gap-2">
        <Label htmlFor="email">البريد الإلكتروني</Label>
        <Input
          id="email"
          type="email"
          inputMode="email"
          autoComplete="email"
          dir="ltr"
          required
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="admin@example.com"
          className="text-start"
        />
      </div>

      <div className="flex flex-col gap-2">
        <Label htmlFor="password">كلمة المرور</Label>
        <Input
          id="password"
          type="password"
          autoComplete="current-password"
          dir="ltr"
          required
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          className="text-start"
        />
      </div>

      {error ? (
        <p
          role="alert"
          className="rounded-md border border-destructive/30 bg-brand-error-container px-3 py-2 text-sm text-brand-error-on"
        >
          {error}
        </p>
      ) : null}

      <Button type="submit" size="lg" disabled={busy} className="w-full">
        {busy ? <Loader2 className="size-4 animate-spin" /> : null}
        تسجيل الدخول
      </Button>
    </form>
  );
}
