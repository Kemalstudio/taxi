export interface Place {
  name: string;
  sub: string;
  lat: number;
  lng: number;
}

/** A chosen point on the map (from / to / a stop). */
export interface GeoPoint {
  label: string;
  lat: number;
  lng: number;
}

/** An address input: the typed text plus the resolved point (once picked). */
export interface AddressField {
  text: string;
  point: GeoPoint | null;
}

export interface StopRow {
  id: number;
  field: AddressField;
}

export type RideMode = "now" | "later";

export interface RouteResult {
  /** [lat, lng] pairs forming the road geometry. */
  coords: [number, number][];
  km: number;
  min: number;
  /** true when OSRM failed and we fell back to a straight line. */
  approximate: boolean;
}
