import { MessageSquare, UserPlus, ChevronRight, MessageCircle, Accessibility } from "lucide-react";

export interface OptionsState {
  comment: string;
  otherOpen: boolean;
  otherName: string;
  otherPhone: string;
  textOnly: boolean;
  wheelchair: boolean;
}

interface Props {
  value: OptionsState;
  onChange: (patch: Partial<OptionsState>) => void;
}

export function OptionsCard({ value, onChange }: Props) {
  return (
    <div className="card">
      <div className="opt">
        <div className="comment">
          <span className="ic">
            <MessageSquare size={20} />
          </span>
          <input
            placeholder="Комментарий водителю…"
            value={value.comment}
            onChange={(e) => onChange({ comment: e.target.value })}
          />
        </div>
      </div>

      <button className="row-toggle" onClick={() => onChange({ otherOpen: !value.otherOpen })}>
        <span className="rt-ic">
          <UserPlus size={20} />
        </span>
        <span className="rt-t">Заказ другому человеку</span>
        <span className="chev">
          <ChevronRight size={18} />
        </span>
      </button>
      {value.otherOpen && (
        <div className="other-fields">
          <div className="mini-inp">
            <input
              placeholder="Имя пассажира"
              value={value.otherName}
              onChange={(e) => onChange({ otherName: e.target.value })}
            />
          </div>
          <div className="mini-inp">
            <span className="cc">🇹🇲 +993</span>
            <input
              placeholder="65 12 34 56"
              inputMode="tel"
              value={value.otherPhone}
              onChange={(e) => onChange({ otherPhone: e.target.value })}
            />
          </div>
        </div>
      )}

      <button className="row-toggle" onClick={() => onChange({ textOnly: !value.textOnly })}>
        <span className="rt-ic">
          <MessageCircle size={20} />
        </span>
        <span className="rt-t">Общаюсь только текстом</span>
        <span className={`switch${value.textOnly ? " on" : ""}`} />
      </button>
      <button className="row-toggle" onClick={() => onChange({ wheelchair: !value.wheelchair })}>
        <span className="rt-ic">
          <Accessibility size={20} />
        </span>
        <span className="rt-t">Буду с инвалидным креслом</span>
        <span className={`switch${value.wheelchair ? " on" : ""}`} />
      </button>
    </div>
  );
}
