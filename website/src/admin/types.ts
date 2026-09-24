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

export type PaymentMethod = "CASH" | "CARD";
export type PaymentStatus = "NOT_APPLICABLE" | "PENDING" | "CONFIRMED" | "FAILED";

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

export interface DriverInfo {
  userId: string;
  fullName: string;
  phone: string | null;
  vehicleMake: string | null;
  vehicleModel: string | null;
  plateNumber: string | null;
  rating: string;
}

export interface Ride {
  id: string;
  passengerId: string;
  driverId: string | null;
  pickup: GeoPoint;
  dropoff: GeoPoint;
  pickupLabel: string | null;
  dropoffLabel: string | null;
  status: RideStatus;
  requestedAt: string;
  scheduledAt: string | null;
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
  surgeMultiplier: number;
  cancellationFee: number | null;
  /** What cancelling right now would cost — only present on a fresh GET of a single ride. */
  estimatedCancellationFee?: number | null;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  driver?: DriverInfo | null;
}

export interface PassengerInfo {
  userId: string;
  fullName: string;
  phone: string | null;
  email: string;
}

export interface RideOfferSummary {
  driverId: string;
  driverName: string;
  status: "PENDING" | "ACCEPTED" | "REJECTED" | "EXPIRED" | "CANCELLED";
  offeredAt: string;
  expiresAt: string;
  respondedAt: string | null;
}

export interface AdminRideDetail {
  ride: Ride;
  passenger: PassengerInfo | null;
  offers: RideOfferSummary[];
}

export interface ChatMessage {
  id: string;
  rideId: string;
  senderId: string;
  senderRole: string;
  body: string;
  createdAt: string;
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

export type VerificationStatus = "PENDING" | "APPROVED" | "REJECTED";
export type DocumentKind = "LICENSE" | "VEHICLE";

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
  verificationStatus: VerificationStatus;
  rejectionReason: string | null;
  hasLicenseDoc: boolean;
  hasVehicleDoc: boolean;
  banned: boolean;
  bannedReason: string | null;
  createdAt: string;
}

export interface OnlineDriver {
  driverId: string;
  lat: number;
  lng: number;
}

export type Role =
  | "PASSENGER"
  | "DRIVER"
  | "OPERATOR"
  | "DISPATCHER"
  | "MODERATOR"
  | "ACCOUNTANT"
  | "ADMIN"
  | "SUPER_ADMIN";

export interface AdminUser {
  userId: string;
  email: string;
  fullName: string;
  phone: string | null;
  role: Role;
  createdAt: string;
  banned: boolean;
  bannedReason: string | null;
}

export type DiscountType = "PERCENT" | "FIXED";

export interface AdminPromoCode {
  id: string;
  code: string;
  discountType: DiscountType;
  discountValue: number;
  maxUses: number | null;
  usedCount: number;
  active: boolean;
  expiresAt: string | null;
}

/** Only surfaced while there's no real SMS gateway wired in — see backend LoggingOtpAdapter. */
export interface OtpChallenge {
  phone: string;
  code: string;
  expiresAt: string;
  consumedAt: string | null;
  createdAt: string;
}

export interface DriverEarningsPoint {
  date: string;
  totalFare: number;
  rideCount: number;
}

export interface DriverEarnings {
  driverId: string;
  totalFare: number;
  rideCount: number;
  points: DriverEarningsPoint[];
}

export interface DriverActivityStats {
  driverId: string;
  completedRides: number;
  cancelledRides: number;
  averageRating: string | null;
}

export interface PlatformRevenue {
  totalFare: number;
  rideCount: number;
  points: DriverEarningsPoint[];
}

export type PayoutStatus = "PENDING" | "PAID";

export interface Payout {
  id: string;
  driverId: string;
  periodFrom: string;
  periodTo: string;
  amount: number;
  rideCount: number;
  status: PayoutStatus;
  createdAt: string;
  paidAt: string | null;
}

export interface TariffRevenuePoint {
  tariff: RideTariff;
  totalFare: number;
  rideCount: number;
}

export type BroadcastSegment = "ALL_DRIVERS" | "ONLINE_DRIVERS" | "ALL_PASSENGERS" | "ALL_USERS";

export interface BroadcastLogEntry {
  id: string;
  actorName: string;
  segment: BroadcastSegment;
  title: string;
  body: string;
  recipientCount: number;
  createdAt: string;
}

export interface AuditLogEntry {
  id: string;
  actorId: string;
  actorName: string;
  action: string;
  targetType: string;
  targetId: string | null;
  details: string | null;
  createdAt: string;
}

export interface SystemStatus {
  databaseReachable: boolean;
  dispatchReachable: boolean;
  smsGatewayConfigured: boolean;
  pushNotificationsConfigured: boolean;
}
