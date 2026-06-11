import react from "@vitejs/plugin-react";
import { defineConfig } from "vitest/config";

export default defineConfig(({ mode }) => ({
  base: mode === "github-pages" ? "/futureme-financial-ai/" : "/",
  plugins: [react()],
  server: {
    port: 5173,
  },
  test: {
    environment: "jsdom",
  },
}));
