import { useEffect, useState } from "react";
import { Banknote, Clock, MapPin, Settings, LifeBuoy, LogOut, Star, Gift } from "lucide-react";
import { useI18n } from "../i18n";
import { getMe } from "../lib/api";
import type { Session } from "./LoginModal";

interface Props {
  session: Session;
  onClose: () => void;
  onLogout: () => void;
}

export function ProfileModal({ session, onClose, onLogout }: Props) {
  const { t } = useI18n();
  const initial = session.name.charAt(0).toUpperCase();
  const [points, setPoints] = useState<number | null>(null);
  const [role, setRole] = useState<string | null>(null);

  useEffect(() => {
    if (!session.online) return;
    getMe()
      .then((me) => {
        setPoints(me.loyaltyPoints);
        setRole(me.role);
      })
      .catch(() => {});
  }, [session.online]);

  const rows = [
    { icon: Banknote, title: t("prof.payment"), sub: t("prof.cash") },
    { icon: Clock, title: t("prof.history"), sub: t("prof.trips") },
    { icon: MapPin, title: t("prof.saved"), sub: `${t("saved.home")} · ${t("saved.work")}` },
    { icon: Settings, title: t("prof.settings"), sub: "" },
    { icon: LifeBuoy, title: t("prof.support"), sub: "" },
  ];

  return (
    <div className="overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal profile-modal">
        <div className="prof-hero">
          <button className="prof-close" onClick={onClose} aria-label="Close">
            ×
          </button>
          <div className="prof-av-ring">
            <div className="prof-av">{initial}</div>
          </div>
          <div className="prof-id">
            <div className="prof-name">{session.name}</div>
            <div className="prof-phone">+993 {session.phone}</div>
            <div className="prof-meta">
              <span className="prof-rating">
                <Star size={13} fill="currentColor" /> 5.0
              </span>
              {role && <span className="prof-badge">{t(`role.${role}`)}</span>}
              <span className={`prof-badge${session.online ? " on" : ""}`}>
                {session.online ? t("prof.online") : t("prof.demo")}
              </span>
            </div>
          </div>
        </div>

        {points != null && (
          <div className="prof-points">
            <Gift size={16} />
            <span>{t("prof.points")}</span>
            <strong>{points}</strong>
          </div>
        )}

        <div className="prof-list">
          {rows.map((r) => (
            <button className="prof-row" key={r.title}>
              <span className="prof-ic">
                <r.icon size={19} />
              </span>
              <span className="prof-rt">
                {r.title}
                {r.sub && <small>{r.sub}</small>}
              </span>
              <span className="prof-chev">›</span>
            </button>
          ))}
          <button className="prof-row danger" onClick={onLogout}>
            <span className="prof-ic">
              <LogOut size={19} />
            </span>
            <span className="prof-rt">{t("prof.logout")}</span>
          </button>
        </div>
      </div>
    </div>
  );
}
