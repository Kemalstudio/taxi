import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api } from "./api";
import { token as tokenStore, userId as userIdStore, role as roleStore } from "../../lib/api";

const PERMISSIONS_KEY = "taksi_admin_permissions";
const BACK_OFFICE_ROLES = new Set([
  "OPERATOR",
  "DISPATCHER",
  "MODERATOR",
  "ACCOUNTANT",
  "ADMIN",
  "SUPER_ADMIN",
]);

export interface PendingTwoFactor {
  challengeId: string;
  setupRequired: boolean;
  expiresAt: string;
  secret?: string;
  provisioningUri?: string;
}

interface AdminAuthResponse {
  userId: string;
  role: string;
  token: string;
  permissions: string[];
}

interface AuthState {
  token: string | null;
  role: string | null;
  permissions: ReadonlySet<string>;
  isAuthenticated: boolean;
  hasPermission: (permission: string) => boolean;
  login: (identifier: string, password: string) => Promise<PendingTwoFactor>;
  verifyTwoFactor: (challengeId: string, code: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

function resolveEmail(identifier: string): string {
  const trimmed = identifier.trim();
  if (trimmed.includes("@")) return trimmed;
  return `993${trimmed.replace(/\D/g, "")}@taksigo.local`;
}

function loadPermissions(): Set<string> {
  try {
    const stored = JSON.parse(localStorage.getItem(PERMISSIONS_KEY) ?? "[]");
    return new Set(Array.isArray(stored) ? stored.filter((item): item is string => typeof item === "string") : []);
  } catch {
    return new Set();
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const storedRole = roleStore.get();
  const initialRole = BACK_OFFICE_ROLES.has(storedRole ?? "") ? storedRole : null;
  const [token, setToken] = useState<string | null>(() => (initialRole ? tokenStore.get() : null));
  const [role, setRole] = useState<string | null>(initialRole);
  const [permissions, setPermissions] = useState<Set<string>>(() => (initialRole ? loadPermissions() : new Set()));

  useEffect(() => {
    if (!token) return;
    let cancelled = false;
    api.get<{ userId: string; role: string; permissions: string[]; twoFactorEnabled: boolean }>("/me")
      .then(({ data }) => {
        if (cancelled) return;
        if (!BACK_OFFICE_ROLES.has(data.role) || !data.twoFactorEnabled) {
          tokenStore.clear();
          userIdStore.clear();
          roleStore.clear();
          localStorage.removeItem(PERMISSIONS_KEY);
          setToken(null);
          setRole(null);
          setPermissions(new Set());
          return;
        }
        roleStore.set(data.role);
        localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(data.permissions));
        setRole(data.role);
        setPermissions(new Set(data.permissions));
      })
      .catch((error: { response?: { status?: number } }) => {
        if (cancelled || (error.response?.status !== 401 && error.response?.status !== 403)) return;
        tokenStore.clear();
        userIdStore.clear();
        roleStore.clear();
        localStorage.removeItem(PERMISSIONS_KEY);
        setToken(null);
        setRole(null);
        setPermissions(new Set());
      });
    return () => { cancelled = true; };
  }, [token]);

  const value = useMemo<AuthState>(
    () => ({
      token,
      role,
      permissions,
      isAuthenticated: Boolean(token),
      hasPermission: (permission) => permissions.has(permission),
      login: async (identifier, password) => {
        const loginResponse = await api.post<PendingTwoFactor>("/auth/admin/login", {
          email: resolveEmail(identifier),
          password,
        });
        const pending = loginResponse.data;
        if (!pending.setupRequired) return pending;
        const setupResponse = await api.post<{ secret: string; provisioningUri: string }>("/auth/admin/2fa/setup", {
          challengeId: pending.challengeId,
        });
        return { ...pending, ...setupResponse.data };
      },
      verifyTwoFactor: async (challengeId, code) => {
        const { data } = await api.post<AdminAuthResponse>("/auth/admin/2fa/verify", { challengeId, code });
        if (!BACK_OFFICE_ROLES.has(data.role)) throw new Error("This account is not a back-office account.");
        tokenStore.set(data.token);
        userIdStore.set(data.userId);
        roleStore.set(data.role);
        localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(data.permissions));
        setToken(data.token);
        setRole(data.role);
        setPermissions(new Set(data.permissions));
      },
      logout: () => {
        tokenStore.clear();
        userIdStore.clear();
        roleStore.clear();
        localStorage.removeItem(PERMISSIONS_KEY);
        setToken(null);
        setRole(null);
        setPermissions(new Set());
      },
    }),
    [token, role, permissions],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
