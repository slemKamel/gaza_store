import type { ReactNode } from "react";

export function StatCard({
  label,
  value,
  hint,
}: {
  label: string;
  value: ReactNode;
  hint?: string;
}) {
  return (
    <div className="rounded-xl border border-border bg-card p-4">
      <span className="text-sm text-muted-foreground">{label}</span>
      <div className="mt-2 text-2xl font-bold text-foreground font-latin">
        {value}
      </div>
      {hint ? (
        <div className="mt-1 text-xs text-brand-copper">{hint}</div>
      ) : null}
    </div>
  );
}
