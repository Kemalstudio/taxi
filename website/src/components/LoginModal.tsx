import { useState } from "react";
import { User, Lock } from "lucide-react";
import { useI18n } from "../i18n";
import { register, login, ApiError, NetworkError } from "../lib/api";

export interface Session {
  name: string;
  phone: string;
  /** false when the backend was unreachable and we signed in locally (demo). */
  online: boolean;
}

export function LoginModal({
  onClose,
  onAuth,
}: {
  onClose: () => void;
  onAuth: (s: Session) => void;
}) {
  const { t } = useI18n();
  const [tab, setTab] = useState<"login" | "register">("login");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    if (!phone.trim()) {
      setErr(t("auth.needPhone"));
      return;
    }
    setErr(null);
    setBusy(true);
    const nm = name.trim() || "Ýolagçy";
    try {
      if (tab === "register") await register(phone.trim(), password, nm);
      else await login(phone.trim(), password);
      onAuth({ name: nm, phone: phone.trim(), online: true });
    } catch (e) {
      if (e instanceof NetworkError) {
        // Backend not running here — continue in demo mode so the site stays usable.
        onAuth({ name: nm, phone: phone.trim(), online: false });
      } else if (e instanceof ApiError) {
        setErr(e.message);
      } else {
        setErr(String(e));
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="auth-tabs">
          <button className={tab === "login" ? "on" : ""} onClick={() => setTab("login")}>
            {t("auth.login")}
          </button>
          <button className={tab === "register" ? "on" : ""} onClick={() => setTab("register")}>
            {t("auth.register")}
          </button>
        </div>

        {tab === "register" && (
          <div className="auth-field">
            <label>{t("auth.name")}</label>
            <div className="auth-input">
              <span className="ic">
                <User size={18} />
              </span>
              <input value={name} placeholder={t("auth.namePh")} onChange={(e) => setName(e.target.value)} />
            </div>
          </div>
        )}

        <div className="auth-field">
          <label>{t("auth.phone")}</label>
          <div className="auth-input">
            <span className="cc">🇹🇲 +993</span>
            <input
              value={phone}
              inputMode="tel"
              placeholder="65 12 34 56"
              onChange={(e) => setPhone(e.target.value)}
            />
          </div>
        </div>

        <div className="auth-field">
          <label>{t("auth.password")}</label>
          <div className="auth-input">
            <span className="ic">
              <Lock size={18} />
            </span>
            <input
              type="password"
              value={password}
              placeholder="••••••••"
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
        </div>

        {err && <div className="auth-err">{err}</div>}

        <button className="auth-submit" onClick={submit} disabled={busy}>
          {busy ? "…" : tab === "login" ? t("auth.loginBtn") : t("auth.registerBtn")}
        </button>
      </div>
    </div>
  );
}
