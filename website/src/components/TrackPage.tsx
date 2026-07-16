import { useEffect, useMemo, useRef, useState } from "react";
import type maplibregl from "maplibre-gl";
import { MapView } from "./MapView";
import { RideSocket } from "../lib/rideSocket";
import { useI18n } from "../i18n";
import type { GeoPoint } from "../types";

const ENDED_STATUSES = ["COMPLETED", "CANCELLED", "NO_DRIVERS_FOUND"];

function pathRideId(): string | null {
  const m = location.pathname.match(/^\/track\/([^/]+)/);
  return m ? m[1] : null;
}

function pointFromParams(latKey: string, lngKey: string): GeoPoint | null {
  const params = new URLSearchParams(location.search);
  const lat = params.get(latKey);
  const lng = params.get(lngKey);
  if (lat == null || lng == null) return null;
  return { label: "", lat: Number(lat), lng: Number(lng) };
}

export function TrackPage() {
  const { t } = useI18n();
  const mapRef = useRef<maplibregl.Map | null>(null);
  const [driver, setDriver] = useState<GeoPoint | null>(null);
  const [status, setStatus] = useState("IN_PROGRESS");
  const rideId = useMemo(pathRideId, []);
  const from = useMemo(() => pointFromParams("pLat", "pLng"), []);
  const to = useMemo(() => pointFromParams("dLat", "dLng"), []);

  useEffect(() => {
    if (!rideId) return;
    const sock = new RideSocket(rideId, {
      onLocation: (m) => setDriver({ label: "driver", lat: m.lat, lng: m.lng }),
      onStatus: (m) => setStatus(m.status),
    });
    sock.connect();
    return () => sock.disconnect();
  }, [rideId]);

  if (!rideId) return null;

  const ended = ENDED_STATUSES.includes(status);

  return (
    <>
      <MapView mapRef={mapRef} from={from} to={to} stops={[]} route={null} me={null} driver={driver} />
      <div className="track-banner">
        <div className="track-title">{t("track.title")}</div>
        <div className="track-sub">{t("track.sub")}</div>
        <div className={`track-status${ended ? " ended" : ""}`}>{ended ? t("track.ended") : t("track.live")}</div>
      </div>
    </>
  );
}
