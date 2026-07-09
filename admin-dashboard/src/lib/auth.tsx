import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { api, tokenStore } from "./api";
import type { AuthResponse } from "../types";

interface AuthState {
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => tokenStore.get());

  const value = useMemo<AuthState>(
    () => ({
      token,
      isAuthenticated: Boolean(token),
      login: async (email, password) => {
        const { data } = await api.post<AuthResponse>("/auth/login", { email, password });
        if (data.role !== "ADMIN") {
          throw new Error("This account is not an administrator.");
        }
        tokenStore.set(data.token);
        setToken(data.token);
      },
      logout: () => {
        tokenStore.clear();
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
