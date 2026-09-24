import { useState } from "react";
import { User, Lock, AtSign, MessageSquare } from "lucide-react";
import { useI18n } from "../i18n";
import { register, login, requestOtp, verifyOtp, userId as apiUserId, ApiError, NetworkError } from "../lib/api";

export interface Session {
  name: string;
  phone: string;
  /** false when the backend was unreachable and we signed in locally (demo). */
  online: boolean;
  /** Authenticated user's backend id — null in demo mode. */
  userId: string | null;
}

const ADMIN_ROLES = new Set(["ADMIN", "SUPER_ADMIN"]);

export function LoginModal({
  onClose,
  onAuth,
}: {
  onClose: () => void;
  onAuth: (s: Session) => void;
}) {
  const { t } = useI18n();
  const [tab, setTab] = useState<"login" | "register" | "otp">("login");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const [otpPhone, setOtpPhone] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [otpStep, setOtpStep] = useState<"phone" | "code">("phone");
  const [otpErr, setOtpErr] = useState<string | null>(null);
  const [otpBusy, setOtpBusy] = useState(false);

  const errorMessage = (e: unknown): string => {
    if (e instanceof ApiError) return e.message;
    if (e instanceof NetworkError) return t("m.demo");
    return String(e);
  };

  const sendOtp = async () => {
    if (!otpPhone.trim()) {
      setOtpErr(t("auth.needPhone"));
      return;
    }
    setOtpErr(null);
    setOtpBusy(true);
    try {
      await requestOtp(otpPhone.trim());
      setOtpStep("code");
    } catch (e) {
      setOtpErr(errorMessage(e));
    } finally {
      setOtpBusy(false);
    }
  };

  const confirmOtp = async () => {
    if (!otpCode.trim()) return;
    setOtpErr(null);
    setOtpBusy(true);
    try {
      const data = await verifyOtp(otpPhone.trim(), otpCode.trim());
      if (ADMIN_ROLES.has(data.role)) {
        window.location.href = "/admin";
        return;
      }
      onAuth({ name: "Ýolagçy", phone: otpPhone.trim(), online: true, userId: apiUserId.get() });
    } catch (e) {
      setOtpErr(errorMessage(e));
    } finally {
      setOtpBusy(false);
    }
  };

  const submit = async () => {
    if (tab === "register") {
      if (!phone.trim()) {
        setErr(t("auth.needPhone"));
        return;
      }
      setErr(null);
      setBusy(true);
      const nm = name.trim() || "Ýolagçy";
      try {
        await register(phone.trim(), password, nm);
        onAuth({ name: nm, phone: phone.trim(), online: true, userId: apiUserId.get() });
      } catch (e) {
        if (e instanceof NetworkError) {
          // Backend not running here — continue in demo mode so the site stays usable.
          onAuth({ name: nm, phone: phone.trim(), online: false, userId: null });
        } else if (e instanceof ApiError) {
          setErr(e.message);
        } else {
          setErr(String(e));
        }
      } finally {
        setBusy(false);
      }
      return;
    }

    if (!identifier.trim()) {
      setErr(t("auth.needIdentifier"));
      return;
    }
    setErr(null);
    setBusy(true);
    const nm = "Ýolagçy";
    try {
      const data = await login(identifier.trim(), password);
      if (ADMIN_ROLES.has(data.role)) {
        window.location.href = "/admin";
        return;
      }
      onAuth({ name: nm, phone: identifier.trim(), online: true, userId: apiUserId.get() });
    } catch (e) {
      if (e instanceof NetworkError) {
        // Backend not running here — continue in demo mode so the site stays usable.
        onAuth({ name: nm, phone: identifier.trim(), online: false, userId: null });
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
          <button className={tab === "otp" ? "on" : ""} onClick={() => setTab("otp")}>
            {t("auth.otpTab")}
          </button>
        </div>

        {tab === "otp" ? (
          <>
            <div className="auth-field">
              <label>{t("auth.phone")}</label>
              <div className="auth-input">
                <span className="cc">🇹🇲 +993</span>
                <input
                  value={otpPhone}
                  inputMode="tel"
                  placeholder="65 12 34 56"
                  disabled={otpStep === "code"}
                  onChange={(e) => setOtpPhone(e.target.value)}
                />
              </div>
            </div>

            {otpStep === "code" && (
              <div className="auth-field">
                <label>{t("auth.otpCode")}</label>
                <div className="auth-input">
                  <span className="ic">
                    <MessageSquare size={18} />
                  </span>
                  <input
                    value={otpCode}
                    inputMode="numeric"
                    placeholder="123456"
                    onChange={(e) => setOtpCode(e.target.value)}
                  />
                </div>
              </div>
            )}

            {otpErr && <div className="auth-err">{otpErr}</div>}

            <button
              className="auth-submit"
              onClick={otpStep === "phone" ? sendOtp : confirmOtp}
              disabled={otpBusy}
            >
              {otpBusy ? "…" : otpStep === "phone" ? t("auth.otpSend") : t("auth.otpConfirm")}
            </button>
          </>
        ) : (
          <>
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

            {tab === "register" ? (
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
            ) : (
              <div className="auth-field">
                <label>{t("auth.identifier")}</label>
                <div className="auth-input">
                  <span className="ic">
                    <AtSign size={18} />
                  </span>
                  <input
                    value={identifier}
                    placeholder={t("auth.identifierPh")}
                    onChange={(e) => setIdentifier(e.target.value)}
                  />
                </div>
              </div>
            )}

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
          </>
        )}
      </div>
    </div>
  );
}
