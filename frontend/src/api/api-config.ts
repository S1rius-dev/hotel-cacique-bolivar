export function getApiUrl(): string {
  const envUrl = import.meta.env.VITE_API_URL;
  if (envUrl) {
    return envUrl;
  }

  const hostname = window.location.hostname;

  if (hostname === "localhost" || hostname === "127.0.0.1") {
    return "http://localhost:8080";
  }

  // Sin VITE_API_URL y fuera de localhost, se asume la API en el mismo host, puerto 8080.
  return `${window.location.protocol}//${hostname}:8080`;
}
