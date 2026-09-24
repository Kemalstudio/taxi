import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { getPlatformSettings } from "./lib/api";

type Theme = "light" | "dark";
const STORAGE_KEY = "taksi_theme";

/** Settings keys of the form `color.<name>` map onto the `--<name>` CSS custom property
 * defined in index.css's `:root` block — admin-edited via DesignSettingsPage. */
const COLOR_SETTING_PREFIX = "color.";

interface ThemeCtx {
  theme: Theme;
  toggle: () => void;
}

const Ctx = createContext<ThemeCtx | undefined>(undefined);

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<Theme>(() => (localStorage.getItem(STORAGE_KEY) as Theme) || "light");

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem(STORAGE_KEY, theme);
  }, [theme]);

  useEffect(() => {
    getPlatformSettings()
      .then((settings) => {
        Object.entries(settings).forEach(([key, value]) => {
          if (key.startsWith(COLOR_SETTING_PREFIX) && value) {
            document.documentElement.style.setProperty(`--${key.slice(COLOR_SETTING_PREFIX.length)}`, value);
          }
        });
      })
      .catch(() => {
        /* best-effort — the site still works with the built-in default colors */
      });
  }, []);

  const value = useMemo<ThemeCtx>(
    () => ({ theme, toggle: () => setTheme((t) => (t === "dark" ? "light" : "dark")) }),
    [theme],
  );
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useTheme(): ThemeCtx {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("useTheme must be used within ThemeProvider");
  return ctx;
}
