import { CarFront } from "lucide-react";
import { useI18n } from "../i18n";

export function PromoCards() {
  const { t } = useI18n();
  return (
    <>
      <div className="promo-phone">
        <div>
          <div className="pp-num">+993 (12) 46-70-74</div>
          <div className="pp-sub">{t("promo.phone")}</div>
        </div>
        <div className="pp-car">🚕</div>
      </div>
      <div className="promo-driver">
        <div>
          <div className="pd-t">{t("promo.becomeDriver")}</div>
          <div className="pd-s">{t("promo.partners")}</div>
        </div>
        <div className="pd-ph">
          <CarFront size={22} />
        </div>
      </div>
    </>
  );
}

export function Footer() {
  const { t } = useI18n();
  return (
    <div className="footer">
      <span>🌐 {t("footer.lang")}</span>
      <a>{t("footer.tariffs")}</a>
      <a>{t("nav.partners")}</a>
      <a>{t("footer.agreement")}</a>
      <a>{t("footer.legal")}</a>
      <span style={{ marginLeft: "auto" }}>© 2011–2026 Taksi Go</span>
    </div>
  );
}
