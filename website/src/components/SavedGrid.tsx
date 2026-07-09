import { Home, Briefcase, Plane, ShoppingBag, Store, Dumbbell } from "lucide-react";
import { SAVED_PLACES, type SavedPlace } from "../data/places";
import type { GeoPoint } from "../types";

const ICONS = {
  home: Home,
  briefcase: Briefcase,
  plane: Plane,
  "shopping-bag": ShoppingBag,
  store: Store,
  dumbbell: Dumbbell,
} as const;

export function SavedGrid({ onPick }: { onPick: (p: GeoPoint) => void }) {
  return (
    <div className="card">
      <div className="saved-grid">
        {SAVED_PLACES.map((s: SavedPlace) => {
          const Icon = ICONS[s.icon];
          return (
            <button
              className="saved-tile"
              key={s.key}
              onClick={() => onPick({ label: s.place, lat: s.lat, lng: s.lng })}
            >
              <span className="sti">
                <Icon size={18} />
              </span>
              <div>
                <div className="stk">{s.key}</div>
                <div className="stv">{s.short}</div>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
