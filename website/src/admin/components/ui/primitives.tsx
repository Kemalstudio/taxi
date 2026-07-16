import type { ReactNode } from "react";
import type { RideStatus, DriverStatus } from "../../types";

export function Card({
  children,
  className = "",
}: {
  children: ReactNode;
  className?: string;
}) {
  return <div className={`glass p-5 ${className}`}>{children}</div>;
}

export function Spinner({ label }: { label?: string }) {
  return (
    <div className="flex items-center gap-3 text-mist-500">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-mist-600 border-t-amber" />
      {label && <span className="text-sm">{label}</span>}
    </div>
  );
}

const STATUS_STYLES: Record<string, string> = {
  // Ride statuses
  REQUESTED: "bg-white/5 text-mist-300",
  SEARCHING: "bg-info/15 text-info animate-pulse-soft",
  ACCEPTED: "bg-amber-muted text-amber",
  DRIVER_ARRIVED: "bg-amber-muted text-amber",
  IN_PROGRESS: "bg-info/15 text-info",
  COMPLETED: "bg-success/15 text-success",
  CANCELLED: "bg-danger/15 text-danger",
  NO_DRIVERS_FOUND: "bg-danger/15 text-danger",
  // Driver statuses
  ONLINE: "bg-success/15 text-success",
  BUSY: "bg-amber-muted text-amber",
  OFFLINE: "bg-white/5 text-mist-500",
};

export function StatusBadge({ status }: { status: RideStatus | DriverStatus | string }) {
  const style = STATUS_STYLES[status] ?? "bg-white/5 text-mist-300";
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ${style}`}>
      {status.replaceAll("_", " ").toLowerCase()}
    </span>
  );
}

export function StatTile({
  label,
  value,
  hint,
  accent = false,
}: {
  label: string;
  value: string | number;
  hint?: string;
  accent?: boolean;
}) {
  return (
    <div className={`glass glass-hover animate-fade-up p-5 ${accent ? "ring-1 ring-amber/20" : ""}`}>
      <p className="text-sm text-mist-500">{label}</p>
      <p className={`mt-2 text-3xl font-semibold tnum ${accent ? "text-amber" : "text-mist-100"}`}>
        {value}
      </p>
      {hint && <p className="mt-1 text-xs text-mist-600">{hint}</p>}
    </div>
  );
}

export function EmptyState({ message }: { message: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-2 py-16 text-center">
      <div className="grid h-12 w-12 place-items-center rounded-full bg-white/5 text-mist-500">◎</div>
      <p className="text-sm text-mist-500">{message}</p>
    </div>
  );
}
