import { useEffect } from "react";
import { PhoneCall, Share2, X } from "lucide-react";
import { useI18n } from "../i18n";
import { triggerSos } from "../lib/api";
import type { GeoPoint } from "../types";

interface Props {
  rideId: string;
  point: GeoPoint | null;
  from: GeoPoint | null;
  to: GeoPoint | null;
  onClose: () => void;
}

export function SosSheet({ rideId, point, from, to, onClose }: Props) {
  const { t } = useI18n();

  useEffect(() => {
    if (point) triggerSos(rideId, point).catch(() => {});
    // Fire once when the sheet opens — that's the SOS signal itself.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const trackUrl = () => {
    const params = new URLSearchParams();
    if (from) {
      params.set("pLat", String(from.lat));
      params.set("pLng", String(from.lng));
    }
    if (to) {
      params.set("dLat", String(to.lat));
      params.set("dLng", String(to.lng));
    }
    const query = params.toString();
    return `${location.origin}/track/${rideId}${query ? `?${query}` : ""}`;
  };

  const shareTrip = async () => {
    const url = trackUrl();
    const text = t("sos.shareText");
    if (navigator.share) {
      try {
        await navigator.share({ title: text, text, url });
        return;
      } catch {
        /* user cancelled or unsupported — fall through to clipboard */
      }
    }
    try {
      await navigator.clipboard.writeText(`${text}: ${url}`);
    } catch {
      /* best-effort */
    }
  };

  return (
    <div className="overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal sos-modal">
        <div className="chat-head">
          <span>{t("sos.title")}</span>
          <button className="chat-close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>
        <p className="msub">{t("sos.sub")}</p>
        <div className="sos-sent-badge">{t("sos.sent")}</div>
        <a className="sos-action" href="tel:112">
          <PhoneCall size={18} /> {t("sos.call112")}
        </a>
        <button className="sos-action" onClick={shareTrip}>
          <Share2 size={18} /> {t("sos.share")}
        </button>
      </div>
    </div>
  );
}
