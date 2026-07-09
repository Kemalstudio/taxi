import type { GeoPoint } from "../types";

const BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const TOKEN_KEY = "taksi_token";

/** Thrown when the backend responded but rejected the request (bad creds, duplicate, …). */
export class ApiError extends Error {}
/** Thrown when the backend could not be reached at all (not running / CORS / offline). */
export class NetworkError extends Error {}

export const token = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t: string) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

/** The backend authenticates by email; we derive a stable one from the +993 phone. */
function phoneToEmail(phone: string): string {
  const digits = phone.replace(/\D/g, "");
  return `993${digits}@taksigo.local`;
}

interface AuthResponse {
  userId: string;
  role: string;
  token: string;
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

export async function register(phone: string, password: string, fullName: string): Promise<void> {
  const data = await post<AuthResponse>("/auth/register", {
    email: phoneToEmail(phone),
    password,
    role: "PASSENGER",
    fullName: fullName || "Ýolagçy",
    phone,
  });
  token.set(data.token);
}

export async function login(phone: string, password: string): Promise<void> {
  const data = await post<AuthResponse>("/auth/login", {
    email: phoneToEmail(phone),
    password,
  });
  token.set(data.token);
}

export interface RideResponse {
  id: string;
  status: string;
}

/** Creates a ride on the backend — it appears in the shared admin dashboard. */
export async function createRide(pickup: GeoPoint, dropoff: GeoPoint): Promise<RideResponse> {
  return post<RideResponse>(
    "/rides",
    {
      pickup: { lat: pickup.lat, lng: pickup.lng },
      dropoff: { lat: dropoff.lat, lng: dropoff.lng },
    },
    true,
  );
}
