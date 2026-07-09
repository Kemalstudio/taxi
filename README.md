# Taxi Platform

A staged build-out of a ride-hailing platform. Each phase is a real, runnable
slice rather than a mockup:

- **Phase 1 — Backend core** (`backend/`): Kotlin + Spring Boot + PostgreSQL +
  Redis service owning the full ride lifecycle, including automatic
  nearest-driver dispatch with reject/timeout reassignment.
- **Phase 2 — Driver app** (`driver-app/`): native Android client (Kotlin +
  Jetpack Compose) for the driver side of that lifecycle — register/login,
  go online with live location, receive ride offers in real time, and drive
  a ride through arrived → started → completed.
- **Phase 3 — Admin dashboard** (`admin-dashboard/`): React + Vite +
  TypeScript + Tailwind operations console — live driver map (OpenStreetMap),
  real-time KPIs, rides-by-status analytics, and rides/drivers tables, wired
  to admin-only backend endpoints.

Not yet built: passenger app, payments, AI services, or social/biometric
login — see each phase's "Roadmap" section below.

## Backend (Phase 1)

### Prerequisites

- JDK 21+
- Docker + Docker Compose

### Run everything with Docker Compose (recommended)

```bash
docker compose up --build
```

This starts Postgres, Redis, and the Spring Boot app on `http://localhost:8080`.
Flyway migrations run automatically on startup.

### Run the backend locally against Dockerized Postgres/Redis

```bash
docker compose up -d postgres redis
cd backend
./gradlew bootRun
```

### Run tests

```bash
cd backend
./gradlew test
```

The integration test (`RideLifecycleIntegrationTest`) uses Testcontainers and
requires Docker to be running.

### API overview

- `POST /auth/register`, `POST /auth/login`
- `POST /rides`, `GET /rides/{id}`, `POST /rides/{id}/cancel`
- `POST /rides/{id}/accept`, `POST /rides/{id}/reject` (driver)
- `POST /rides/{id}/arrive`, `POST /rides/{id}/start`, `POST /rides/{id}/complete` (driver)
- `POST /driver/status`, `POST /driver/location`
- WebSocket (STOMP over SockJS) at `/ws`:
  - Driver subscribes to `/topic/driver/{driverId}` for incoming ride offers
  - Passenger/driver subscribe to `/topic/ride/{rideId}` for status changes
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### How dispatch works

See `application/dispatch/DispatchService.kt` and `OfferTimeoutScheduler.kt`.
On request, the nearest online driver (via Redis `GEOSEARCH`) is offered the
ride with a 15s window. An explicit reject or a silent timeout both advance
the offer to the next-nearest untried driver automatically, until one accepts
or candidates are exhausted (`NO_DRIVERS_FOUND`).

### Roadmap (explicitly out of scope for this phase)

OTP/SMS, social/biometric login, GraphQL/gRPC, payments/wallet, promo codes,
admin dashboards, AI services (matching/fraud/surge), Kubernetes, CI/CD.

## Driver App (Phase 2)

Native Android app (Kotlin + Jetpack Compose + Material 3, MVVM/Clean
Architecture, Hilt, Retrofit, Coroutines/Flow) in `driver-app/`.

### Prerequisites

- Android Studio (Koala+) with Android SDK — **not installed in this
  environment**; this code has been written and manually reviewed but not
  compiled here. Open `driver-app/` in Android Studio to build/run it.
- The backend running (see above).

### Running it

1. Open `driver-app/` as a project in Android Studio and let it sync (this
   downloads the Android Gradle Plugin, SDK platform/build-tools, and all
   dependencies — expect this to take a while on first sync).
2. The backend base URL defaults to `http://10.0.2.2:8080/`, which reaches
   your host machine's `localhost:8080` from the Android **emulator**. If
   you're running on a **physical device**, override it:
   ```bash
   ./gradlew :app:assembleDebug -PbackendBaseUrl=http://<your-lan-ip>:8080/
   ```
3. Run the app. Register as a driver, toggle online (grants location
   permission), and you're ready to receive ride offers.

### End-to-end check

1. `docker compose up --build` (backend, from repo root).
2. In the driver app: register, go online.
3. As a passenger, call the backend directly to request a ride, e.g.:
   ```bash
   curl -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" \
     -d '{"email":"passenger@example.com","password":"SuperSecret123","role":"PASSENGER","fullName":"Test Passenger"}'
   # use the returned token:
   curl -X POST http://localhost:8080/rides -H "Content-Type: application/json" -H "Authorization: Bearer <token>" \
     -d '{"pickup":{"lat":52.52,"lng":13.405},"dropoff":{"lat":52.53,"lng":13.41}}'
   ```
4. The driver app should show the incoming offer within a couple of seconds
   (the driver's reported location needs to be near the pickup point for
   dispatch to find it). Accept it, then walk it through
   arrived → start → complete.

### Architecture notes

- `data/remote/ws/StompClient.kt` is a hand-rolled STOMP-over-WebSocket
  client (avoids depending on unmaintained Android STOMP libraries) talking
  to the backend's raw SockJS transport at `/ws/websocket`.
- `domain/repository/RideEventsRepository.currentOffer` is a `StateFlow`
  (not a cold event stream) specifically so a screen that starts observing
  *after* an offer arrived — e.g. Home reacting and navigating to the Offer
  screen — still sees it instead of silently missing it.

### Roadmap (explicitly out of scope for this phase)

