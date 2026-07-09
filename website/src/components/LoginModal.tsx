import { useState } from "react";
import { User, Lock } from "lucide-react";
import { useI18n } from "../i18n";

export interface Session {
  name: string;
  phone: string;
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

  const submit = () => {
    if (!phone.trim()) {
      setErr(t("auth.needPhone"));
      return;
    }
    // Client-side session for now (backend auth is a later phase).
    onAuth({ name: name.trim() || "Ýolagçy", phone: phone.trim() });
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

        <button className="auth-submit" onClick={submit}>
          {tab === "login" ? t("auth.loginBtn") : t("auth.registerBtn")}
        </button>
      </div>
    </div>
  );
}
