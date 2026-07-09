import { Home, Briefcase, Plane, ShoppingBag, Store, Dumbbell } from "lucide-react";
import { SAVED_PLACES, type SavedPlace } from "../data/places";
import { useI18n } from "../i18n";
import type { GeoPoint } from "../types";

const ICONS = {
  home: Home,
  briefcase: Briefcase,
  plane: Plane,
  "shopping-bag": ShoppingBag,
  store: Store,
  dumbbell: Dumbbell,
} as const;

const LABEL_KEY: Record<SavedPlace["icon"], string> = {
  home: "saved.home",
  briefcase: "saved.work",
  plane: "saved.airport",
  "shopping-bag": "saved.mall",
  store: "saved.market",
  dumbbell: "saved.stadium",
};

export function SavedGrid({ onPick }: { onPick: (p: GeoPoint) => void }) {
  const { t } = useI18n();
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
                <div className="stk">{t(LABEL_KEY[s.icon])}</div>
                <div className="stv">{s.short}</div>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
