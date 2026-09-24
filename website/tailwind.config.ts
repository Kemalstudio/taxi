import type { Config } from "tailwindcss";

export default {
  content: ["./src/admin/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        // Deep graphite-navy instrument-panel scale — a control-room black, not a warm "SaaS ink".
        ink: {
          900: "#0A0E13",
          800: "#10151C",
          700: "#161C25",
          600: "#1D242F",
          500: "#29323F",
        },
        mist: {
          100: "#EDF1F4",
          300: "#B8C2CC",
          500: "#7C8894",
          600: "#5C6672",
        },
        // Brand / "live signal" accent — a GPS-blip teal, deliberately not the generic
        // amber-glow-on-black look. `amber` stays reserved purely for the warning semantic below.
        accent: {
          DEFAULT: "#35D0BE",
          soft: "#5FE0D2",
          muted: "rgba(53, 208, 190, 0.14)",
        },
        amber: {
          DEFAULT: "#E8A23D",
          soft: "#F0B767",
          muted: "rgba(232, 162, 61, 0.14)",
        },
        success: "#3DCB78",
        danger: "#F0555F",
        info: "#5B8CF0",
        violet: "#9370F5",
        chart: {
          1: "#35D0BE",
          2: "#5B8CF0",
          3: "#3DCB78",
          4: "#F0555F",
          5: "#7C8894",
          6: "#E8A23D",
        },
      },
      fontFamily: {
        sans: ["IBM Plex Sans", "system-ui", "-apple-system", "Segoe UI", "sans-serif"],
        mono: ["IBM Plex Mono", "SFMono-Regular", "Menlo", "Consolas", "monospace"],
      },
      boxShadow: {
        panel: "0 1px 0 rgba(255, 255, 255, 0.04) inset, 0 12px 24px -12px rgba(0, 0, 0, 0.5)",
        glow: "0 0 0 1px rgba(53, 208, 190, 0.25), 0 8px 24px rgba(53, 208, 190, 0.12)",
      },
      backdropBlur: {
        xs: "2px",
      },
      borderRadius: {
        xl2: "1.25rem",
      },
      keyframes: {
        "fade-up": {
          "0%": { opacity: "0", transform: "translateY(8px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
        pulseSoft: {
          "0%, 100%": { opacity: "1" },
          "50%": { opacity: "0.45" },
        },
      },
      animation: {
        "fade-up": "fade-up 0.4s ease-out both",
        "pulse-soft": "pulseSoft 2s ease-in-out infinite",
      },
    },
  },
  plugins: [],
} satisfies Config;
