import Link from "next/link";
import { ChevronLeft, ChevronRight } from "lucide-react";

type Href = React.ComponentProps<typeof Link>["href"];

function PageLink({
  href,
  label,
  icon,
}: {
  href: Href | null;
  label: string;
  icon: React.ReactNode;
}) {
  if (!href) {
    return (
      <span className="inline-flex h-9 items-center gap-1 rounded-md border border-border px-3 text-sm text-muted-foreground/50">
        {icon}
        {label}
      </span>
    );
  }

  return (
    <Link
      href={href}
      className="inline-flex h-9 items-center gap-1 rounded-md border border-border px-3 text-sm font-medium transition-colors hover:bg-accent"
    >
      {icon}
      {label}
    </Link>
  );
}

export function Pagination({
  page,
  prevHref,
  nextHref,
}: {
  page: number;
  prevHref: Href | null;
  nextHref: Href | null;
}) {
  return (
    <div className="mt-4 flex items-center justify-between gap-2">
      <span className="text-sm text-muted-foreground">صفحة {page}</span>
      <div className="flex gap-2">
        <PageLink
          href={prevHref}
          label="السابق"
          icon={<ChevronRight className="size-4" />}
        />
        <PageLink
          href={nextHref}
          label="التالي"
          icon={<ChevronLeft className="size-4" />}
        />
      </div>
    </div>
  );
}
