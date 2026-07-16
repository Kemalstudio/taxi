import type { GeoPoint, RideTariff, RouteResult } from "../types";

export const TARIFF_MULTIPLIER: Record<RideTariff, number> = {
  ECONOMY: 1,
  COMFORT: 1.3,
  BUSINESS: 1.8,
  ELECTRO: 1.15,
};

/** Cash fare in manat (TMT): base + per-km, floored at a minimum, scaled by tariff. */
export function priceFor(km: number, tariff: RideTariff = "ECONOMY"): number {
  const base = Math.max(15, Math.round(10 + km * 3.2));
  return Math.round(base * TARIFF_MULTIPLIER[tariff]);
}

function haversine(a: GeoPoint, b: GeoPoint): number {
  const R = 6371;
  const dLat = ((b.lat - a.lat) * Math.PI) / 180;
  const dLng = ((b.lng - a.lng) * Math.PI) / 180;
  const s =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((a.lat * Math.PI) / 180) * Math.cos((b.lat * Math.PI) / 180) * Math.sin(dLng / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(s), Math.sqrt(1 - s));
}

/**
 * Real road route via the public OSRM server; falls back to a straight line +
 * haversine distance if the request fails (offline / rate-limited).
 */
export async function routeThrough(points: GeoPoint[]): Promise<RouteResult> {
  const coordStr = points.map((p) => `${p.lng},${p.lat}`).join(";");
  try {
    const res = await fetch(
      `https://router.project-osrm.org/route/v1/driving/${coordStr}?overview=full&geometries=geojson`,
    );
    const data = await res.json();
    if (!data.routes || !data.routes.length) throw new Error("no route");
    const route = data.routes[0];
    const coords: [number, number][] = route.geometry.coordinates.map((c: [number, number]) => [c[1], c[0]]);
    return { coords, km: route.distance / 1000, min: route.duration / 60, approximate: false };
  } catch {
    const coords: [number, number][] = points.map((p) => [p.lat, p.lng]);
    let km = 0;
    for (let i = 1; i < points.length; i++) km += haversine(points[i - 1], points[i]);
    return { coords, km, min: km / 0.5, approximate: true };
  }
}
