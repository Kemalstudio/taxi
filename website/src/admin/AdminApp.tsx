import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import type { ReactNode } from "react";
// Scoped to this lazy-loaded chunk so the font payload only ships to /admin visitors.
import "@fontsource/ibm-plex-sans/400.css";
import "@fontsource/ibm-plex-sans/500.css";
import "@fontsource/ibm-plex-sans/600.css";
import "@fontsource/ibm-plex-sans/700.css";
import "@fontsource/ibm-plex-mono/400.css";
import "@fontsource/ibm-plex-mono/500.css";
import "@fontsource/ibm-plex-mono/600.css";
import { AuthProvider, useAuth } from "./lib/auth";
import { AppShell } from "./components/layout/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";
import { RidesPage } from "./pages/RidesPage";
import { RideDetailPage } from "./pages/RideDetailPage";
import { DriversPage } from "./pages/DriversPage";
import { DriverDetailPage } from "./pages/DriverDetailPage";
import { DriverEarningsPage } from "./pages/DriverEarningsPage";
import { UsersPage } from "./pages/UsersPage";
import { PromoCodesPage } from "./pages/PromoCodesPage";
import { DesignSettingsPage } from "./pages/DesignSettingsPage";
import { PricingSettingsPage } from "./pages/PricingSettingsPage";
import { AuditLogPage } from "./pages/AuditLogPage";
import { FinancePage } from "./pages/FinancePage";
import { NotificationsPage } from "./pages/NotificationsPage";
import { FleetMapPage } from "./pages/FleetMapPage";
import "./styles/admin.css";

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
});

function RequireAuth({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <AppShell>{children}</AppShell>;
}

function RequirePermission({ permission, children }: { permission: string; children: ReactNode }) {
  const { isAuthenticated, hasPermission } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!hasPermission(permission)) return <Navigate to="/" replace />;
  return <AppShell>{children}</AppShell>;
}

function AdminRoutes() {
  const { isAuthenticated } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <DashboardPage />
          </RequireAuth>
        }
      />
      <Route
        path="/fleet-map"
        element={
          <RequirePermission permission="admin.drivers.view">
            <FleetMapPage />
          </RequirePermission>
        }
      />
      <Route
        path="/rides"
        element={
          <RequirePermission permission="admin.rides.view">
            <RidesPage />
          </RequirePermission>
        }
      />
      <Route
        path="/rides/:rideId"
        element={
          <RequirePermission permission="admin.rides.view">
            <RideDetailPage />
          </RequirePermission>
        }
      />
      <Route
        path="/drivers"
        element={
          <RequirePermission permission="admin.drivers.view">
            <DriversPage />
          </RequirePermission>
        }
      />
      <Route
        path="/drivers/:driverId"
        element={
          <RequirePermission permission="admin.drivers.view">
            <DriverDetailPage />
          </RequirePermission>
        }
      />
      <Route
        path="/drivers/:driverId/earnings"
        element={
          <RequirePermission permission="admin.finance.view">
            <DriverEarningsPage />
          </RequirePermission>
        }
      />
      <Route
        path="/users"
        element={
          <RequirePermission permission="admin.users.view">
            <UsersPage />
          </RequirePermission>
        }
      />
      <Route
        path="/promo-codes"
        element={
          <RequirePermission permission="admin.promo.manage">
            <PromoCodesPage />
          </RequirePermission>
        }
      />
      <Route
        path="/design"
        element={
          <RequirePermission permission="admin.settings.manage">
            <DesignSettingsPage />
          </RequirePermission>
        }
      />
      <Route
        path="/pricing"
        element={
          <RequirePermission permission="admin.settings.manage">
            <PricingSettingsPage />
          </RequirePermission>
        }
      />
      <Route
        path="/finance"
        element={
          <RequirePermission permission="admin.finance.view">
            <FinancePage />
          </RequirePermission>
        }
      />
      <Route
        path="/notifications"
        element={
          <RequirePermission permission="admin.notifications.send">
            <NotificationsPage />
          </RequirePermission>
        }
      />
      <Route
        path="/audit-log"
        element={
          <RequirePermission permission="admin.audit.view">
            <AuditLogPage />
          </RequirePermission>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function AdminApp() {
  return (
    <div className="admin-shell">
      <QueryClientProvider client={queryClient}>
        <BrowserRouter basename="/admin">
          <AuthProvider>
            <AdminRoutes />
          </AuthProvider>
        </BrowserRouter>
      </QueryClientProvider>
    </div>
  );
}
