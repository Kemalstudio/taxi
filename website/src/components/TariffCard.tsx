import { Car, Sparkles, Crown, Zap } from "lucide-react";
import { useI18n } from "../i18n";
import { TARIFF_MULTIPLIER, priceFor } from "../lib/routing";
import type { RideTariff } from "../types";

const TARIFFS: { id: RideTariff; icon: typeof Car; labelKey: string }[] = [
  { id: "ECONOMY", icon: Car, labelKey: "tariff.economy" },
  { id: "COMFORT", icon: Sparkles, labelKey: "tariff.comfort" },
  { id: "BUSINESS", icon: Crown, labelKey: "tariff.business" },
  { id: "ELECTRO", icon: Zap, labelKey: "tariff.electro" },
];

interface Props {
  value: RideTariff;
  onChange: (t: RideTariff) => void;
  km: number | null;
}

export function TariffCard({ value, onChange, km }: Props) {
  const { t } = useI18n();
  return (
    <div className="card">
      <div className="tariffs">
        {TARIFFS.map(({ id, icon: Icon, labelKey }) => (
          <button
            key={id}
            className={`tariff-opt${value === id ? " on" : ""}`}
            onClick={() => onChange(id)}
          >
            <Icon size={20} />
            <span className="to-label">{t(labelKey)}</span>
            <span className="to-price">
              {km != null ? `${priceFor(km, id)} TMT` : `×${TARIFF_MULTIPLIER[id]}`}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
}
