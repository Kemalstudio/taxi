import { MapContainer, TileLayer, CircleMarker, Tooltip } from "react-leaflet";
import type { OnlineDriver } from "../types";

const DEFAULT_CENTER: [number, number] = [52.52, 13.405];

export function LiveMap({ drivers }: { drivers: OnlineDriver[] }) {
  const center: [number, number] = drivers.length
    ? [drivers[0].lat, drivers[0].lng]
    : DEFAULT_CENTER;

  return (
    <div className="h-full w-full overflow-hidden rounded-xl2">
      <MapContainer
        center={center}
        zoom={12}
        scrollWheelZoom
        style={{ height: "100%", width: "100%" }}
        attributionControl
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; OpenStreetMap contributors'
        />
        {drivers.map((driver) => (
          <CircleMarker
            key={driver.driverId}
            center={[driver.lat, driver.lng]}
            radius={8}
            pathOptions={{
              color: "#F5A623",
              fillColor: "#F5A623",
              fillOpacity: 0.85,
              weight: 2,
            }}
          >
            <Tooltip>Driver {driver.driverId.slice(0, 8)}</Tooltip>
          </CircleMarker>
        ))}
      </MapContainer>
    </div>
  );
}
