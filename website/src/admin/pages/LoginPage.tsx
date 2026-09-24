import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth, type PendingTwoFactor } from "../lib/auth";
import { Card } from "../components/ui/primitives";
import { Input, Label } from "@/admin/components/ui/input";
import { Button } from "@/admin/components/ui/button";
import { Alert } from "@/admin/components/ui/alert";

function errorMessage(error: unknown): string {
  return (
    (error as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    (error as Error)?.message ??
    "Login failed"
  );
}

export function LoginPage() {
  const { login, verifyTwoFactor } = useAuth();
  const navigate = useNavigate();
  const [identifier, setIdentifier] = useState("admin@taxi.local");
  const [password, setPassword] = useState("");
  const [code, setCode] = useState("");
  const [pending, setPending] = useState<PendingTwoFactor | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submitCredentials(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      setPending(await login(identifier, password));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  async function submitCode(event: FormEvent) {
    event.preventDefault();
    if (!pending) return;
    setError(null);
    setLoading(true);
    try {
      await verifyTwoFactor(pending.challengeId, code);
      navigate("/", { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid min-h-full grid-cols-1 lg:grid-cols-[55%_45%]">
      <div className="admin-shell hidden flex-col justify-center bg-ink-900 px-16 lg:flex">
        <div className="flex items-center gap-3">
          <div className="grid h-11 w-11 place-items-center rounded-xl bg-accent text-lg font-bold text-ink-900">T</div>
          <div>
            <h1 className="text-lg font-semibold text-mist-100">Taxi Platform</h1>
            <p className="text-sm text-mist-500">Operations Console</p>
          </div>
        </div>
        <p className="mt-10 max-w-sm text-sm leading-relaxed text-mist-500">
          Live dispatch, driver verification, finance and every back-office decision — one console, in real time.
        </p>
      </div>

      <div className="grid place-items-center p-6">
        <Card className="w-full max-w-md animate-fade-up p-8">
          <div className="mb-8 flex items-center gap-3 lg:hidden">
            <div className="grid h-11 w-11 place-items-center rounded-xl bg-accent text-lg font-bold text-ink-900">T</div>
            <div>
              <h1 className="text-lg font-semibold">Taxi Platform</h1>
              <p className="text-sm text-mist-500">Operations Console</p>
            </div>
          </div>

          {!pending ? (
            <form onSubmit={submitCredentials} className="flex flex-col gap-4">
              <div>
                <Label htmlFor="login-identifier">Phone or email</Label>
                <Input id="login-identifier" value={identifier} onChange={(event) => setIdentifier(event.target.value)} autoComplete="username" />
              </div>
              <div>
                <Label htmlFor="login-password">Password</Label>
                <Input id="login-password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" placeholder="••••••••" />
              </div>
              {error && <Alert>{error}</Alert>}
              <Button type="submit" variant="primary" className="mt-2 w-full" disabled={loading}>
                {loading ? "Checking…" : "Continue"}
              </Button>
            </form>
          ) : (
            <form onSubmit={submitCode} className="flex flex-col gap-4">
              <div>
                <h2 className="text-lg font-semibold">Two-factor authentication</h2>
                <p className="mt-1 text-sm text-mist-500">
                  {pending.setupRequired
                    ? "Add this account to your authenticator, then enter the six-digit code."
                    : "Enter the six-digit code from your authenticator app."}
                </p>
              </div>

              {pending.setupRequired && pending.secret && (
                <div className="rounded-xl border border-white/10 bg-white/5 p-4">
                  <p className="text-xs text-mist-500">Manual setup key</p>
                  <p className="mt-1 break-all font-mono text-sm text-mist-100">{pending.secret}</p>
                  {pending.provisioningUri && (
                    <a className="mt-3 inline-block text-xs text-accent hover:underline" href={pending.provisioningUri}>
                      Open in authenticator
                    </a>
                  )}
                </div>
              )}

              <div>
                <Label htmlFor="login-code">Authentication code</Label>
                <Input
                  id="login-code"
                  value={code}
                  onChange={(event) => setCode(event.target.value.replace(/\D/g, "").slice(0, 6))}
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  placeholder="000000"
                  autoFocus
                />
              </div>
              {error && <Alert>{error}</Alert>}
              <Button type="submit" variant="primary" className="mt-2 w-full" disabled={loading || code.length !== 6}>
                {loading ? "Verifying…" : pending.setupRequired ? "Enable 2FA and sign in" : "Sign in"}
              </Button>
              <Button type="button" variant="secondary" className="w-full" onClick={() => { setPending(null); setCode(""); setError(null); }}>
                Back
              </Button>
            </form>
          )}

          <p className="mt-6 text-center text-xs text-mist-600">Default seed: admin@taxi.local · set via ADMIN_PASSWORD</p>
        </Card>
      </div>
    </div>
  );
}
