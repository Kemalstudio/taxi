import type { ReactNode } from "react";
import { NavLink } from "react-router-dom";
import { useAuth } from "../../lib/auth";

const NAV = [
  { to: "/", label: "Dashboard", icon: "▦", end: true },
  { to: "/rides", label: "Rides", icon: "◈" },
  { to: "/drivers", label: "Drivers", icon: "⬡" },
];

function Sidebar() {
  return (
    <aside className="hidden w-64 shrink-0 flex-col gap-8 border-r border-white/5 bg-ink-800/40 p-6 lg:flex">
      <div className="flex items-center gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-xl bg-amber text-lg font-bold text-ink-900">
          T
        </div>
        <div>
          <p className="font-semibold leading-tight">Taxi Platform</p>
          <p className="text-xs text-mist-600">Operations Console</p>
        </div>
      </div>

      <nav className="flex flex-col gap-1">
        {NAV.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) => `nav-item ${isActive ? "nav-item-active" : ""}`}
          >
            <span className="text-base">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="mt-auto text-xs text-mist-600">
        <p>v0.1.0 · Phase 3</p>
      </div>
    </aside>
  );
}

function Topbar() {
  const { logout } = useAuth();
  return (
    <header className="flex items-center justify-between border-b border-white/5 bg-ink-800/30 px-6 py-4 backdrop-blur-md">
      <div>
        <p className="text-sm text-mist-500">Welcome back</p>
        <p className="font-semibold">Platform Administrator</p>
      </div>
      <div className="flex items-center gap-3">
        <span className="hidden items-center gap-2 rounded-full bg-success/10 px-3 py-1.5 text-xs text-success sm:inline-flex">
          <span className="h-2 w-2 rounded-full bg-success animate-pulse-soft" />
          Live
        </span>
        <button onClick={logout} className="btn-ghost text-sm">
          Sign out
        </button>
      </div>
    </header>
  );
}

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex h-full">
      <Sidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar />
        <main className="flex-1 overflow-y-auto p-6">{children}</main>
      </div>
    </div>
  );
}
