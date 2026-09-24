import { useEffect, useMemo, useRef, useState } from "react";
import type maplibregl from "maplibre-gl";
import { ScanEye } from "lucide-react";
import { MapView } from "./MapView";
import { ArFinder } from "./ArFinder";
import { RideSocket } from "../lib/rideSocket";
import { routeThrough } from "../lib/routing";
import { distanceMeters } from "../lib/geo";
import { useI18n } from "../i18n";
import type { GeoPoint, RouteResult } from "../types";

const ENDED_STATUSES = ["COMPLETED", "CANCELLED", "NO_DRIVERS_FOUND"];

/** Re-route (hit OSRM again) only when the driver has moved this far from the last fix we
 * routed from, or this much time has passed — whichever comes first. Keeps the ETA fresh
 * without hammering the public OSRM server on every single location ping. */
const REROUTE_MIN_METERS = 150;
const REROUTE_MIN_MS = 15000;

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
  const [route, setRoute] = useState<RouteResult | null>(null);
  const [arOpen, setArOpen] = useState(false);
  const rideId = useMemo(pathRideId, []);
  const from = useMemo(() => pointFromParams("pLat", "pLng"), []);
  const to = useMemo(() => pointFromParams("dLat", "dLng"), []);
  const lastRouted = useRef<{ at: number; point: GeoPoint; target: GeoPoint } | null>(null);

  useEffect(() => {
    if (!rideId) return;
    const sock = new RideSocket(rideId, {
      onLocation: (m) => setDriver({ label: "driver", lat: m.lat, lng: m.lng }),
      onStatus: (m) => setStatus(m.status),
    });
    sock.connect();
    return () => sock.disconnect();
  }, [rideId]);

  const ended = ENDED_STATUSES.includes(status);
  // Before the ride starts the driver is heading to pickup; once it's in progress, to dropoff.
  const target = status === "IN_PROGRESS" ? to : from;

  useEffect(() => {
    if (!driver || !target || ended) return;
    const last = lastRouted.current;
    const due =
      !last ||
      last.target !== target ||
      Date.now() - last.at > REROUTE_MIN_MS ||
      distanceMeters(last.point, driver) > REROUTE_MIN_METERS;
    if (!due) return;
    lastRouted.current = { at: Date.now(), point: driver, target };
    let cancelled = false;
    routeThrough([driver, target]).then((r) => {
      if (!cancelled) setRoute(r);
    });
    return () => {
      cancelled = true;
    };
  }, [driver, target, ended]);

  if (!rideId) return null;

  return (
    <>
      <MapView mapRef={mapRef} from={from} to={to} stops={[]} route={route} me={null} driver={driver} autoFit={false} />
      <div className="track-banner">
        <div className="track-title">{t("track.title")}</div>
        <div className="track-sub">{t("track.sub")}</div>
        <div className={`track-status${ended ? " ended" : ""}`}>{ended ? t("track.ended") : t("track.live")}</div>
        {!ended && route && (
          <div className="track-eta">
            {status === "IN_PROGRESS" ? t("track.eta.dropoff") : t("track.eta.pickup")}{" "}
            <b>
              {Math.max(1, Math.round(route.min))} {t("sch.min")}
            </b>{" "}
            · {route.km.toFixed(1)} {t("sch.km")}
          </div>
        )}
        {!ended && driver && (
          <button className="track-ar-btn" onClick={() => setArOpen(true)}>
            <ScanEye size={16} />
            {t("ar.button")}
          </button>
        )}
      </div>
      {arOpen && <ArFinder driver={driver} onClose={() => setArOpen(false)} />}
    </>
  );
}
