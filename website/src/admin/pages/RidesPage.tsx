import { useState } from "react";
import { Link } from "react-router-dom";
import { useConfirmPayment, useRides } from "../hooks/queries";
import { useAuth } from "../lib/auth";
import { Card, EmptyState, Spinner, StatusBadge } from "../components/ui/primitives";
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from "@/admin/components/ui/table";
import { Button } from "@/admin/components/ui/button";
import { ExportCsvButton } from "@/admin/components/ui/export-csv-button";
import type { CsvColumn } from "../lib/csv";
import type { Ride, RideStatus } from "../types";

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
  const confirmPayment = useConfirmPayment();
  const { hasPermission } = useAuth();
  const canManageFinance = hasPermission("admin.finance.manage");

  const csvColumns: CsvColumn<Ride>[] = [
    { label: "Ride ID", value: (r) => r.id },
    { label: "Status", value: (r) => r.status },
    { label: "Tariff", value: (r) => r.tariff },
    { label: "Fare (TMT)", value: (r) => r.fare ?? "" },
    { label: "Payment method", value: (r) => r.paymentMethod },
    { label: "Payment status", value: (r) => r.paymentStatus },
    { label: "Pickup", value: (r) => r.pickupLabel ?? `${r.pickup.lat}, ${r.pickup.lng}` },
    { label: "Dropoff", value: (r) => r.dropoffLabel ?? `${r.dropoff.lat}, ${r.dropoff.lng}` },
    { label: "Requested at", value: (r) => r.requestedAt },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Rides</h1>
          <p className="text-sm text-mist-500">Most recent trips across the platform</p>
        </div>
        <ExportCsvButton rows={rides} columns={csvColumns} filename={`rides-${filter.toLowerCase()}.csv`} />
      </div>

      <div className="flex flex-wrap gap-2">
        {FILTERS.map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`rounded-full px-3.5 py-1.5 text-xs font-medium transition ${
              filter === f
                ? "bg-accent text-ink-900"
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
          <Table className="min-w-[720px]">
            <TableHeader>
              <TableRow>
                <TableHead>Ride</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Tariff</TableHead>
                <TableHead>Fare</TableHead>
                <TableHead>Payment</TableHead>
                <TableHead>Pickup</TableHead>
                <TableHead>Dropoff</TableHead>
                <TableHead>Requested</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rides.map((ride) => (
                <TableRow key={ride.id}>
                  <TableCell className="font-mono text-xs">
                    <Link to={`/rides/${ride.id}`} className="text-accent hover:underline">
                      {ride.id.slice(0, 8)}
                    </Link>
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={ride.status} />
                  </TableCell>
                  <TableCell>{ride.tariff.toLowerCase()}</TableCell>
                  <TableCell className="tnum">
                    {ride.fare != null ? `${ride.fare} TMT` : "—"}
                    {ride.discountApplied ? (
                      <span className="ml-1 text-xs text-success">(−{ride.discountApplied})</span>
                    ) : null}
                  </TableCell>
                  <TableCell>
                    {ride.paymentMethod === "CARD" ? (
                      ride.paymentStatus === "PENDING" && canManageFinance ? (
                        <Button
                          size="sm"
                          variant="primary"
                          onClick={() => confirmPayment.mutate(ride.id)}
                          disabled={confirmPayment.isPending}
                        >
                          Confirm card payment
                        </Button>
                      ) : (
                        <span className="text-xs">card · {ride.paymentStatus.toLowerCase()}</span>
                      )
                    ) : (
                      <span className="text-xs text-mist-500">cash</span>
                    )}
                  </TableCell>
                  <TableCell className="tnum">{coord(ride.pickup.lat, ride.pickup.lng)}</TableCell>
                  <TableCell className="tnum">{coord(ride.dropoff.lat, ride.dropoff.lng)}</TableCell>
                  <TableCell className="text-mist-500">{timeAgo(ride.requestedAt)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>
    </div>
  );
}
