import { useEffect, useRef } from "react";
import { MapContainer, TileLayer, Marker, Popup, Tooltip, useMap } from "react-leaflet";
import { Link } from "react-router-dom";
import L from "leaflet";
import type { AdminDriver, OnlineDriver } from "../types";
import { bearing, distanceMeters, unwrapBearing } from "../../lib/geo";

const DEFAULT_CENTER: [number, number] = [37.9601, 58.3261];

// Matches StatusBadge's tone map: ONLINE=success, BUSY=info, OFFLINE=neutral — the live
// map and every status pill elsewhere in the admin now agree on what each color means.
const STATUS_COLOR: Record<string, string> = {
  ONLINE: "#3DCB78",
  BUSY: "#5B8CF0",
  OFFLINE: "#5C6672",
};

/** Below this, a "moved" reading is just GPS jitter — don't spin the car icon in place. */
const MIN_HEADING_DISTANCE_M = 3;

function FocusDriver({ driver }: { driver?: OnlineDriver }) {
  const map = useMap();
  useEffect(() => {
    if (driver) map.flyTo([driver.lat, driver.lng], Math.max(map.getZoom(), 15), { duration: 0.8 });
  }, [driver, map]);
  return null;
}

/** Nose points up (bearing 0); rotated per-driver by the wrapping `driver-marker-icon` div. */
function driverIcon(color: string, rotationDeg: number): L.DivIcon {
  return L.divIcon({
    className: "driver-marker",
    html:
      `<div class="driver-marker-icon" style="transform:rotate(${rotationDeg}deg)">` +
      `<svg width="20" height="20" viewBox="0 0 24 24"><rect x="7" y="9" width="10" height="13" rx="3" fill="${color}"/><path d="M12 2 L17 10 H7 Z" fill="${color}"/></svg>` +
      `</div>`,
    iconSize: [20, 20],
    iconAnchor: [10, 10],
  });
}

export function LiveMap({
  drivers,
  driverDetails = [],
  selectedDriverId,
  onSelect,
}: {
  drivers: OnlineDriver[];
  driverDetails?: AdminDriver[];
  selectedDriverId?: string | null;
  onSelect?: (driverId: string) => void;
}) {
  const center: [number, number] = drivers.length
    ? [drivers[0].lat, drivers[0].lng]
    : DEFAULT_CENTER;
  const detailsById = new Map(driverDetails.map((d) => [d.userId, d]));

  // Per-driver heading state, kept across renders — GPS pings don't carry a compass bearing, so
  // it's derived from consecutive fixes, unwrapped so the icon always turns the short way.
  const prevPositions = useRef<Map<string, { lat: number; lng: number }>>(new Map());
  const headings = useRef<Map<string, number>>(new Map());

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
        <FocusDriver driver={drivers.find((d) => d.driverId === selectedDriverId)} />
        {drivers.map((driver) => {
          const detail = detailsById.get(driver.driverId);
          const color = STATUS_COLOR[detail?.status ?? "ONLINE"];
          const pos = { lat: driver.lat, lng: driver.lng };
          const prev = prevPositions.current.get(driver.driverId);
          let heading = headings.current.get(driver.driverId) ?? 0;
          if (prev && distanceMeters(prev, pos) > MIN_HEADING_DISTANCE_M) {
            heading = unwrapBearing(heading, bearing(prev, pos));
            headings.current.set(driver.driverId, heading);
          }
          prevPositions.current.set(driver.driverId, pos);
          return (
            <Marker
              key={driver.driverId}
              position={[driver.lat, driver.lng]}
              icon={driverIcon(color, heading)}
              eventHandlers={{ click: () => onSelect?.(driver.driverId) }}
              zIndexOffset={selectedDriverId === driver.driverId ? 1000 : 0}
            >
              <Tooltip>{detail?.fullName ?? `Driver ${driver.driverId.slice(0, 8)}`}</Tooltip>
              <Popup>
                <div className="flex flex-col gap-1 text-xs">
                  <span className="font-semibold">{detail?.fullName ?? `Driver ${driver.driverId.slice(0, 8)}`}</span>
                  {detail && (
                    <>
                      <span>
                        {[detail.vehicleMake, detail.vehicleModel].filter(Boolean).join(" ") || "No vehicle on file"}
                        {detail.plateNumber ? ` · ${detail.plateNumber}` : ""}
                      </span>
                      <span>★ {detail.rating}</span>
                      <span>Status: {detail.status.toLowerCase()}</span>
                      <Link to={`/drivers/${detail.userId}`} className="font-semibold text-accent">Open driver profile →</Link>
                    </>
                  )}
                </div>
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>
    </div>
  );
}
