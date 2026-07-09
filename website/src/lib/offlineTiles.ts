/** Pre-download Aşgabat OSM tiles into the SW cache so the city works offline
 *  and zoom/3D stay fast (served from cache instead of the network). */

const CACHE = "taksi-go-v1"; // must match public/sw.js
const BBOX = { lngMin: 58.2, lngMax: 58.52, latMin: 37.85, latMax: 38.02 };
const ZOOMS = [11, 12, 13, 14, 15];
const CONCURRENCY = 6;

function lon2tile(lon: number, z: number): number {
  return Math.floor(((lon + 180) / 360) * 2 ** z);
}
function lat2tile(lat: number, z: number): number {
  const rad = (lat * Math.PI) / 180;
  return Math.floor(((1 - Math.log(Math.tan(rad) + 1 / Math.cos(rad)) / Math.PI) / 2) * 2 ** z);
}

interface Tile {
  z: number;
  x: number;
  y: number;
}

export function cityTiles(): Tile[] {
  const tiles: Tile[] = [];
  for (const z of ZOOMS) {
    const x0 = lon2tile(BBOX.lngMin, z);
    const x1 = lon2tile(BBOX.lngMax, z);
    const y0 = lat2tile(BBOX.latMax, z); // note: y grows southward
    const y1 = lat2tile(BBOX.latMin, z);
    for (let x = x0; x <= x1; x++) for (let y = y0; y <= y1; y++) tiles.push({ z, x, y });
  }
  return tiles;
}

/** Cache key normalised to a single host so any a/b/c subdomain request hits it. */
function keyFor(t: Tile): Request {
  return new Request(`https://tile.openstreetmap.org/${t.z}/${t.x}/${t.y}.png`);
}

export async function downloadCity(onProgress: (done: number, total: number) => void): Promise<void> {
  const cache = await caches.open(CACHE);
  const tiles = cityTiles();
  let done = 0;
  let next = 0;

  async function worker() {
    while (next < tiles.length) {
      const t = tiles[next++];
      try {
        const res = await fetch(`https://a.tile.openstreetmap.org/${t.z}/${t.x}/${t.y}.png`);
        if (res.ok) await cache.put(keyFor(t), res);
      } catch {
        /* skip failed tile */
      }
      done++;
      if (done % 10 === 0 || done === tiles.length) onProgress(done, tiles.length);
    }
  }

  onProgress(0, tiles.length);
  await Promise.all(Array.from({ length: CONCURRENCY }, worker));
}
