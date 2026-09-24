import { useMemo } from "react";
import { Link } from "react-router-dom";
import {
  Area,
  AreaChart,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
} from "recharts";
import {
  Bell,
  Car,
  Database,
  MessageSquare,
  Navigation,
  Route as RouteIcon,
  TrendingDown,
  TrendingUp,
  Users as UsersIcon,
} from "lucide-react";
import {
  useDrivers,
  useLiveDriverUpdates,
  useOnlineDrivers,
  useRevenue,
  useRides,
  useSos,
  useStats,
  useSystemStatus,
} from "../hooks/queries";
import { Card, CardHeader, CardTitle, EmptyState, KpiCard, Spinner } from "../components/ui/primitives";
import { LiveMap } from "../components/LiveMap";
import type { SystemStatus } from "../types";
import { useAuth } from "../lib/auth";

function timeAgo(iso: string) {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

const STATUS_COLORS: Record<string, string> = {
  REQUESTED: "#5C6672",
  SEARCHING: "#5B8CF0",
  ACCEPTED: "#F0B767",
  DRIVER_ARRIVED: "#E8A23D",
  IN_PROGRESS: "#9370F5",
  COMPLETED: "#3DCB78",
  CANCELLED: "#F0555F",
  NO_DRIVERS_FOUND: "#7C8894",
};

function SosAlerts({ enabled }: { enabled: boolean }) {
  const { data: alerts = [] } = useSos(20, enabled);
  if (!enabled) return null;
  return (
    <Card>
      <CardHeader>
        <CardTitle>🚨 SOS alerts</CardTitle>
        <span className="text-xs text-mist-600">{alerts.length} recent</span>
      </CardHeader>
      {alerts.length === 0 ? (
        <EmptyState message="No SOS signals — all clear." />
      ) : (
        <div className="flex flex-col gap-2">
          {alerts.slice(0, 6).map((a) => (
            <div
              key={a.id}
              className="flex items-center justify-between rounded-lg border-l-2 border-danger bg-danger/10 px-3 py-2 text-sm"
            >
              <span className="font-mono text-xs text-mist-400">{a.rideId.slice(0, 8)}</span>
              <span className="tnum text-mist-300">
                {a.lat.toFixed(4)}, {a.lng.toFixed(4)}
              </span>
              <span className="text-xs text-mist-500">{timeAgo(a.createdAt)}</span>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}

function RidesByStatusCard({ data }: { data: Record<string, number> }) {
  const chartData = Object.entries(data)
    .filter(([, count]) => count > 0)
    .map(([status, count]) => ({ status, count, fill: STATUS_COLORS[status] ?? "#7C8894" }));
  const total = chartData.reduce((sum, d) => sum + d.count, 0);

  return (
    <Card className="flex flex-col">
      <CardHeader>
        <CardTitle>Rides by status</CardTitle>
      </CardHeader>
      {chartData.length === 0 ? (
        <p className="py-10 text-center text-sm text-mist-600">No rides yet</p>
      ) : (
        <div className="flex flex-col gap-5">
          <div className="relative">
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie
                  data={chartData}
                  dataKey="count"
                  nameKey="status"
                  innerRadius="68%"
                  outerRadius="100%"
                  paddingAngle={2}
                  stroke="none"
                >
                  {chartData.map((d) => (
                    <Cell key={d.status} fill={d.fill} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    background: "#161C25",
                    border: "1px solid rgba(255,255,255,0.08)",
                    borderRadius: 12,
                    color: "#EDF1F4",
                  }}
                  formatter={(value: number, _name, entry) => [
                    value,
                    String(entry.payload.status).replaceAll("_", " ").toLowerCase(),
                  ]}
                />
              </PieChart>
            </ResponsiveContainer>
            <div className="pointer-events-none absolute inset-0 grid place-items-center">
              <p className="text-2xl font-semibold tnum text-mist-100">{total}</p>
              <p className="text-xs text-mist-600">Total</p>
            </div>
          </div>
          <div className="flex flex-col gap-2">
            {chartData.map((d) => (
              <div key={d.status} className="flex items-center justify-between text-sm">
                <span className="flex items-center gap-2 text-mist-400">
                  <span className="h-2 w-2 rounded-full" style={{ background: d.fill }} />
                  {d.status.replaceAll("_", " ").toLowerCase()}
                </span>
                <span className="tnum text-mist-200">{d.count}</span>
              </div>
            ))}
          </div>
          <Link to="/rides" className="text-xs font-medium text-accent hover:underline">
            View full report &rarr;
          </Link>
        </div>
      )}
    </Card>
  );
}

function RevenueOverviewCard({ enabled }: { enabled: boolean }) {
  const { data: revenue, isLoading } = useRevenue(14, enabled);

  const { chartPoints, currentTotal, trendPct } = useMemo(() => {
    const points = [...(revenue?.points ?? [])].sort((a, b) => a.date.localeCompare(b.date));
    const last7 = points.slice(-7);
    const prev7 = points.slice(-14, -7);
    const currentTotal = last7.reduce((sum, p) => sum + p.totalFare, 0);
    const prevTotal = prev7.reduce((sum, p) => sum + p.totalFare, 0);
    const trendPct = prevTotal > 0 ? Math.round(((currentTotal - prevTotal) / prevTotal) * 100) : null;
    const chartPoints = last7.map((p) => ({
      day: new Date(p.date).toLocaleDateString(undefined, { weekday: "short" }),
      totalFare: p.totalFare,
    }));
    return { chartPoints, currentTotal, trendPct };
  }, [revenue]);

  if (!enabled) return null;
  return (
    <Card className="flex flex-col">
      <CardHeader>
        <div>
          <CardTitle>Revenue overview</CardTitle>
          <p className="mt-1 text-2xl font-semibold tnum text-mist-100">{currentTotal} TMT</p>
        </div>
        {trendPct != null && (
          <span
            className={`flex items-center gap-1 text-xs font-medium ${trendPct >= 0 ? "text-success" : "text-danger"}`}
          >
            {trendPct >= 0 ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
            {Math.abs(trendPct)}% vs prior week
          </span>
        )}
      </CardHeader>
      {isLoading ? (
        <Spinner label="Loading revenue…" />
      ) : chartPoints.length === 0 ? (
        <p className="py-10 text-center text-sm text-mist-600">No completed rides in this window yet</p>
      ) : (
        <ResponsiveContainer width="100%" height={220}>
          <AreaChart data={chartPoints} margin={{ top: 8, right: 8, bottom: 0, left: -20 }}>
            <defs>
              <linearGradient id="revenueFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#35D0BE" stopOpacity={0.35} />
                <stop offset="100%" stopColor="#35D0BE" stopOpacity={0} />
              </linearGradient>
            </defs>
            <XAxis dataKey="day" tick={{ fill: "#5C6672", fontSize: 11 }} axisLine={false} tickLine={false} />
            <Tooltip
              contentStyle={{
                background: "#161C25",
                border: "1px solid rgba(255,255,255,0.08)",
                borderRadius: 12,
                color: "#EDF1F4",
              }}
              formatter={(value: number) => [`${value} TMT`, "Revenue"]}
            />
            <Area type="monotone" dataKey="totalFare" stroke="#35D0BE" strokeWidth={2} fill="url(#revenueFill)" />
          </AreaChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}

function RecentRidesCard({ enabled }: { enabled: boolean }) {
  const { data: rides = [], isLoading } = useRides("ALL", 6, undefined, enabled);
  if (!enabled) return null;
  return (
    <Card>
      <CardHeader>
        <CardTitle>Recent rides</CardTitle>
        <Link to="/rides" className="text-xs font-medium text-accent hover:underline">
          View all
        </Link>
      </CardHeader>
      {isLoading ? (
        <Spinner label="Loading rides…" />
      ) : rides.length === 0 ? (
        <EmptyState message="No rides yet." />
      ) : (
        <div className="flex flex-col divide-y divide-white/[0.06]">
          {rides.map((ride) => (
            <div key={ride.id} className="flex items-center gap-3 py-2.5 first:pt-0 last:pb-0">
              <span className="h-2 w-2 shrink-0 rounded-full" style={{ background: STATUS_COLORS[ride.status] }} />
              <span className="w-16 shrink-0 font-mono text-xs text-mist-500">{ride.id.slice(0, 8)}</span>
              <span className="flex-1 truncate text-sm text-mist-300">{ride.tariff.toLowerCase()} ride</span>
              <span className="tnum text-sm text-mist-100">{ride.fare != null ? `${ride.fare} TMT` : "—"}</span>
              <span className="w-16 shrink-0 text-right text-xs text-mist-600">{timeAgo(ride.requestedAt)}</span>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}

const STATUS_PANEL_ROWS: {
  key: keyof SystemStatus;
  label: string;
  icon: typeof Database;
  degradedLabel: string;
}[] = [
  { key: "databaseReachable", label: "Database", icon: Database, degradedLabel: "Unreachable" },
  { key: "dispatchReachable", label: "Dispatch / geo-index", icon: Navigation, degradedLabel: "Unreachable" },
  { key: "smsGatewayConfigured", label: "SMS gateway", icon: MessageSquare, degradedLabel: "Not configured" },
  { key: "pushNotificationsConfigured", label: "Push notifications", icon: Bell, degradedLabel: "Not configured" },
];

function StatusPill({ healthy, degradedLabel }: { healthy?: boolean; degradedLabel: string }) {
  if (healthy === undefined) {
    return <span className="rounded-full bg-white/5 px-2.5 py-1 text-xs text-mist-600">checking…</span>;
  }
  return healthy ? (
    <span className="rounded-full bg-success/15 px-2.5 py-1 text-xs font-medium text-success">Healthy</span>
  ) : (
    <span className="rounded-full bg-white/5 px-2.5 py-1 text-xs font-medium text-mist-500">{degradedLabel}</span>
  );
}

function SystemStatusCard() {
  const { data: status } = useSystemStatus();
  return (
    <Card>
      <CardHeader>
        <CardTitle>System status</CardTitle>
      </CardHeader>
      <div className="flex flex-col gap-3">
        {STATUS_PANEL_ROWS.map((row) => {
          const healthy = status ? status[row.key] : undefined;
          return (
            <div key={row.key} className="flex items-center gap-3">
              <div
                className={`grid h-9 w-9 shrink-0 place-items-center rounded-lg ${
                  healthy ? "bg-success/15 text-success" : "bg-white/5 text-mist-500"
                }`}
              >
                <row.icon size={16} />
              </div>
              <span className="flex-1 text-sm text-mist-300">{row.label}</span>
              <StatusPill healthy={healthy} degradedLabel={row.degradedLabel} />
            </div>
          );
        })}
      </div>
    </Card>
  );
}

export function DashboardPage() {
  const { hasPermission } = useAuth();
  const canViewDrivers = hasPermission("admin.drivers.view");
  const canViewRides = hasPermission("admin.rides.view");
  const canViewFinance = hasPermission("admin.finance.view");
  const { data: stats, isLoading, isError } = useStats();
  const { data: onlineDrivers = [] } = useOnlineDrivers(canViewDrivers);
  const { data: drivers = [] } = useDrivers(canViewDrivers);
  useLiveDriverUpdates(canViewDrivers);

  if (isLoading) {
    return (
      <div className="grid h-full place-items-center">
        <Spinner label="Loading platform metrics…" />
      </div>
    );
  }

  if (isError || !stats) {
    return (
      <Card className="border-danger/30">
        <p className="text-danger">Failed to load stats. Is the backend running on the configured URL?</p>
      </Card>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Dashboard</h1>
        <p className="text-sm text-mist-500">Real-time overview of the platform</p>
      </div>

      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <KpiCard label="Drivers online" value={stats.driversOnline} hint="live" icon={<Car size={18} />} tone="accent" />
        <KpiCard
          label="Active rides"
          value={stats.activeRides}
          hint="in progress now"
          icon={<RouteIcon size={18} />}
          tone="success"
        />
        <KpiCard label="Total rides" value={stats.totalRides} icon={<RouteIcon size={18} />} tone="info" />
        <KpiCard
          label="Total users"
          value={stats.totalUsers}
          hint={`${stats.totalDrivers} drivers`}
          icon={<UsersIcon size={18} />}
          tone="violet"
        />
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {canViewDrivers && <Card className="xl:col-span-2 flex flex-col">
          <CardHeader>
            <CardTitle>Live driver map</CardTitle>
            <span className="flex items-center gap-2 text-xs text-mist-600">
              <span className="h-2 w-2 rounded-full bg-success animate-pulse-soft" />
              {onlineDrivers.length} online
            </span>
          </CardHeader>
          <div className="h-[360px]">
            <LiveMap drivers={onlineDrivers} driverDetails={drivers} />
          </div>
        </Card>}

        <RidesByStatusCard data={stats.ridesByStatus} />
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        <RevenueOverviewCard enabled={canViewFinance} />
        <RecentRidesCard enabled={canViewRides} />
        <SystemStatusCard />
      </div>

      <SosAlerts enabled={canViewRides} />
    </div>
  );
}
