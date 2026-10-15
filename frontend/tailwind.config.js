module.exports = {
  content: ["./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
        heading: ["Playfair Display", "serif"],
      },
      colors: {
        tienda: {
          bg: "#0e111a",
          surface: "#171b26",
          border: "#262b38",
          accent: "#c81e3a",
          success: "#7bd88f",
          warning: "#e8b13a",
          danger: "#e0616a",
          text: "#f2f2f0",
          textSec: "#8b8f9c",
          muted: "#63677a",
        },
      },
    },
  },
  plugins: [],
};
