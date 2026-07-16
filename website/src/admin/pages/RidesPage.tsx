import { useState } from "react";
import { useRides } from "../hooks/queries";
import { Card, EmptyState, Spinner, StatusBadge } from "../components/ui/primitives";
import type { RideStatus } from "../types";

const FILTERS: (RideStatus | "ALL")[] = [
  "ALL",
  "SEARCHING",
  "ACCEPTED",
  "IN_PROGRESS",
  "COMPLETED",
  "CANCELLED",
  "NO_DRIVERS_FOUND",
];

function coord(lat: number, lng: number) {
  return `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
}

function timeAgo(iso: string) {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

export function RidesPage() {
  const [filter, setFilter] = useState<RideStatus | "ALL">("ALL");
  const { data: rides = [], isLoading } = useRides(filter);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Rides</h1>
        <p className="text-sm text-mist-500">Most recent trips across the platform</p>
      </div>

      <div className="flex flex-wrap gap-2">
        {FILTERS.map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`rounded-full px-3.5 py-1.5 text-xs font-medium transition ${
              filter === f
                ? "bg-amber text-ink-900"
                : "border border-white/10 text-mist-400 hover:text-mist-100"
            }`}
          >
            {f.replaceAll("_", " ").toLowerCase()}
          </button>
        ))}
      </div>

      <Card className="p-0">
        {isLoading ? (
          <div className="p-6">
            <Spinner label="Loading rides…" />
          </div>
        ) : rides.length === 0 ? (
          <EmptyState message="No rides match this filter yet." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="text-xs uppercase tracking-wide text-mist-600">
                <tr className="border-b border-white/5">
                  <th className="px-5 py-3 font-medium">Ride</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                  <th className="px-5 py-3 font-medium">Tariff</th>
                  <th className="px-5 py-3 font-medium">Fare</th>
                  <th className="px-5 py-3 font-medium">Pickup</th>
                  <th className="px-5 py-3 font-medium">Dropoff</th>
                  <th className="px-5 py-3 font-medium">Requested</th>
                </tr>
              </thead>
              <tbody>
                {rides.map((ride) => (
                  <tr key={ride.id} className="border-b border-white/5 transition hover:bg-white/[0.03]">
                    <td className="px-5 py-3 font-mono text-xs text-mist-400">{ride.id.slice(0, 8)}</td>
                    <td className="px-5 py-3">
                      <StatusBadge status={ride.status} />
                    </td>
                    <td className="px-5 py-3 text-mist-300">{ride.tariff.toLowerCase()}</td>
                    <td className="px-5 py-3 tnum text-mist-300">
                      {ride.fare != null ? `${ride.fare} TMT` : "—"}
                      {ride.discountApplied ? (
                        <span className="ml-1 text-xs text-success">(−{ride.discountApplied})</span>
                      ) : null}
                    </td>
                    <td className="px-5 py-3 tnum text-mist-300">{coord(ride.pickup.lat, ride.pickup.lng)}</td>
                    <td className="px-5 py-3 tnum text-mist-300">{coord(ride.dropoff.lat, ride.dropoff.lng)}</td>
                    <td className="px-5 py-3 text-mist-500">{timeAgo(ride.requestedAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
