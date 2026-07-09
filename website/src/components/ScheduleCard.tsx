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
  return (
    <div className="card">
      <div className="seg">
        <button className={p.mode === "now" ? "on" : ""} onClick={() => p.onMode("now")}>
          Сейчас
        </button>
        <button className={p.mode === "later" ? "on" : ""} onClick={() => p.onMode("later")}>
          Ко времени
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
          <div className="fl-k">Стоимость · наличные</div>
          <div className="fl-v">{p.fareText}</div>
        </div>
        <div className="fare-right">
          <div dangerouslySetInnerHTML={{ __html: p.distText }} />
          <div dangerouslySetInnerHTML={{ __html: p.timeText }} />
        </div>
      </div>

      <div style={{ padding: "0 10px 12px" }}>
        <button className="order-btn" disabled={!p.canOrder} onClick={p.onOrder}>
          {p.canOrder ? p.orderLabel : "Укажите точки A и B"}
        </button>
      </div>
      <div className="hint">{p.hint}</div>
    </div>
  );
}
