import { AlertTriangle } from "lucide-react";
import { cn } from "@/lib/utils";

export function ErrorNotice({
  message,
  className,
}: {
  message: string;
  className?: string;
}) {
  return (
    <div
      role="alert"
      className={cn(
        "flex items-start gap-2 rounded-xl border border-destructive/30 bg-brand-error-container px-4 py-3 text-sm text-brand-error-on",
        className,
      )}
    >
      <AlertTriangle className="mt-0.5 size-4 shrink-0" />
      <span>{message}</span>
    </div>
  );
}
