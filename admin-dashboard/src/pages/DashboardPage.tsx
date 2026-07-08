import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { useOnlineDrivers, useStats } from "../hooks/queries";
import { Card, Spinner, StatTile } from "../components/ui/primitives";
import { LiveMap } from "../components/LiveMap";

function StatusChart({ data }: { data: Record<string, number> }) {
  const chartData = Object.entries(data)
    .filter(([, count]) => count > 0)
    .map(([status, count]) => ({ status: status.replaceAll("_", " ").toLowerCase(), count }));

  if (chartData.length === 0) {
    return <p className="py-10 text-center text-sm text-mist-600">No rides yet</p>;
  }

  return (
    <ResponsiveContainer width="100%" height={220}>
      <BarChart data={chartData} margin={{ top: 8, right: 8, bottom: 8, left: -20 }}>
        <XAxis
          dataKey="status"
          tick={{ fill: "#6B7385", fontSize: 11 }}
          axisLine={false}
          tickLine={false}
          interval={0}
          angle={-20}
          textAnchor="end"
          height={50}
        />
        <YAxis tick={{ fill: "#6B7385", fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
        <Tooltip
          cursor={{ fill: "rgba(255,255,255,0.04)" }}
          contentStyle={{
            background: "#151925",
            border: "1px solid rgba(255,255,255,0.08)",
            borderRadius: 12,
            color: "#F4F6FB",
          }}
        />
        <Bar dataKey="count" fill="#F5A623" radius={[6, 6, 0, 0]} maxBarSize={44} />
      </BarChart>
    </ResponsiveContainer>
  );
}

export function DashboardPage() {
  const { data: stats, isLoading, isError } = useStats();
  const { data: onlineDrivers = [] } = useOnlineDrivers();

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
        <StatTile label="Drivers online" value={stats.driversOnline} accent hint="live" />
        <StatTile label="Active rides" value={stats.activeRides} hint="in progress now" />
        <StatTile label="Total rides" value={stats.totalRides} />
        <StatTile label="Total users" value={stats.totalUsers} hint={`${stats.totalDrivers} drivers`} />
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        <Card className="xl:col-span-2 flex flex-col">
          <div className="mb-2 flex items-center justify-between">
            <h2 className="font-semibold">Live driver map</h2>
            <span className="text-xs text-mist-600">{onlineDrivers.length} online</span>
          </div>
          <div className="h-[360px]">
            <LiveMap drivers={onlineDrivers} />
          </div>
        </Card>

        <Card className="flex flex-col">
          <h2 className="mb-3 font-semibold">Rides by status</h2>
          <StatusChart data={stats.ridesByStatus} />
        </Card>
      </div>
    </div>
  );
}
