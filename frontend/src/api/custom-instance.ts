import axios, { type AxiosRequestConfig } from "axios";
import { toast } from "sonner";
import type { useAuth } from "@/app/auth/AuthContext";
import { getApiUrl } from "@/api/api-config";
import { rateLimitStore } from "@/lib/rate-limit-store";
import { readFieldErrors } from "@/lib/extract-error";

export interface ApiError {
  message: string;
  code?: string;
  fieldErrors?: Record<string, string>;
  status?: number;
}
const API_URL = getApiUrl();

let authContextRef: ReturnType<typeof useAuth> | null = null;

export const setAuthContextRef = (auth: ReturnType<typeof useAuth>) => {
  authContextRef = auth;
};

const AXIOS_INSTANCE = axios.create({
  baseURL: API_URL,
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
  paramsSerializer: {
    serialize: (params) => {
      const searchParams = new URLSearchParams();
      for (const [key, value] of Object.entries(params)) {
        if (
          value !== null &&
          typeof value === "object" &&
          !Array.isArray(value)
        ) {
          for (const [k, v] of Object.entries(value)) {
            if (Array.isArray(v)) {
              v.forEach((item) => searchParams.append(k, String(item)));
            } else if (v !== undefined && v !== null) {
              searchParams.append(k, String(v));
            }
          }
        } else if (value !== undefined && value !== null) {
          searchParams.append(key, String(value));
        }
      }
      return searchParams.toString();
    },
  },
});

let isRefreshing = false;
let refreshQueue: Array<() => void> = [];

const processQueue = () => {
  refreshQueue.forEach((cb) => cb());
  refreshQueue = [];
};

AXIOS_INSTANCE.interceptors.response.use(
  (response) => {
    const headers = response.headers as Record<string, string | undefined>;
    rateLimitStore.parseHeaders(headers);
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;
    const data = error.response?.data;
    const headers = error.response?.headers as Record<string, string | undefined>;

    if (status === 429) {
      const retryAfter = headers?.["retry-after"];
      const seconds = retryAfter ? Number(retryAfter) : 60;
      rateLimitStore.setCooldown(seconds);
      const message = `Demasiadas peticiones. Intenta de nuevo en ${seconds} segundo${seconds !== 1 ? "s" : ""}.`;
      toast.error(message);
      // Se rechaza con el mismo texto que ya se mostró: los `onError` que
      // también hacen toast no deben duplicar el aviso en otro idioma.
      return Promise.reject({ message, status: 429 } as ApiError);
    }

    const isRefreshEndpoint =
      originalRequest?.url?.includes("/api/auth/refresh");

    if (
      status === 401 &&
      !isRefreshEndpoint &&
      originalRequest?.headers?.["X-Retry"] !== "true"
    ) {
      originalRequest.headers = { ...originalRequest.headers, "X-Retry": "true" };

      if (isRefreshing) {
        return new Promise((resolve) => {
          refreshQueue.push(() => {
            resolve(AXIOS_INSTANCE(originalRequest));
          });
        });
      }

      isRefreshing = true;

      try {
        await AXIOS_INSTANCE.post("/api/auth/refresh");
        isRefreshing = false;
        processQueue();
        return AXIOS_INSTANCE(originalRequest);
      } catch {
        isRefreshing = false;
        processQueue();

        if (originalRequest?.headers?.["X-Retry"] === "true") {
          authContextRef?.clearUser?.();
        }
      }
    }

    if (data) {
      const apiError: ApiError = {
        message: data?.message || data?.detail || "Error inesperado",
        code: data?.error,
        // Cubre tanto el array `errors: [{ field, message }]` como el mapa
        // `fieldErrors: { campo: mensaje }`.
        fieldErrors: readFieldErrors(data),
        status,
      };

      return Promise.reject(apiError);
    }

    return Promise.reject(error);
  },
);

export const customInstance = <T>(config: AxiosRequestConfig): Promise<T> => {
  return AXIOS_INSTANCE(config).then(({ data }) => data);
};
