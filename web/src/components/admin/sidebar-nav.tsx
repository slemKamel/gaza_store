"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  ClipboardList,
  FolderTree,
  Image as ImageIcon,
  LayoutDashboard,
  Package,
} from "lucide-react";
import { cn } from "@/lib/utils";

const NAV = [
  { href: "/dashboard", label: "لوحة التحكم", icon: LayoutDashboard },
  { href: "/categories", label: "الأقسام", icon: FolderTree },
  { href: "/items", label: "المنتجات", icon: Package },
  { href: "/banners", label: "البانرات", icon: ImageIcon },
  { href: "/orders", label: "الطلبات", icon: ClipboardList },
] as const;

export function SidebarNav({
  variant = "desktop",
}: {
  variant?: "desktop" | "mobile";
}) {
  const pathname = usePathname();

  return (
    <nav
      className={cn(
        variant === "mobile"
          ? "flex gap-1 overflow-x-auto px-2 pb-2"
          : "flex flex-col gap-1",
      )}
    >
      {NAV.map(({ href, label, icon: Icon }) => {
        const active = pathname === href || pathname.startsWith(`${href}/`);
        return (
          <Link
            key={href}
            href={href}
            className={cn(
              "flex items-center gap-2 whitespace-nowrap rounded-md px-3 py-2 text-sm font-medium transition-colors",
              active
                ? "bg-primary text-primary-foreground"
                : "text-muted-foreground hover:bg-accent hover:text-accent-foreground",
            )}
          >
            <Icon className="size-4" />
            {label}
          </Link>
        );
      })}
    </nav>
  );
}
