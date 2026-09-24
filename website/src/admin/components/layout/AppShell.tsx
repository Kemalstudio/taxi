import { useEffect, useMemo, useState, type ReactNode } from "react";
import { NavLink } from "react-router-dom";
import { PanelLeftClose, PanelLeftOpen, Menu, X, LogOut, Bell, LifeBuoy } from "lucide-react";
import { useAuth } from "../../lib/auth";
import { useSos } from "../../hooks/queries";
import { cn } from "@/lib/cn";
import { NAV_GROUPS } from "./navItems";
import { CommandPalette } from "../CommandPalette";

const ROLE_LABELS: Record<string, string> = {
  ADMIN: "Administrator",
  SUPER_ADMIN: "Super administrator",
  OPERATOR: "Operator",
  DISPATCHER: "Dispatcher",
  MODERATOR: "Moderator",
  ACCOUNTANT: "Accountant",
};

const COLLAPSE_KEY = "taxi_admin_sidebar_collapsed";

function NavLinks({ onNavigate }: { onNavigate?: () => void }) {
  const { hasPermission } = useAuth();
  return (
    <nav className="flex flex-col gap-5">
      {NAV_GROUPS.map(({ group, items }) => {
        const visible = items.filter((item) => hasPermission(item.permission));
        if (visible.length === 0) return null;
        return (
          <div key={group} className="flex flex-col gap-1">
            <p className="px-3 text-[11px] font-semibold uppercase tracking-wide text-mist-600 group-data-[collapsed=true]:hidden">
              {group}
            </p>
            {visible.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                onClick={onNavigate}
                className={({ isActive }) =>
                  cn(
                    "flex h-10 items-center gap-2.5 rounded-lg px-3 text-sm font-medium transition-colors",
                    isActive
                      ? "bg-accent text-ink-900 shadow-glow"
                      : "text-mist-500 hover:bg-white/5 hover:text-mist-100",
                  )
                }
              >
                <item.icon size={18} className="shrink-0" />
                <span className="group-data-[collapsed=true]:hidden">{item.label}</span>
              </NavLink>
            ))}
          </div>
        );
      })}
    </nav>
  );
}

function SupportCard() {
  return (
    <a
      href="mailto:support@taksigo.local"
      className="flex flex-col gap-2 rounded-xl2 bg-white/5 p-3.5 text-left transition hover:bg-white/[0.08] group-data-[collapsed=true]:hidden"
    >
      <div className="grid h-8 w-8 place-items-center rounded-lg bg-accent-muted text-accent">
        <LifeBuoy size={16} />
      </div>
      <div>
        <p className="text-xs font-semibold text-mist-100">Need help?</p>
        <p className="text-[11px] text-mist-600">Contact support</p>
      </div>
    </a>
  );
}

function Sidebar() {
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem(COLLAPSE_KEY) === "1");

  useEffect(() => {
    localStorage.setItem(COLLAPSE_KEY, collapsed ? "1" : "0");
  }, [collapsed]);

  return (
    <aside
      data-collapsed={collapsed}
      className={cn(
        "group hidden shrink-0 flex-col gap-6 border-r border-white/8 bg-ink-800 p-4 transition-[width] duration-200 lg:flex",
        collapsed ? "w-[68px]" : "w-[248px]",
      )}
    >
      <div className="flex h-8 items-center gap-3 px-1">
        <div className="grid h-8 w-8 shrink-0 place-items-center rounded-lg bg-accent text-sm font-bold text-ink-900">
          T
        </div>
        <div className="min-w-0 group-data-[collapsed=true]:hidden">
          <p className="truncate text-sm font-semibold leading-tight text-mist-100">Taxi Platform</p>
          <p className="truncate text-xs text-mist-600">Operations Console</p>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto">
        <NavLinks />
      </div>

      <div className="mt-auto flex flex-col gap-3">
        <SupportCard />
        <button
          onClick={() => setCollapsed((c) => !c)}
          className="flex h-9 items-center gap-2.5 rounded-lg px-3 text-sm text-mist-500 transition-colors hover:bg-white/5 hover:text-mist-100"
          title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
        >
          {collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
          <span className="group-data-[collapsed=true]:hidden">Collapse</span>
        </button>
      </div>
    </aside>
  );
}

