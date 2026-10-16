import type { ReactNode } from "react";
import type { AdminSession } from "@/lib/auth";
import { SidebarNav } from "./sidebar-nav";
import { SignOutButton } from "./sign-out-button";

function Brand() {
  return (
    <div className="flex flex-col leading-tight">
      <span className="font-logo-ar text-base font-bold text-primary">متجر غزة</span>
      <span className="text-xs text-muted-foreground">لوحة التحكم</span>
    </div>
  );
}

export function AdminShell({
  admin,
  children,
}: {
  admin: AdminSession;
  children: ReactNode;
}) {
  return (
    <div className="flex min-h-svh flex-1 flex-col lg:flex-row">
      {/* Mobile top bar */}
      <header className="border-b border-border bg-card lg:hidden">
        <div className="flex items-center justify-between px-4 py-3">
          <Brand />
          <SignOutButton variant="ghost" showLabel={false} />
        </div>
        <SidebarNav variant="mobile" />
      </header>

      {/* Desktop sidebar */}
      <aside className="hidden border-e border-border bg-card lg:flex lg:h-svh lg:w-64 lg:shrink-0 lg:flex-col lg:sticky lg:top-0">
        <div className="px-4 py-5">
          <Brand />
        </div>
        <div className="flex-1 px-2">
          <SidebarNav />
        </div>
        <div className="border-t border-border p-3">
          <p className="mb-2 truncate text-xs text-muted-foreground" dir="ltr">
            {admin.email}
          </p>
          <SignOutButton variant="ghost" className="w-full justify-start" />
        </div>
      </aside>

      <main className="flex-1 px-4 py-6 lg:px-8">
        <div className="mx-auto w-full max-w-6xl">{children}</div>
      </main>
    </div>
  );
}
