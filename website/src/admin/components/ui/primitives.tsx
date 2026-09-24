import type { ReactNode } from "react";
import { TrendingDown, TrendingUp } from "lucide-react";
import type { RideStatus, DriverStatus } from "../../types";
import { Card as CardBase } from "./card";
import { Badge } from "./badge";
import { cn } from "@/lib/cn";

export { Card } from "./card";
export { CardHeader, CardTitle, CardDescription } from "./card";
export { Badge } from "./badge";

export function Spinner({ label }: { label?: string }) {
  return (
    <div className="flex items-center gap-3 text-mist-500">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-mist-600 border-t-accent" />
      {label && <span className="text-sm">{label}</span>}
    </div>
  );
}

const STATUS_TONE: Record<string, "neutral" | "success" | "danger" | "info" | "amber"> = {
  // Ride statuses
  REQUESTED: "neutral",
  SEARCHING: "info",
  ACCEPTED: "amber",
  DRIVER_ARRIVED: "amber",
  IN_PROGRESS: "info",
  COMPLETED: "success",
  CANCELLED: "danger",
  NO_DRIVERS_FOUND: "danger",
  // Driver statuses
  ONLINE: "success",
  BUSY: "amber",
  OFFLINE: "neutral",
};

export function StatusBadge({ status }: { status: RideStatus | DriverStatus | string }) {
  return (
    <Badge tone={STATUS_TONE[status] ?? "neutral"} className={status === "SEARCHING" ? "animate-pulse-soft" : ""}>
      {status.replaceAll("_", " ").toLowerCase()}
    </Badge>
  );
}

interface StatTileProps {
  label: string;
  value: string | number;
  hint?: string;
  accent?: boolean;
  trend?: { direction: "up" | "down"; label: string };
}

export function StatTile({ label, value, hint, accent = false, trend }: StatTileProps) {
  return (
    <CardBase className={cn("animate-fade-up p-5 transition hover:border-white/[0.14]", accent && "ring-1 ring-accent/20")}>
      <p className="text-sm text-mist-500">{label}</p>
      <p className={cn("mt-2 text-[30px] font-semibold leading-none tnum", accent ? "text-accent" : "text-mist-100")}>
        {value}
      </p>
      {trend && (
        <p className={cn("mt-2 flex items-center gap-1 text-[13px]", trend.direction === "up" ? "text-success" : "text-danger")}>
          {trend.direction === "up" ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
          {trend.label}
        </p>
      )}
      {hint && !trend && <p className="mt-1 text-xs text-mist-600">{hint}</p>}
    </CardBase>
  );
}

interface KpiCardProps {
  label: string;
  value: string | number;
  hint?: string;
  icon: ReactNode;
  tone?: "accent" | "amber" | "success" | "info" | "violet";
}

const KPI_TONE_CLASSES: Record<NonNullable<KpiCardProps["tone"]>, string> = {
  accent: "bg-accent-muted text-accent",
  amber: "bg-amber-muted text-amber",
  success: "bg-success/15 text-success",
  info: "bg-info/15 text-info",
  violet: "bg-violet/15 text-violet",
};

/** Icon-circle stat card, styled after the reference dashboard mockup. */
export function KpiCard({ label, value, hint, icon, tone = "accent" }: KpiCardProps) {
  return (
    <CardBase className="animate-fade-up p-5 transition hover:border-white/[0.14]">
      <div className={cn("grid h-10 w-10 place-items-center rounded-xl2", KPI_TONE_CLASSES[tone])}>{icon}</div>
      <p className="mt-4 text-sm text-mist-500">{label}</p>
      <p className="mt-1 text-[28px] font-semibold leading-none tnum text-mist-100">{value}</p>
      {hint && <p className="mt-2 text-xs text-mist-600">{hint}</p>}
    </CardBase>
  );
}

export function EmptyState({ message, icon }: { message: string; icon?: ReactNode }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-center">
      <div className="grid h-12 w-12 place-items-center rounded-full bg-white/5 text-mist-500">
        {icon ?? "◎"}
      </div>
      <p className="text-sm text-mist-500">{message}</p>
    </div>
  );
}