Google Maps rendering (pickup/dropoff shown as coordinates for now), push
notifications, earnings/documents/heat maps, biometric login, background
(foreground-service) location updates, offline mode, tablet/foldable
layouts, passenger app.

## Admin Dashboard (Phase 3)

Operations console in `admin-dashboard/` — React 18 + Vite + TypeScript +
Tailwind, TanStack Query for polling, Recharts for analytics, and Leaflet +
OpenStreetMap for the live driver map (no API key required). Premium dark
"glass" UI.

### Backend additions for this phase

- New `ADMIN` role; an admin user is **seeded on startup** if none exists
  (`admin@taxi.local` by default — override with `ADMIN_EMAIL` /
  `ADMIN_PASSWORD`; disable with `ADMIN_SEED_ENABLED=false`).
- Admin-only endpoints (require an ADMIN JWT):
  `GET /admin/stats`, `GET /admin/rides?status=&limit=`,
  `GET /admin/drivers`, `GET /admin/drivers/online` (live map feed).
- CORS is enabled for the dashboard origin
  (`CORS_ALLOWED_ORIGINS`, default `http://localhost:5173`).

### Prerequisites

- Node.js 18+ and npm.
- The backend running (see above).

### Running it

```bash
cd admin-dashboard
npm install
npm run dev          # http://localhost:5173
```

The API base URL is read from `.env` (`VITE_API_BASE_URL`, default
`http://localhost:8080`). Log in with the seeded admin credentials. A
non-admin account is rejected at login.

### Build / verify

```bash
cd admin-dashboard
npm run build        # tsc typecheck + vite production build
```

### End-to-end check

1. Start the backend (`docker compose up --build`).
2. `npm run dev` in `admin-dashboard/`, log in as the admin.
3. Register a driver (via the driver app or `curl`), have them go online and
   post a location; a marker appears on the live map and the "Drivers online"
   KPI ticks up. Request a ride as a passenger and watch the KPIs, the
   rides-by-status chart, and the Rides table update in near-real-time.

### Roadmap (explicitly out of scope for this phase)

KYC/verification workflows, payments/refunds admin, CMS, role-permission
editor, audit-log viewer, promotions/coupons, real analytics warehouse,
separate operator/super-admin dashboards, SSO.

## Taksi Go Website (Phase 4)

Passenger-facing booking website in `website/` — **React 18 + Vite +
TypeScript**, styled after Yandex Go (yellow accent, white panels, real map).
Ported from the `design/taksi-go-web.html` prototype into a proper component
codebase using npm libraries instead of vendored files:

- **react-leaflet** + **leaflet** — real OpenStreetMap tiles, custom markers.
- **OSRM** public router — actual road routes A→B (through stops), with a
  straight-line haversine fallback if offline.
- **lucide-react** — icons. **@fontsource/plus-jakarta-sans** — self-hosted font.

Features: pick points A and B with autocomplete, add/remove stops, comment to
driver, "order for another person", accessibility toggles, **booking**
(Сейчас / Ко времени with date+time), live fare in manat (TMT, cash), and an
order/booking confirmation. Component structure under `website/src/components`
(`TopNav`, `MapView`, `OrderPanel` pieces: `AddressCard`, `SavedGrid`,
`OptionsCard`, `ScheduleCard`, `OrderModal`); state orchestrated in `App.tsx`,
routing/pricing in `src/lib/routing.ts`.

### Prerequisites

- Node.js 18+ and npm. (Map tiles + OSRM need internet.)

### Running it

```bash
cd website
npm install
npm run dev          # http://localhost:5174
```

### Build / verify

```bash
cd website
npm run build        # tsc typecheck + vite production build
```

### Product features

- **Dark / light theme** (top-nav toggle, persisted).
- **Languages: Russian / English / Türkmen** (top-nav switcher, persisted) —
  dictionary in `src/i18n.tsx`.
- **GPS "my location"** — browser geolocation sets the pickup point.
- **3D map** — the map runs on **MapLibre GL (WebGL)**; the `3D/2D` button
  tilts it (pitch) for a perspective view. Uses raster OSM tiles (no API key).
- **Login / Register** (`LoginModal`) — talks to the real backend.

### Connected to the backend (shared API)

The website talks to the **same backend** as the driver app and admin
dashboard, so an order placed on the site shows up in the admin dashboard's
rides list.

- `src/lib/api.ts` calls `POST /auth/register` / `POST /auth/login`
  (a `+993` phone is mapped to a stable email) and `POST /rides`.
- Base URL from `website/.env` (`VITE_API_BASE_URL`, default
  `http://localhost:8080`).
- The backend's CORS now allows the site origins (5174/5175) alongside the
  admin dashboard (5173) — see `taxi.cors.allowed-origins`.
- If the backend isn't running, the site falls back to a **demo mode** so it
  stays usable; scheduled ("Ко времени") bookings are confirmed locally
  (backend ride scheduling is a later phase).

### End-to-end check (needs Docker for the backend)

1. `docker compose up --build` (backend + Postgres + Redis).
2. `cd website && npm run dev`, open it, **Sign in** (registers a passenger),
   pick A→B, **Заказать**.
3. `cd admin-dashboard && npm run dev`, log in as the seeded admin — the new
   ride appears in **Rides** and the KPIs update. The driver app (once built)
   talks to the same API, so drivers can accept these rides.

### Roadmap (explicitly out of scope for this phase)

Offline map (downloaded Ashgabat vector tiles + PWA cache), backend ride
**scheduling** for the "Ко времени" bookings, live driver tracking after
ordering, marketing/landing pages.
