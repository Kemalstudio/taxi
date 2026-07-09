import { useEffect } from "react";
import { MapContainer, TileLayer, Marker, Polyline, useMap } from "react-leaflet";
import type L from "leaflet";
import { fromIcon, stopIcon, pinIcon } from "../lib/mapIcons";
import type { GeoPoint, RouteResult } from "../types";

const ASHGABAT: [number, number] = [37.95, 58.38];

function FitToRoute({ route }: { route: RouteResult | null }) {
  const map = useMap();
  useEffect(() => {
    if (!route || route.coords.length < 2) return;
    const lats = route.coords.map((c) => c[0]);
    const lngs = route.coords.map((c) => c[1]);
    map.fitBounds(
      [
        [Math.min(...lats), Math.min(...lngs)],
        [Math.max(...lats), Math.max(...lngs)],
      ],
      { paddingTopLeft: [400, 120], paddingBottomRight: [60, 140] },
    );
  }, [route, map]);
  return null;
}

interface Props {
  mapRef: React.MutableRefObject<L.Map | null>;
  from: GeoPoint | null;
  to: GeoPoint | null;
  stops: GeoPoint[];
  route: RouteResult | null;
}

export function MapView({ mapRef, from, to, stops, route }: Props) {
  return (
    <MapContainer
      id="map"
      ref={mapRef}
      center={ASHGABAT}
      zoom={13}
      zoomControl={false}
      attributionControl
    >
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution="© OpenStreetMap"
        maxZoom={19}
      />
      {from && <Marker position={[from.lat, from.lng]} icon={fromIcon} />}
      {stops.map((s, i) => (
        <Marker key={`stop-${i}`} position={[s.lat, s.lng]} icon={stopIcon} />
      ))}
      {to && <Marker position={[to.lat, to.lng]} icon={pinIcon} />}
      {route && route.coords.length > 1 && (
        <Polyline
          positions={route.coords}
          pathOptions={{
            color: "#1DB268",
            weight: 6,
            opacity: route.approximate ? 0.9 : 0.95,
            lineCap: "round",
            lineJoin: "round",
            dashArray: route.approximate ? "2 10" : undefined,
          }}
        />
      )}
      <FitToRoute route={route} />
    </MapContainer>
  );
}
