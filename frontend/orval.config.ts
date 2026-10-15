import { defineConfig } from "orval";

export default defineConfig({
  hotelApi: {
    input: "http://127.0.0.1:8080/v3/api-docs",
    output: {
      target: "./src/api/generated",
      client: "axios",
      mode: "tags-split",
      clean: true,
      override: {
        mutator: {
          path: "./src/api/custom-instance.ts",
          name: "customInstance",
        },
      },
    },
  },
});
