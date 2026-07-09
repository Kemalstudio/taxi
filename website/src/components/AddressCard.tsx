import { useState } from "react";
import { Plus, X, MapPin, LocateFixed } from "lucide-react";
import { PLACES } from "../data/places";
import { useI18n } from "../i18n";
import type { AddressField, GeoPoint, StopRow } from "../types";

export type FieldTarget = "from" | "to" | `stop-${number}`;

interface Props {
  from: AddressField;
  to: AddressField;
  stops: StopRow[];
  onText: (target: FieldTarget, text: string) => void;
  onPick: (target: FieldTarget, point: GeoPoint) => void;
  onAddStop: () => void;
  onRemoveStop: (id: number) => void;
  onGps: () => void;
}

function suggestionsFor(text: string): typeof PLACES {
  const q = text.trim().toLowerCase();
  return PLACES.filter((p) => !q || p.name.toLowerCase().includes(q)).slice(0, 6);
}

export function AddressCard({ from, to, stops, onText, onPick, onAddStop, onRemoveStop, onGps }: Props) {
  const { t } = useI18n();
  const [active, setActive] = useState<FieldTarget | null>(null);

  const renderSuggest = (target: FieldTarget, text: string) => {
    if (active !== target) return null;
    const list = suggestionsFor(text);
    if (!list.length) return null;
    return (
      <div className="suggest" style={{ top: "100%" }}>
        {list.map((p) => (
          <div
            className="sug"
            key={p.name}
            onMouseDown={(e) => {
              e.preventDefault();
              onPick(target, { label: p.name, lat: p.lat, lng: p.lng });
              setActive(null);
            }}
          >
            <span className="sic">
              <MapPin size={18} />
            </span>
            <div>
              <div className="sk">{p.name}</div>
              <div className="sv">{p.sub}</div>
            </div>
          </div>
        ))}
      </div>
    );
  };

  const row = (
    target: FieldTarget,
    dotClass: string,
    label: string,
    placeholder: string,
    field: AddressField,
    removeId?: number,
  ) => (
    <div className="addr-row">
      <span className={`dot ${dotClass}`} />
      <div className="fieldwrap">
        <div className="lab">{label}</div>
        <input
          value={field.text}
          placeholder={placeholder}
          autoComplete="off"
          onFocus={() => setActive(target)}
          onBlur={() => setTimeout(() => setActive((a) => (a === target ? null : a)), 120)}
          onChange={(e) => onText(target, e.target.value)}
        />
      </div>
      {removeId !== undefined && (
        <span className="rm" title="Убрать" onClick={() => onRemoveStop(removeId)}>
          <X size={18} />
        </span>
      )}
      {renderSuggest(target, field.text)}
    </div>
  );

  return (
    <div className="card addr-card">
      {row("from", "dot-from", t("addr.from"), t("addr.fromPh"), from)}
      {stops.map((s) => row(`stop-${s.id}`, "dot-stop", t("addr.stop"), t("addr.stopPh"), s.field, s.id))}
      {row("to", "dot-to", t("addr.to"), t("addr.toPh"), to)}
      <button className="gps-btn" onClick={onGps}>
        <span className="ic">
          <LocateFixed size={19} />
        </span>
        {t("addr.gps")}
      </button>
      <button className="add-stop" onClick={onAddStop}>
        <span className="ic">
          <Plus size={20} />
        </span>{" "}
        {t("addr.addStop")}
      </button>
    </div>
  );
}
