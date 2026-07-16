import { useState } from "react";
import { ChevronDown, Moon, Sun, Globe } from "lucide-react";
import { useTheme } from "../theme";
import { useI18n, LANGS } from "../i18n";
import type { Session } from "./LoginModal";

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

  return (
    <nav className="topnav">
      <div className="logo">
        <span className="badge">Go</span> Taksi
      </div>
      <div className="navmenu">
        <a>
          {t("nav.users")} <ChevronDown size={15} />
        </a>
        <a>
          {t("nav.drivers")} <ChevronDown size={15} />
        </a>
        <a>
          {t("nav.business")} <ChevronDown size={15} />
        </a>
        <a>
          {t("nav.partners")} <ChevronDown size={15} />
        </a>
        <a>{t("nav.download")}</a>
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
