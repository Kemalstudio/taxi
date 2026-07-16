import { Phone, MessageCircle, ShieldAlert, X } from "lucide-react";
import { useI18n } from "../i18n";
import type { RideDetails } from "../types";

const CANCELLABLE_STATUSES = ["SCHEDULED", "REQUESTED", "SEARCHING", "ACCEPTED", "DRIVER_ARRIVED"];

interface Props {
  ride: RideDetails;
  onChat: () => void;
  onSos: () => void;
  onCancel: () => void;
  unreadChat: boolean;
}

export function ActiveRideCard({ ride, onChat, onSos, onCancel, unreadChat }: Props) {
  const { t } = useI18n();
  const driver = ride.driver;

  return (
    <div className="active-ride-card">
      <div className="arc-status">{t(`ride.status.${ride.status}`)}</div>

      {driver && (
        <div className="arc-driver">
          <div className="arc-driver-av">{driver.fullName.charAt(0).toUpperCase()}</div>
          <div className="arc-driver-info">
            <div className="arc-driver-name">{driver.fullName}</div>
            <div className="arc-driver-car">
              {[driver.vehicleMake, driver.vehicleModel].filter(Boolean).join(" ") || "—"}
              {driver.plateNumber ? ` · ${driver.plateNumber}` : ""}
            </div>
          </div>
          <div className="arc-driver-rating">★ {driver.rating}</div>
        </div>
      )}

      <div className="arc-actions">
        {driver?.phone && (
          <a className="arc-btn" href={`tel:${driver.phone}`} title={t("chat.call")}>
            <Phone size={18} />
          </a>
        )}
        {driver && (
          <button className="arc-btn" onClick={onChat} title={t("chat.title")}>
            <MessageCircle size={18} />
            {unreadChat && <span className="arc-dot" />}
          </button>
        )}
        <button className="arc-btn sos" onClick={onSos} title={t("sos.button")}>
          <ShieldAlert size={18} />
        </button>
        {CANCELLABLE_STATUSES.includes(ride.status) && (
          <button className="arc-btn cancel" onClick={onCancel} title={t("ride.cancel")}>
            <X size={18} />
          </button>
        )}
      </div>
    </div>
  );
}
