import type { Place } from "../types";

/** Search suggestions — well-known Aşgabat locations. */
export const PLACES: Place[] = [
  { name: "Bitarap Türkmenistan şaýoly", sub: "Aşgabat", lat: 37.9409, lng: 58.3794 },
  { name: "Aşgabat Halkara Howa Menzili", sub: "Аэропорт · 11 км", lat: 37.9868, lng: 58.361 },
  { name: "Berkarar Söwda Merkezi", sub: "ТЦ · 3.2 км", lat: 37.928, lng: 58.386 },
  { name: "Garaşsyzlyk binasy", sub: "Монумент · 5.1 км", lat: 37.91, lng: 58.38 },
  { name: "Türkmenbaşy şaýoly", sub: "Проспект", lat: 37.952, lng: 58.39 },
  { name: "Parahat 7, jaý 42", sub: "Жилой район", lat: 37.92, lng: 58.42 },
  { name: "Olimpiýa şäherçesi", sub: "Стадион", lat: 37.9, lng: 58.41 },
  { name: "Ertogrul Gazy metjidi", sub: "Мечеть", lat: 37.945, lng: 58.37 },
  { name: "Gülüstan bazary", sub: "Русский базар", lat: 37.95, lng: 58.385 },
  { name: "Türkmenistan döwlet uniwersiteti", sub: "Университет", lat: 37.942, lng: 58.398 },
];

export interface SavedPlace {
  place: string;
  lat: number;
  lng: number;
  key: string;
  short: string;
  icon: "home" | "briefcase" | "plane" | "shopping-bag" | "store" | "dumbbell";
}

export const SAVED_PLACES: SavedPlace[] = [
  { place: "Parahat 7, jaý 42", lat: 37.92, lng: 58.42, key: "Дом", short: "Parahat 7", icon: "home" },
  { place: "Garaşsyzlyk şaýoly 15", lat: 37.91, lng: 58.38, key: "Работа", short: "Garaşsyzlyk 15", icon: "briefcase" },
  { place: "Aşgabat Halkara Howa Menzili", lat: 37.9868, lng: 58.361, key: "Аэропорт", short: "Howa menzili", icon: "plane" },
  { place: "Berkarar Söwda Merkezi", lat: 37.928, lng: 58.386, key: "Berkarar", short: "Söwda merkezi", icon: "shopping-bag" },
  { place: "Gülüstan bazary", lat: 37.95, lng: 58.385, key: "Базар", short: "Gülüstan", icon: "store" },
  { place: "Olimpiýa şäherçesi", lat: 37.9, lng: 58.41, key: "Стадион", short: "Olimpiýa", icon: "dumbbell" },
];

export const DEFAULT_FROM: GeoPointSeed = {
  label: "Bitarap Türkmenistan şaýoly",
  lat: 37.9409,
  lng: 58.3794,
};

interface GeoPointSeed {
  label: string;
  lat: number;
  lng: number;
}
