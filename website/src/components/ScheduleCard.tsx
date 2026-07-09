import { useI18n } from "../i18n";
import type { RideMode } from "../types";

interface Props {
  mode: RideMode;
  onMode: (m: RideMode) => void;
  date: string;
  time: string;
  onDate: (v: string) => void;
  onTime: (v: string) => void;
  fareText: string;
  distText: string;
  timeText: string;
  canOrder: boolean;
  orderLabel: string;
  hint: string;
  onOrder: () => void;
}

export function ScheduleCard(p: Props) {
  const { t } = useI18n();
  return (
    <div className="card">
      <div className="seg">
        <button className={p.mode === "now" ? "on" : ""} onClick={() => p.onMode("now")}>
          {t("sch.now")}
        </button>
        <button className={p.mode === "later" ? "on" : ""} onClick={() => p.onMode("later")}>
          {t("sch.later")}
        </button>
      </div>

      {p.mode === "later" && (
        <div className="when">
          <input type="date" value={p.date} onChange={(e) => p.onDate(e.target.value)} />
          <input type="time" value={p.time} onChange={(e) => p.onTime(e.target.value)} />
        </div>
      )}

      <div className="fare-card">
        <div className="fare-left">
          <div className="fl-k">{t("sch.fare")}</div>
          <div className="fl-v">{p.fareText}</div>
        </div>
        <div className="fare-right">
          <div dangerouslySetInnerHTML={{ __html: p.distText }} />
          <div dangerouslySetInnerHTML={{ __html: p.timeText }} />
        </div>
      </div>

      <div style={{ padding: "0 10px 12px" }}>
        <button className="order-btn" disabled={!p.canOrder} onClick={p.onOrder}>
          {p.canOrder ? p.orderLabel : t("sch.needAB")}
        </button>
      </div>
      <div className="hint">{p.hint}</div>
    </div>
  );
}
