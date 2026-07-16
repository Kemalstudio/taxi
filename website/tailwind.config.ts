import type { Config } from "tailwindcss";

export default {
  content: ["./src/admin/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: {
          900: "#0E111A",
          800: "#151925",
          700: "#1B1F2A",
          600: "#232838",
          500: "#2E3446",
        },
        amber: {
          DEFAULT: "#F5A623",
          soft: "#F7B84E",
          muted: "rgba(245, 166, 35, 0.12)",
        },
        mist: {
          100: "#F4F6FB",
          300: "#C7CEDB",
          500: "#8B93A7",
          600: "#6B7385",
        },
        success: "#2ECC71",
        danger: "#FF5A5F",
        info: "#4C8DFF",
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "-apple-system", "Segoe UI", "sans-serif"],
      },
      boxShadow: {
        glass: "0 8px 32px rgba(0, 0, 0, 0.35)",
        glow: "0 0 0 1px rgba(245, 166, 35, 0.25), 0 8px 24px rgba(245, 166, 35, 0.12)",
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
