import { useEffect, useRef } from "react";
import maplibregl, { type StyleSpecification, type GeoJSONSource } from "maplibre-gl";
import type { GeoPoint, RouteResult } from "../types";
import { bearing, distanceMeters, unwrapBearing } from "../lib/geo";

/** Below this, a "moved" reading is just GPS jitter — don't spin the car icon in place. */
const MIN_HEADING_DISTANCE_M = 3;

const ASHGABAT: [number, number] = [58.38, 37.95]; // [lng, lat]

/** Raster OSM style — no API key; MapLibre still tilts (pitch) for the 2.5D effect. */
const OSM_STYLE: StyleSpecification = {
  version: 8,
  sources: {
    osm: {
      type: "raster",
      tiles: [
        "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
        "https://b.tile.openstreetmap.org/{z}/{x}/{y}.png",
        "https://c.tile.openstreetmap.org/{z}/{x}/{y}.png",
      ],
      tileSize: 256,
      minzoom: 0,
      maxzoom: 18,
      attribution: "© OpenStreetMap",
    },
  },
  layers: [{ id: "osm", type: "raster", source: "osm" }],
};

function el(className: string): HTMLDivElement {
  const d = document.createElement("div");
  d.className = className;
  return d;
}

function pinEl(): HTMLDivElement {
  const d = document.createElement("div");
  d.style.filter = "drop-shadow(0 4px 6px rgba(0,0,0,.3))";
  d.innerHTML =
    '<svg width="30" height="40" viewBox="0 0 30 40"><path d="M15 0C6.7 0 0 6.7 0 15c0 10 15 25 15 25s15-15 15-25C30 6.7 23.3 0 15 0Z" fill="#F5333F"/><circle cx="15" cy="15" r="6" fill="#fff"/></svg>';
  return d;
}

/** Nose points up (bearing 0 / north) — MapLibre's Marker rotation handles turning it to heading. */
function driverEl(): HTMLDivElement {
  const d = document.createElement("div");
  d.className = "mk-driver";
  d.innerHTML =
    '<svg width="16" height="16" viewBox="0 0 24 24"><rect x="7" y="9" width="10" height="13" rx="3" fill="#16181C"/><path d="M12 2 L17 10 H7 Z" fill="#16181C"/></svg>';
  return d;
}

interface Props {
  mapRef: React.MutableRefObject<maplibregl.Map | null>;
  from: GeoPoint | null;
  to: GeoPoint | null;
  stops: GeoPoint[];
  route: RouteResult | null;
  me: GeoPoint | null;
  driver: GeoPoint | null;
  /** Fit the viewport to the route every time it changes (default true — booking flow, where a
   * new route means the user picked new points). Pass false for a live-updating route (e.g. the
   * driver's position feeding a recalculated ETA) so it redraws without yanking the map around
   * every refresh — it's still fit once, the first time a route appears. */
  autoFit?: boolean;
}

export function MapView({ mapRef, from, to, stops, route, me, driver, autoFit = true }: Props) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const markers = useRef<maplibregl.Marker[]>([]);
  const meMarker = useRef<maplibregl.Marker | null>(null);
  const driverMarker = useRef<maplibregl.Marker | null>(null);
  const driverPrevPoint = useRef<GeoPoint | null>(null);
  const driverHeading = useRef(0);
  const hasFittedRoute = useRef(false);
  const loaded = useRef(false);

  // init once
  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;
    const map = new maplibregl.Map({
      container: containerRef.current,
      style: OSM_STYLE,
      center: ASHGABAT,
      zoom: 12.5,
      pitch: 0,
      maxZoom: 18,
      maxPitch: 60,
      fadeDuration: 0,
      attributionControl: { compact: true },
    });
    map.on("load", () => {
      loaded.current = true;
      map.addSource("route", { type: "geojson", data: emptyLine() });
      map.addLayer({
        id: "route",
        type: "line",
        source: "route",
        layout: { "line-cap": "round", "line-join": "round" },
        paint: { "line-color": "#1DB268", "line-width": 6, "line-opacity": 0.95 },
      });
    });
    mapRef.current = map;
    return () => {
      map.remove();
      mapRef.current = null;
      loaded.current = false;
    };
  }, [mapRef]);

  // markers
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    markers.current.forEach((m) => m.remove());
    markers.current = [];
    if (from) markers.current.push(new maplibregl.Marker({ element: el("mk-from") }).setLngLat([from.lng, from.lat]).addTo(map));
    stops.forEach((s) => markers.current.push(new maplibregl.Marker({ element: el("mk-stop") }).setLngLat([s.lng, s.lat]).addTo(map)));
    if (to) markers.current.push(new maplibregl.Marker({ element: pinEl(), anchor: "bottom" }).setLngLat([to.lng, to.lat]).addTo(map));
  }, [mapRef, from, to, stops]);

  // "you are here" marker
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    if (meMarker.current) {
      meMarker.current.remove();
      meMarker.current = null;
    }
    if (me) {
      meMarker.current = new maplibregl.Marker({ element: el("mk-me") }).setLngLat([me.lng, me.lat]).addTo(map);
    }
  }, [mapRef, me]);

  // live driver marker — reuse one marker, move it (smooth via CSS transition) and turn it to
  // face its heading, computed from consecutive fixes (GPS pings don't carry a compass bearing).
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    if (!driver) {
      driverMarker.current?.remove();
      driverMarker.current = null;
      driverPrevPoint.current = null;
      return;
    }
    const prev = driverPrevPoint.current;
    if (prev && distanceMeters(prev, driver) > MIN_HEADING_DISTANCE_M) {
      driverHeading.current = unwrapBearing(driverHeading.current, bearing(prev, driver));
    }
    driverPrevPoint.current = driver;
    if (!driverMarker.current) {
      driverMarker.current = new maplibregl.Marker({ element: driverEl(), rotationAlignment: "map" })
        .setLngLat([driver.lng, driver.lat])
        .setRotation(driverHeading.current)
        .addTo(map);
    } else {
      driverMarker.current.setLngLat([driver.lng, driver.lat]);
      driverMarker.current.setRotation(driverHeading.current);
    }
  }, [mapRef, driver]);

  // route line + fit
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    const apply = () => {
      const src = map.getSource("route") as GeoJSONSource | undefined;
      if (!src) return;
      if (!route || route.coords.length < 2) {
        src.setData(emptyLine());
        return;
      }
      src.setData({
        type: "Feature",
        properties: {},
        geometry: { type: "LineString", coordinates: route.coords.map((c) => [c[1], c[0]]) },
      });
      if (!autoFit && hasFittedRoute.current) return;
      hasFittedRoute.current = true;
      const lngs = route.coords.map((c) => c[1]);
      const lats = route.coords.map((c) => c[0]);
      map.fitBounds(
        [
          [Math.min(...lngs), Math.min(...lats)],
          [Math.max(...lngs), Math.max(...lats)],
        ],
        { padding: { top: 120, left: 400, right: 60, bottom: 140 }, duration: 600 },
      );
    };
    if (loaded.current) apply();
    else map.once("load", apply);
  }, [mapRef, route, autoFit]);

  return <div id="map" ref={containerRef} />;
}

function emptyLine(): GeoJSON.Feature {
  return { type: "Feature", properties: {}, geometry: { type: "LineString", coordinates: [] } };
}
