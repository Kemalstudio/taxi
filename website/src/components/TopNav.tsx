import { useState } from "react";
import { ChevronDown, Moon, Sun, Globe } from "lucide-react";
import { useTheme } from "../theme";
import { useI18n, LANGS } from "../i18n";
import type { Session } from "./LoginModal";

const PHONE = "+993 (64) 00-53-74";
const TEL = `tel:${PHONE.replace(/[^\d+]/g, "")}`;

interface NavItem {
  labelKey: string;
  items: { label: string; href: string }[];
}

function NavDropdown({ item, t }: { item: NavItem; t: (key: string) => string }) {
  return (
    <div className="nav-drop">
      <a tabIndex={0} role="button">
        {t(item.labelKey)} <ChevronDown size={15} />
      </a>
      <div className="nav-drop-menu">
        <div className="nav-drop-menu-inner">
          {item.items.map((it) => (
            <a key={it.label} href={it.href}>
              {it.label}
            </a>
          ))}
        </div>
      </div>
    </div>
  );
}

interface Props {
  session: Session | null;
  onSignIn: () => void;
  onProfile: () => void;
}

export function TopNav({ session, onSignIn, onProfile }: Props) {
  const { theme, toggle } = useTheme();
  const { lang, setLang, t } = useI18n();
  const [langOpen, setLangOpen] = useState(false);
  const current = LANGS.find((l) => l.code === lang)!;

  const NAV_ITEMS: NavItem[] = [
    {
      labelKey: "nav.users",
      items: [
        { label: t("footer.tariffs"), href: "/" },
        { label: t("nav.download"), href: "/" },
      ],
    },
    {
      labelKey: "nav.drivers",
      items: [
        { label: t("mkt.drivers.title"), href: "/drivers" },
        { label: t("mkt.drivers.cta"), href: TEL },
      ],
    },
    {
      labelKey: "nav.business",
      items: [
        { label: t("mkt.business.title"), href: "/business" },
        { label: t("mkt.business.cta"), href: TEL },
      ],
    },
    {
      labelKey: "nav.partners",
      items: [
        { label: t("mkt.partners.title"), href: "/partners" },
        { label: t("mkt.partners.cta"), href: TEL },
      ],
    },
  ];

  return (
    <nav className="topnav">
      <div className="logo">
        <span className="badge">Go</span> Taksi
      </div>
      <div className="navmenu">
        {NAV_ITEMS.map((item) => (
          <NavDropdown key={item.labelKey} item={item} t={t} />
        ))}
      </div>

      <div className="nav-right">
        <div className="nav-tools">
          <button className="theme-btn" onClick={toggle} aria-label="Theme">
            {theme === "dark" ? <Sun size={18} /> : <Moon size={18} />}
          </button>

          <div className="lang-wrap">
            <button className="lang-btn" onClick={() => setLangOpen((o) => !o)} onBlur={() => setTimeout(() => setLangOpen(false), 150)}>
              <Globe size={16} /> {current.flag} {current.code.toUpperCase()}
            </button>
            {langOpen && (
              <div className="lang-menu">
                {LANGS.map((l) => (
                  <button
                    key={l.code}
                    className={`lang-item${l.code === lang ? " on" : ""}`}
                    onMouseDown={() => {
                      setLang(l.code);
                      setLangOpen(false);
                    }}
                  >
                    {l.flag} {l.label}
                  </button>
                ))}
              </div>
            )}
          </div>

          {session ? (
            <button className="avatar" title={session.name} onClick={onProfile}>
              {session.name.charAt(0).toUpperCase()}
            </button>
          ) : (
            <button className="sign-btn" onClick={onSignIn}>
              {t("nav.signin")}
            </button>
          )}
        </div>
      </div>
    </nav>
  );
}
