/* Taksi Go — offline service worker.
 * Runtime cache-first (stale-while-revalidate) for:
 *  - the app shell + built assets (same-origin: index.html, /assets/*.js/.css/.woff2)
 *  - OpenStreetMap map tiles (so already-viewed areas of Aşgabat work offline)
 * Nothing is precached by name (Vite hashes filenames); we cache what's actually fetched.
 */
const CACHE = "taksi-go-v1";

self.addEventListener("install", () => self.skipWaiting());

self.addEventListener("activate", (event) => {
  event.waitUntil(
    (async () => {
      const keys = await caches.keys();
      await Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k)));
      await self.clients.claim();
    })(),
  );
});

function isTile(url) {
  return url.hostname.endsWith("tile.openstreetmap.org");
}

function shouldCache(request) {
  if (request.method !== "GET") return false;
  const url = new URL(request.url);
  return url.origin === self.location.origin || isTile(url);
}

/** Normalise tile keys so any a/b/c subdomain maps to the same cached entry. */
function cacheKey(request) {
  const url = new URL(request.url);
  if (isTile(url)) return new Request(`https://tile.openstreetmap.org${url.pathname}`);
  return request;
}

// ---- Web Push: real OS-level notifications, delivered even when the tab/PWA is closed ----
// The backend currently sends an empty push body (see WebPushSender.kt) — this default text
// covers that case; a payload (if one's ever added) still overrides it below.
self.addEventListener("push", (event) => {
  let data = { title: "Taksi Go", body: "Обновление по вашей поездке — откройте приложение" };
  try {
    if (event.data) data = { ...data, ...event.data.json() };
  } catch {
    if (event.data) data.body = event.data.text();
  }
  event.waitUntil(
    self.registration.showNotification(data.title || "Taksi Go", {
      body: data.body || "",
      icon: "/icon.svg",
    }),
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  event.waitUntil(
    (async () => {
      const clients = await self.clients.matchAll({ type: "window", includeUncontrolled: true });
      const existing = clients.find((c) => "focus" in c);
      if (existing) return existing.focus();
      return self.clients.openWindow("/");
    })(),
  );
});

self.addEventListener("fetch", (event) => {
  if (!shouldCache(event.request)) return;
  const req = event.request;
  const url = new URL(req.url);
  const isNavigation = req.mode === "navigate" || (url.origin === self.location.origin && url.pathname === "/");

  event.respondWith(
    (async () => {
      const cache = await caches.open(CACHE);
      const key = cacheKey(req);

      // The HTML shell: network-first so a fresh deploy loads online; cache is the offline fallback.
      if (isNavigation) {
        try {
          const res = await fetch(req);
          if (res && res.ok) cache.put(key, res.clone());
          return res;
        } catch {
          return (await cache.match(key)) || Response.error();
        }
      }

      // Hashed assets + map tiles: cache-first (stale-while-revalidate).
      const cached = await cache.match(key);
      const network = fetch(req)
        .then((res) => {
          if (res && res.ok) cache.put(key, res.clone());
          return res;
        })
        .catch(() => cached);
      return cached || network;
    })(),
  );
});
