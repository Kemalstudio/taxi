import { Check } from "lucide-react";

export interface OrderSummary {
  title: string;
  subtitle: string;
  rows: [string, string][];
}

export function OrderModal({ summary, onClose }: { summary: OrderSummary; onClose: () => void }) {
  return (
    <div className="overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="ok-ic">
          <Check color="#fff" size={32} />
        </div>
        <h3>{summary.title}</h3>
        <p className="msub">{summary.subtitle}</p>
        <div className="summary">
          {summary.rows.map(([k, v]) => (
            <div className="sr" key={k}>
              <span className="k">{k}</span>
              <span className="v">{v}</span>
            </div>
          ))}
        </div>
        <button className="close" onClick={onClose}>
          Готово
        </button>
      </div>
    </div>
  );
}
