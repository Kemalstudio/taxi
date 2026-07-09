import { ChevronDown } from "lucide-react";

const MENU = ["Пользователям", "Водителям", "Бизнесу", "Партнёрам"];

export function TopNav() {
  return (
    <nav className="topnav">
      <div className="logo">
        <span className="badge">Go</span> Taksi
      </div>
      <div className="navmenu">
        {MENU.map((m) => (
          <a key={m}>
            {m} <ChevronDown size={15} />
          </a>
        ))}
        <a>Скачать</a>
      </div>
      <div className="nav-right">
        <span className="nav-user">kemalatayew913@…</span>
        <div className="avatar">K</div>
      </div>
    </nav>
  );
}
