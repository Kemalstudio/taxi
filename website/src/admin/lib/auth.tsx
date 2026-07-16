import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { login as apiLogin, token as tokenStore, userId as userIdStore, role as roleStore } from "../../lib/api";

const ADMIN_ROLES = new Set(["ADMIN", "SUPER_ADMIN"]);

interface AuthState {
  token: string | null;
  isAuthenticated: boolean;
  login: (identifier: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  // Only trust an existing token here if it belongs to an admin — a rider session
  // signed in elsewhere on the site must not grant access to /admin.
  const [token, setToken] = useState<string | null>(() =>
    ADMIN_ROLES.has(roleStore.get() ?? "") ? tokenStore.get() : null,
  );

  const value = useMemo<AuthState>(
    () => ({
      token,
      isAuthenticated: Boolean(token),
      login: async (identifier, password) => {
        const data = await apiLogin(identifier, password);
        if (!ADMIN_ROLES.has(data.role)) {
          throw new Error("This account is not an administrator.");
        }
        setToken(data.token);
      },
      logout: () => {
        tokenStore.clear();
        userIdStore.clear();
        roleStore.clear();
        setToken(null);
      },
    }),
    [token],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
