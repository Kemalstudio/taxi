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

function shouldCache(request) {
  if (request.method !== "GET") return false;
  const url = new URL(request.url);
  if (url.origin === self.location.origin) return true; // app shell + assets
  if (url.hostname.endsWith("tile.openstreetmap.org")) return true; // map tiles
  return false;
}

self.addEventListener("fetch", (event) => {
  if (!shouldCache(event.request)) return;
  event.respondWith(
    (async () => {
      const cache = await caches.open(CACHE);
      const cached = await cache.match(event.request);
      const network = fetch(event.request)
        .then((res) => {
          if (res && res.ok) cache.put(event.request, res.clone());
          return res;
        })
        .catch(() => cached);
      // Serve cache immediately when we have it; otherwise wait for the network.
      return cached || network;
    })(),
  );
});
