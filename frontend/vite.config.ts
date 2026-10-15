import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import svgr from "vite-plugin-svgr";
import tsconfigPaths from "vite-tsconfig-paths"; // Importa el plugin

export default defineConfig({
  plugins: [
    react(),
    tsconfigPaths(),
    tailwindcss(),
    svgr({
      svgrOptions: {
        icon: true,
      },
    }),
  ],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id: string) {
          if (id.includes("node_modules")) {
            if (id.includes("@tanstack/react-query")) return "query";
            if (id.includes("sonner") || id.includes("cmdk")) return "ui";
            if (id.includes("radix-ui")) return "ui";
            if (id.includes("lucide-react")) return "icons";
            if (id.includes("react-hook-form") || id.includes("@hookform/resolvers")) return "forms";
            if (id.includes("zod")) return "forms";
            if (id.includes("axios") || id.includes("date-fns")) return "lib";
            if (id.includes("next-themes") || id.includes("react-helmet-async") || id.includes("@react-oauth/google")) return "providers";
            if (id.includes("recharts")) return "charts";
            if (id.includes("motion")) return "animations";
            if (id.includes("react") || id.includes("react-dom") || id.includes("react-router")) return "vendor";
          }
        },
      },
    },
  },
});
