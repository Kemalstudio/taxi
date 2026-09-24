import { lazy, StrictMode, Suspense } from "react";
import { createRoot } from "react-dom/client";
import "maplibre-gl/dist/maplibre-gl.css";
import "@fontsource/nunito/400.css";
import "@fontsource/nunito/500.css";
import "@fontsource/nunito/600.css";
import "@fontsource/nunito/700.css";
import "@fontsource/nunito/800.css";
import "./index.css";
import App from "./App";
import { TrackPage } from "./components/TrackPage";
import { ThemeProvider } from "./theme";
import { I18nProvider } from "./i18n";

// Lazy so Tailwind/react-query/recharts/react-router only ship to visitors of /admin.
const AdminApp = lazy(() => import("./admin/AdminApp"));
// Lazy so react-router only ships to visitors of the marketing pages — the booking
// screen below stays a single unrouted view.
const MarketingApp = lazy(() => import("./marketing/MarketingApp"));
// Lazy so the driver cabinet's own chart/dashboard code only ships to signed-in drivers.
const DriverApp = lazy(() => import("./driver/DriverApp"));

const isTrackPage = location.pathname.startsWith("/track/");
const isAdmin = location.pathname.startsWith("/admin");
// Not a plain startsWith("/driver") — that would also swallow the "/drivers" marketing page.
const isDriver = location.pathname === "/driver" || location.pathname.startsWith("/driver/");
const MARKETING_PATHS = ["/drivers", "/business", "/partners"];
const isMarketing = MARKETING_PATHS.some((p) => location.pathname.startsWith(p));

function Root() {
  if (isAdmin) {
    return (
      <Suspense fallback={null}>
        <AdminApp />
      </Suspense>
    );
  }
  if (isDriver) {
    return (
      <Suspense fallback={null}>
        <DriverApp />
      </Suspense>
    );
  }
  if (isMarketing) {
    return (
      <Suspense fallback={null}>
        <MarketingApp />
      </Suspense>
    );
  }
  return (
    <ThemeProvider>
      <I18nProvider>{isTrackPage ? <TrackPage /> : <App />}</I18nProvider>
    </ThemeProvider>
  );
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Root />
  </StrictMode>,
);

// Offline support (PWA). Registered only in a production build so it never
// interferes with Vite's dev HMR — test offline via `npm run build && npm run preview`.
if (import.meta.env.PROD && "serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("/sw.js").catch(() => {
      /* offline support is best-effort */
    });
  });
}
