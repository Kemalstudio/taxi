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

export type RideTariff = "ECONOMY" | "COMFORT" | "BUSINESS" | "ELECTRO";

export type RideStatus =
  | "SCHEDULED"
  | "REQUESTED"
  | "SEARCHING"
  | "ACCEPTED"
  | "DRIVER_ARRIVED"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CANCELLED"
  | "NO_DRIVERS_FOUND";

export interface DriverInfo {
  userId: string;
  fullName: string;
  phone: string | null;
  vehicleMake: string | null;
  vehicleModel: string | null;
  plateNumber: string | null;
  rating: string;
}

export interface RideDetails {
  id: string;
  passengerId: string;
  driverId: string | null;
  status: RideStatus;
  tariff: RideTariff;
  fare: number | null;
  promoCode: string | null;
  discountApplied: number | null;
  driver: DriverInfo | null;
}

export interface ChatMessage {
  id: string;
  rideId: string;
  senderId: string;
  senderRole: string;
  body: string;
  createdAt: string;
}
