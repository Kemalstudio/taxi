import { MessageSquare, UserPlus, ChevronRight, MessageCircle, Accessibility } from "lucide-react";
import { useI18n } from "../i18n";

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
  const { t } = useI18n();
  return (
    <div className="card">
      <div className="opt">
        <div className="comment">
          <span className="ic">
            <MessageSquare size={20} />
          </span>
          <input
            placeholder={t("opt.comment")}
            value={value.comment}
            onChange={(e) => onChange({ comment: e.target.value })}
          />
        </div>
      </div>

      <button className="row-toggle" onClick={() => onChange({ otherOpen: !value.otherOpen })}>
        <span className="rt-ic">
          <UserPlus size={20} />
        </span>
        <span className="rt-t">{t("opt.other")}</span>
        <span className="chev">
          <ChevronRight size={18} />
        </span>
      </button>
      {value.otherOpen && (
        <div className="other-fields">
          <div className="mini-inp">
            <input
              placeholder={t("opt.name")}
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
        <span className="rt-t">{t("opt.textOnly")}</span>
        <span className={`switch${value.textOnly ? " on" : ""}`} />
      </button>
      <button className="row-toggle" onClick={() => onChange({ wheelchair: !value.wheelchair })}>
        <span className="rt-ic">
          <Accessibility size={20} />
        </span>
        <span className="rt-t">{t("opt.wheelchair")}</span>
        <span className={`switch${value.wheelchair ? " on" : ""}`} />
      </button>
    </div>
  );
}
