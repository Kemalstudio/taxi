import { useState } from "react";
import { Star } from "lucide-react";
import { useI18n } from "../i18n";
import { rateRide } from "../lib/api";

interface Props {
  rideId: string;
  onClose: () => void;
}

export function RatingModal({ rideId, onClose }: Props) {
  const { t } = useI18n();
  const [stars, setStars] = useState(5);
  const [comment, setComment] = useState("");
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);

  const submit = async () => {
    setBusy(true);
    try {
      await rateRide(rideId, stars, comment.trim() || undefined);
      setDone(true);
      setTimeout(onClose, 1200);
    } catch {
      onClose();
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal rate-modal">
        {done ? (
          <div className="rate-thanks">{t("rate.thanks")}</div>
        ) : (
          <>
            <h3>{t("rate.title")}</h3>
            <p className="msub">{t("rate.sub")}</p>
            <div className="rate-stars">
              {[1, 2, 3, 4, 5].map((n) => (
                <button key={n} className={n <= stars ? "on" : ""} onClick={() => setStars(n)}>
                  <Star size={30} fill={n <= stars ? "currentColor" : "none"} />
                </button>
              ))}
            </div>
            <textarea
              className="rate-comment"
              placeholder={t("rate.placeholder")}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
            />
            <button className="close" onClick={submit} disabled={busy}>
              {t("rate.submit")}
            </button>
            <button className="rate-skip-btn" onClick={onClose}>
              {t("rate.skip")}
            </button>
          </>
        )}
      </div>
    </div>
  );
}
