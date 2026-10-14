"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { Loader2, LogOut } from "lucide-react";
import { createClient } from "@/lib/supabase/client";
import { Button } from "@/components/ui/button";

export function SignOutButton({
  className,
  variant = "ghost",
  showLabel = true,
}: {
  className?: string;
  variant?: "ghost" | "outline" | "default";
  showLabel?: boolean;
}) {
  const router = useRouter();
  const [busy, setBusy] = React.useState(false);

  async function signOut() {
    setBusy(true);
    const supabase = createClient();
    await supabase.auth.signOut();
    router.replace("/login");
    router.refresh();
  }

  return (
    <Button
      type="button"
      variant={variant}
      onClick={signOut}
      disabled={busy}
      className={className}
      aria-label={showLabel ? undefined : "تسجيل الخروج"}
    >
      {busy ? (
        <Loader2 className="size-4 animate-spin" />
      ) : (
        <LogOut className="size-4" />
      )}
      {showLabel ? "تسجيل الخروج" : null}
    </Button>
  );
}
