import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  resolve: {
    // Root-relative string alias — avoids needing @types/node for path.resolve/__dirname.
    alias: { "@": "/src" },
  },
  server: { port: 5174 },
});
