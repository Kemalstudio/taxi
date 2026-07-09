import { CarFront } from "lucide-react";

export function PromoCards() {
  return (
    <>
      <div className="promo-phone">
        <div>
          <div className="pp-num">+993 (12) 46-70-74</div>
          <div className="pp-sub">Заказ такси по телефону</div>
        </div>
        <div className="pp-car">🚕</div>
      </div>
      <div className="promo-driver">
        <div>
          <div className="pd-t">Стать водителем ›</div>
          <div className="pd-s">с партнёрами Taksi Go</div>
        </div>
        <div className="pd-ph">
          <CarFront size={22} />
        </div>
      </div>
    </>
  );
}

export function Footer() {
  return (
    <div className="footer">
      <span>🌐 Язык · Русский</span>
      <a>Тарифы</a>
      <a>Партнёрам</a>
      <a>Пользовательское соглашение</a>
      <a>Правовая информация</a>
      <span style={{ marginLeft: "auto" }}>© 2011–2026 Taksi Go — информационный сервис.</span>
    </div>
  );
}
