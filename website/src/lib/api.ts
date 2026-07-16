import type { ChatMessage, GeoPoint, RideDetails, RideTariff } from "../types";

const BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const TOKEN_KEY = "taksi_token";
const USER_ID_KEY = "taksi_user_id";
const ROLE_KEY = "taksi_role";

/** Thrown when the backend responded but rejected the request (bad creds, duplicate, …). */
export class ApiError extends Error {}
/** Thrown when the backend could not be reached at all (not running / CORS / offline). */
export class NetworkError extends Error {}

export const token = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t: string) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

export const userId = {
  get: () => localStorage.getItem(USER_ID_KEY),
  set: (id: string) => localStorage.setItem(USER_ID_KEY, id),
  clear: () => localStorage.removeItem(USER_ID_KEY),
};

/** Role of the currently signed-in user — set on login/register, read by the admin section. */
export const role = {
  get: () => localStorage.getItem(ROLE_KEY),
  set: (r: string) => localStorage.setItem(ROLE_KEY, r),
  clear: () => localStorage.removeItem(ROLE_KEY),
};

/** The backend authenticates by email; we derive a stable one from the +993 phone. */
function phoneToEmail(phone: string): string {
  const digits = phone.replace(/\D/g, "");
  return `993${digits}@taksigo.local`;
}

/** Accepts either a real email or a phone number and resolves to the email the backend expects. */
function resolveEmail(identifier: string): string {
  const trimmed = identifier.trim();
  return trimmed.includes("@") ? trimmed : phoneToEmail(trimmed);
}

export interface AuthResponse {
  userId: string;
  role: string;
  token: string;
}

async function handle<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const data = await res.json();
      if (data?.message) msg = data.message;
    } catch {
      /* ignore */
    }
    throw new ApiError(msg);
  }
  return res.json() as Promise<T>;
}

async function post<T>(path: string, body: unknown, auth = false): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(auth && token.get() ? { Authorization: `Bearer ${token.get()}` } : {}),
      },
      body: JSON.stringify(body),
    });
  } catch {
    throw new NetworkError("backend unreachable");
  }
  return handle<T>(res);
}

async function get<T>(path: string, auth = true): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, {
      headers: auth && token.get() ? { Authorization: `Bearer ${token.get()}` } : {},
    });
  } catch {
    throw new NetworkError("backend unreachable");
  }
  return handle<T>(res);
}

export async function register(phone: string, password: string, fullName: string): Promise<AuthResponse> {
  const data = await post<AuthResponse>("/auth/register", {
    email: phoneToEmail(phone),
    password,
    role: "PASSENGER",
    fullName: fullName || "Ýolagçy",
    phone,
  });
  token.set(data.token);
  userId.set(data.userId);
  role.set(data.role);
  return data;
}

/** Accepts either a phone number or an email as the identifier (see resolveEmail). */
export async function login(identifier: string, password: string): Promise<AuthResponse> {
  const data = await post<AuthResponse>("/auth/login", {
    email: resolveEmail(identifier),
    password,
  });
  token.set(data.token);
  userId.set(data.userId);
  role.set(data.role);
  return data;
}

export interface RideResponse {
  id: string;
  status: string;
}

/**
 * Creates a ride on the backend — it appears in the shared admin dashboard.
 * Pass `scheduledAt` (ISO instant) to book it for later (backend keeps it
 * SCHEDULED until due, then dispatches automatically).
 */
export async function createRide(
  pickup: GeoPoint,
  dropoff: GeoPoint,
  scheduledAt?: string,
  tariff?: RideTariff,
  fare?: number,
  promoCode?: string,
): Promise<RideResponse> {
  return post<RideResponse>(
    "/rides",
    {
      pickup: { lat: pickup.lat, lng: pickup.lng },
      dropoff: { lat: dropoff.lat, lng: dropoff.lng },
      ...(scheduledAt ? { scheduledAt } : {}),
      ...(tariff ? { tariff } : {}),
      ...(fare != null ? { fare } : {}),
      ...(promoCode ? { promoCode } : {}),
    },
    true,
  );
}

/** Full ride details (including driver info once assigned) — polled after ordering. */
export function getRide(rideId: string): Promise<RideDetails> {
  return get<RideDetails>(`/rides/${rideId}`);
}

export function cancelRide(rideId: string): Promise<unknown> {
  return post(`/rides/${rideId}/cancel`, {}, true);
}

export function rateRide(rideId: string, stars: number, comment?: string): Promise<unknown> {
  return post(`/rides/${rideId}/rating`, { stars, comment }, true);
}

export function getMessages(rideId: string): Promise<ChatMessage[]> {
  return get<ChatMessage[]>(`/rides/${rideId}/messages`);
}

export function sendMessage(rideId: string, body: string): Promise<ChatMessage> {
  return post<ChatMessage>(`/rides/${rideId}/messages`, { body }, true);
}

export function triggerSos(rideId: string, point: GeoPoint, note?: string): Promise<unknown> {
  return post(`/rides/${rideId}/sos`, { point: { lat: point.lat, lng: point.lng }, note }, true);
}

export interface PromoPreview {
  code: string;
  discountAmount: number;
  finalFare: number;
}

export function validatePromo(code: string, fare: number): Promise<PromoPreview> {
  return post<PromoPreview>("/promo/validate", { code, fare }, true);
}

export interface Me {
  userId: string;
  fullName: string;
  phone: string | null;
  role: string;
  loyaltyPoints: number;
}

export function getMe(): Promise<Me> {
  return get<Me>("/me");
}
