import { useDrivers } from "../hooks/queries";
import { Card, EmptyState, Spinner, StatusBadge } from "../components/ui/primitives";

export function DriversPage() {
  const { data: drivers = [], isLoading } = useDrivers();

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Drivers</h1>
        <p className="text-sm text-mist-500">Registered drivers and their current availability</p>
      </div>

      <Card className="p-0">
        {isLoading ? (
          <div className="p-6">
            <Spinner label="Loading drivers…" />
          </div>
        ) : drivers.length === 0 ? (
          <EmptyState message="No drivers registered yet." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="text-xs uppercase tracking-wide text-mist-600">
                <tr className="border-b border-white/5">
                  <th className="px-5 py-3 font-medium">Driver</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                  <th className="px-5 py-3 font-medium">Vehicle</th>
                  <th className="px-5 py-3 font-medium">Plate</th>
                  <th className="px-5 py-3 font-medium">Rating</th>
                </tr>
              </thead>
              <tbody>
                {drivers.map((driver) => (
                  <tr key={driver.userId} className="border-b border-white/5 transition hover:bg-white/[0.03]">
                    <td className="px-5 py-3">
                      <p className="font-medium text-mist-100">{driver.fullName}</p>
                      <p className="text-xs text-mist-600">{driver.email}</p>
                    </td>
                    <td className="px-5 py-3">
                      <StatusBadge status={driver.status} />
                    </td>
                    <td className="px-5 py-3 text-mist-300">
                      {driver.vehicleMake || driver.vehicleModel
                        ? `${driver.vehicleMake ?? ""} ${driver.vehicleModel ?? ""}`.trim()
                        : "—"}
                    </td>
                    <td className="px-5 py-3 font-mono text-xs text-mist-400">{driver.plateNumber ?? "—"}</td>
                    <td className="px-5 py-3 tnum text-amber">★ {driver.rating}</td>
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
