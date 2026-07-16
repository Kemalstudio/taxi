import { useState } from "react";
import { Tag, X } from "lucide-react";
import { useI18n } from "../i18n";
import { validatePromo, type PromoPreview } from "../lib/api";

interface Props {
  fare: number | null;
  applied: PromoPreview | null;
  onApplied: (preview: PromoPreview | null) => void;
}

export function PromoField({ fare, applied, onApplied }: Props) {
  const { t } = useI18n();
  const [code, setCode] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const apply = async () => {
    if (!code.trim() || fare == null) return;
    setBusy(true);
    setError(null);
    try {
      const preview = await validatePromo(code.trim(), fare);
      onApplied(preview);
    } catch {
      onApplied(null);
      setError(t("promo.invalid"));
    } finally {
      setBusy(false);
    }
  };

  const clear = () => {
    onApplied(null);
    setCode("");
    setError(null);
  };

  return (
    <div className="card">
      <div className="promo-field">
        <span className="ic">
          <Tag size={18} />
        </span>
        {applied ? (
          <span className="promo-applied">
            {applied.code} · −{applied.discountAmount} TMT
          </span>
        ) : (
          <input
            placeholder={t("promo.codePh")}
            value={code}
            onChange={(e) => {
              setCode(e.target.value);
              setError(null);
            }}
          />
        )}
        {applied ? (
          <button className="promo-clear-btn" onClick={clear}>
            <X size={16} />
          </button>
        ) : (
          <button className="promo-apply-btn" onClick={apply} disabled={busy || !code.trim() || fare == null}>
            {t("promo.apply")}
          </button>
        )}
      </div>
      {error && <div className="promo-error">{error}</div>}
    </div>
  );
}