/** Mobile: sidebar becomes a slide-in drawer instead of just disappearing. */
function MobileDrawer() {
  const [open, setOpen] = useState(false);
  return (
    <>
      <button
        onClick={() => setOpen(true)}
        className="grid h-9 w-9 shrink-0 place-items-center rounded-lg text-mist-300 hover:bg-white/5 lg:hidden"
        aria-label="Open menu"
      >
        <Menu size={20} />
      </button>
      {open && (
        <div className="fixed inset-0 z-50 lg:hidden" onClick={(e) => e.target === e.currentTarget && setOpen(false)}>
          <div className="absolute inset-0 bg-black/60" />
          <div className="absolute inset-y-0 left-0 flex w-[260px] flex-col gap-6 border-r border-white/8 bg-ink-800 p-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="grid h-8 w-8 place-items-center rounded-lg bg-accent text-sm font-bold text-ink-900">
                  T
                </div>
                <p className="text-sm font-semibold text-mist-100">Taxi Platform</p>
              </div>
              <button onClick={() => setOpen(false)} className="rounded-lg p-1.5 text-mist-500 hover:bg-white/5">
                <X size={18} />
              </button>
            </div>
            <NavLinks onNavigate={() => setOpen(false)} />
          </div>
        </div>
      )}
    </>
  );
}

function TodayBadge() {
  const label = useMemo(
    () => new Date().toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" }),
    [],
  );
  return (
    <span className="hidden items-center rounded-full border border-white/8 bg-white/5 px-3 py-1.5 text-xs text-mist-400 md:inline-flex">
      {label}
    </span>
  );
}

function NotificationBell() {
  const { hasPermission } = useAuth();
  const canViewAlerts = hasPermission("admin.rides.view");
  const { data: alerts = [] } = useSos(20, canViewAlerts);
  if (!canViewAlerts) return null;
  return (
    <NavLink to="/" className="relative grid h-9 w-9 place-items-center rounded-lg text-mist-400 hover:bg-white/5 hover:text-mist-100">
      <Bell size={18} />
      {alerts.length > 0 && (
        <span className="absolute right-1.5 top-1.5 grid h-4 min-w-[16px] place-items-center rounded-full bg-danger px-1 text-[10px] font-semibold text-white">
          {alerts.length > 9 ? "9+" : alerts.length}
        </span>
      )}
    </NavLink>
  );
}

function AvatarMenu() {
  const { logout, role } = useAuth();
  const label = ROLE_LABELS[role ?? ""] ?? "Team member";
  const initials = label
    .split(" ")
    .map((w) => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <div className="flex items-center gap-2.5 pl-1">
      <div className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-accent-muted text-xs font-bold text-accent">
        {initials}
      </div>
      <div className="hidden leading-tight sm:block">
        <p className="text-sm font-medium text-mist-100">{label}</p>
        <p className="text-[11px] text-mist-600">Signed in</p>
      </div>
      <button
        onClick={() => {
          logout();
          window.location.href = "/";
        }}
        className="grid h-9 w-9 place-items-center rounded-lg text-mist-500 transition-colors hover:bg-white/5 hover:text-danger"
        title="Sign out"
      >
        <LogOut size={16} />
      </button>
    </div>
  );
}

function Topbar() {
  return (
    <header className="flex items-center gap-3 border-b border-white/8 bg-ink-800 px-4 py-3.5 sm:px-6">
      <MobileDrawer />
      <CommandPalette />
      <div className="ml-auto flex items-center gap-2 sm:gap-3">
        <TodayBadge />
        <NotificationBell />
        <div className="hidden h-6 w-px bg-white/8 sm:block" />
        <AvatarMenu />
      </div>
    </header>
  );
}

function Footer() {
  return (
    <footer className="flex shrink-0 items-center justify-center border-t border-white/8 bg-ink-800 px-4 py-3 text-center text-xs text-mist-600">
      &copy; {new Date().getFullYear()} Taxi Platform. All rights reserved.
    </footer>
  );
}

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex h-full">
      <Sidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar />
        <main className="flex-1 overflow-y-auto p-4 sm:p-6">{children}</main>
        <Footer />
      </div>
    </div>
  );
}
