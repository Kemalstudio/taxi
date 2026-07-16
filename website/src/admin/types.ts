export type RideStatus =
  | "REQUESTED"
  | "SEARCHING"
  | "ACCEPTED"
  | "DRIVER_ARRIVED"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CANCELLED"
  | "NO_DRIVERS_FOUND";

export type DriverStatus = "OFFLINE" | "ONLINE" | "BUSY";

export type RideTariff = "ECONOMY" | "COMFORT" | "BUSINESS" | "ELECTRO";

export interface PlatformStats {
  totalUsers: number;
  totalDrivers: number;
  totalPassengers: number;
  totalRides: number;
  ridesByStatus: Record<string, number>;
  driversOnline: number;
  activeRides: number;
}

export interface GeoPoint {
  lat: number;
  lng: number;
}

export interface Ride {
  id: string;
  passengerId: string;
  driverId: string | null;
  pickup: GeoPoint;
  dropoff: GeoPoint;
  status: RideStatus;
  requestedAt: string;
  acceptedAt: string | null;
  arrivedAt: string | null;
  startedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
  cancelledReason: string | null;
  tariff: RideTariff;
  fare: number | null;
  promoCode: string | null;
  discountApplied: number | null;
}

export interface SosAlert {
  id: string;
  rideId: string;
  userId: string;
  lat: number;
  lng: number;
  note: string | null;
  createdAt: string;
}

export interface AdminDriver {
  userId: string;
  fullName: string;
  email: string;
  phone: string | null;
  status: DriverStatus;
  vehicleMake: string | null;
  vehicleModel: string | null;
  plateNumber: string | null;
  rating: string;
}

export interface OnlineDriver {
  driverId: string;
  lat: number;
  lng: number;
}
